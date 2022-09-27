package au.csiro.casda.votools.siap1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.result.VotableError;
import au.csiro.casda.votools.siap2.AdqlQueryBuilder;
import au.csiro.casda.votools.surveys.SiapSurvey;
import au.csiro.casda.votools.surveys.SiapSurveysService;
import au.csiro.casda.votools.tap.TapService;
import au.csiro.casda.votools.utils.VoKeys;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2014 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * A service to run Simple Image Access Protocol v1 (SIAP1) queries against the database.
 * <p>
 * Copyright 2021, CSIRO Australia All rights reserved.
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Siap1Service extends Configurable
{
    /**
     * Name to use in generated VOTable results
     */
    static final String CASDA_SIAP1_RESULT_NAME = "CASDA SIA1 Result";

    private static Logger logger = LoggerFactory.getLogger(Siap1Service.class);

    private final freemarker.template.Configuration freemarkerConfiguration;

    private Configuration config;

    private boolean ready;

    private TapService tapService;

    private int siap1OutputLimit;

    private String siap1DefaultMaxrec;

    private String siap1MetadataResponse;

    private List<String> surveyList;

    private SiapSurveysService siapSurveysService;

    private Map<String, Siap1Field> fieldMap;

    private Map<String, String> votableFieldMap;

    private String siap1AccessUrl;

    private String siap1FormatsTable;

    /**
     * Constructor
     * 
     * @param configRegistry
     *            The configuration registry
     * @param tapService
     *            The table access protocol service which will run queries.
     * @param siapSurveysService
     *            The list of surveys that can be queried
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @Autowired
    public Siap1Service(ConfigurationRegistry configRegistry, TapService tapService,
            SiapSurveysService siapSurveysService) throws ConfigurationException
    {
        this.tapService = tapService;
        this.siapSurveysService = siapSurveysService;
        // Register for callbacks when configuration changes.
        configRegistry.register(this);
        freemarkerConfiguration =
                new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_31);
        freemarkerConfiguration.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        ready = false;
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        if (!ready && config != null && tapService.isReady() && siapSurveysService.isReady())
        {
            // authTrustedIp = config.getList("auth.trusted.ip");
            surveyList = siapSurveysService.getSurveyCodeList();
            ((SurveyParamProcessor) Siap1Param.SURVEY.getProcessor()).setSurveys(surveyList);

            fieldMap = readFields();
            votableFieldMap = buildVotableFieldMap(fieldMap);

            siap1OutputLimit = Integer.parseInt(config.get(ConfigKeys.SIA1_OUTPUT_LIMIT.getKey()));
            siap1DefaultMaxrec = config.get(ConfigKeys.SIA1_DEFAULT_MAX_REC.getKey());
            siap1AccessUrl = config.get(ConfigKeys.SIA1_ACCESS_URL.getKey());
            if (CollectionUtils.isNotEmpty(surveyList) && (StringUtils.isBlank(siap1AccessUrl)
                    || !siap1AccessUrl.contains("{obs_publisher_did}") || !siap1AccessUrl.contains("{access_format}")
                    || !siap1AccessUrl.contains("{pos}") || !siap1AccessUrl.contains("{size}")))
            {
                logger.error("The property {} must be defined and include the {obs_publisher_did}, {access_format}"
                        + ", {pos} and {size} placeholders.", ConfigKeys.SIA1_ACCESS_URL.getKey());
                logger.info("{} was {}", ConfigKeys.SIA1_ACCESS_URL.getKey(), siap1AccessUrl);
                return ready;
            }
            siap1FormatsTable =config.get(ConfigKeys.SIA1_FORMAT_TABLE.getKey());
            if (CollectionUtils.isNotEmpty(surveyList) && StringUtils.isBlank(siap1FormatsTable))
            {
                logger.error("The property {} must be defined.", ConfigKeys.SIA1_FORMAT_TABLE.getKey());
                return ready;
            }
            ready = true;
        }
        return ready;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    /**
     * Trigger refreshing the sia1 metadata.
     */
    public void refresh()
    {
        ready = false;
        siapSurveysService.refresh();
    }

    /**
     * Reports an error in an SIA 1 request by outputting a VOTABLE to the writer.
     * 
     * @param writer
     *            The writer to send the error info to.
     * @param errorMsg
     *            The description of the error(s).
     * @throws IOException
     *             If the error cannot be written.
     */
    public void reportSiap1Error(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportError(CASDA_SIAP1_RESULT_NAME, errorMsg));
    }

    /**
     * Validate the parameters passed to a SIAP v2 query request.
     * 
     * @param paramsMap
     *            The parameters for this job. All keys are assumed to be lowercase.
     * 
     * @return the validation error message if the job is not valid, null if the job is valid.
     * @throws IOException
     *             If an error cannot be written to the writer.
     */
    protected List<String> validateSiap1Job(Map<String, String[]> paramsMap) throws IOException
    {
        List<String> errorList = new ArrayList<>();

        if (!isEnabled())
        {
            errorList.add("FatalFault: SIAP1 is not supported by this service.");
            return errorList;
        }

        EnumSet<Siap1Param> paramsToCheck = EnumSet.allOf(Siap1Param.class);
        if (isMetadataRequest(paramsMap))
        {
            paramsToCheck = EnumSet.noneOf(Siap1Param.class);
        }

        for (Siap1Param param : paramsToCheck)
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (!ArrayUtils.isEmpty(values))
            {
                if (values.length > 1)
                {
                    errorList.add(String.format(Siap1ParamProcessor.USAGE_FAULT_MSG,
                            "Only a single " + paramName.toUpperCase() + " value may be specified"));
                }
                else
                {
                    List<String> result = param.getProcessor().validate(paramName, values);

                    errorList.addAll(result);
                }
            }
            else if (param.isRequired())
            {
                errorList.add(String.format(Siap1ParamProcessor.USAGE_FAULT_MSG,
                        "Parameter " + paramName.toUpperCase() + " is required"));
            }
        }

        return errorList;
    }

    /**
     * @return True if the SIAP1 service is enabled in this deployment
     */
    public boolean isEnabled()
    {
        try
        {
            return isReady() && CollectionUtils.isNotEmpty(surveyList);
        }
        catch (ConfigurationException e)
        {
            logger.error("Unable to check if enabled " + e.getMessage());
            return false;
        }
    }

    /**
     * Identify if this request is just for the service metadata.
     * 
     * @param paramsMap
     *            The parameters for this job. All keys are assumed to be lowercase.
     * 
     * @return true if the request is for metadata, false otherwise
     */
    public boolean isMetadataRequest(Map<String, String[]> paramsMap)
    {
        String[] values = paramsMap.get(Siap1Param.FORMAT.toString().toLowerCase());
        if (values != null)
        {
            for (String value : values)
            {
                if ("metadata".equalsIgnoreCase(value))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processMetadataQuery(Writer writer, Map<String, String[]> paramsMap) throws IOException
    {
        String metadataString;
        if (StringUtils.isNotBlank(siap1MetadataResponse))
        {
            File responseFile = new File(siap1MetadataResponse);
            metadataString = FileUtils.readFileToString(responseFile);
        }
        else
        {
            Map<String, Object> model = new HashMap<>();
            String surveyCode = paramsMap.get(Siap1Param.SURVEY.toString().toLowerCase())[0];

            model.put("survey", siapSurveysService.getSurvey(surveyCode).getName());

            SizeParamProcessor spp = (SizeParamProcessor) Siap1Param.SIZE.getProcessor();
            model.put("defaultSizeDegrees", spp.getDefaultSizeDegrees());
            model.put("maxSizeDegrees", spp.getMaxSizeDegrees());

            FormatParamProcessor fpp = (FormatParamProcessor) Siap1Param.FORMAT.getProcessor();
            model.put("formats", fpp.getAllowedFormats());

            List<Siap1Field> orderedFields = new ArrayList<>(fieldMap.values());
            Collections.sort(orderedFields, (Comparator.<Siap1Field> comparingInt(field1 -> field1.getOrder())
                    .thenComparingInt(field2 -> field2.getOrder())));
            model.put("outputFields", orderedFields);

            StringWriter result = new StringWriter();
            String templatePath = "templates/siap1-metadata.xml.ftl";
            Template template;
            try
            {
                template = freemarkerConfiguration.getTemplate(templatePath, Charsets.UTF_8.name());
                template.process(model, result);
            }
            catch (IOException | TemplateException e)
            {
                throw new RuntimeException("Error processing FTL at path " + templatePath, e);
            }
            metadataString = result.toString();
        }

        writer.write(metadataString);

        return true;
    }

    /**
     * Validate and process a SIAP1 query and write the result to the supplied writer. If an error is encountered the
     * error will be written in VOTABLE format to the writer.
     *
     * @param writer
     *            The destination for the query output.
     * @param paramsMap
     *            The parameters for this job.
     * @return true if the query was successful, false if an error occurred
     * @throws InterruptedException
     *             if the job is interrupted
     * @throws IOException
     *             if an error occurs using writer
     */
    public boolean processQuery(Writer writer, Map<String, String[]> paramsMap) throws InterruptedException, IOException
    {
        ZonedDateTime start = ZonedDateTime.now();

        // Parameters to be validated
        List<String> siapv2ValidationErrors = this.validateSiap1Job(paramsMap);
        String userId = paramsMap.get(VoKeys.USER_ID) == null ? null : paramsMap.get(VoKeys.USER_ID)[0];

        if (!siapv2ValidationErrors.isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            for (String error : siapv2ValidationErrors)
            {
                builder.append(error);
                builder.append("\r\n");
            }
            String errorMsg = builder.toString();
            this.reportSiap1Error(writer, errorMsg);
            logger.info(CasdaVoToolsEvents.E178.messageBuilder()
                    .addAll(Arrays.asList(buildSiap1QueryText(paramsMap),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())), errorMsg, userId))
                    .toString());
            return false;
        }

        if (isMetadataRequest(paramsMap))
        {
            return processMetadataQuery(writer, paramsMap);
        }

        try
        {
            String query = buildQuery(paramsMap);

            LinkedHashMap<String, String[]> metaDataMap = new LinkedHashMap<String, String[]>();
            metaDataMap.put(VoKeys.STR_KEY_SERVICE_PROTOCOL, new String[] { "1.0", "SIAP1" });
            addParamsToMetdataMap(metaDataMap, paramsMap);

            Map<String, String> tapParams = new HashMap<>();
            tapParams.put(VoKeys.SUBMITTED_MODE, TapService.SUBMITTED_MODE_SYNC);
            tapParams.put(VoKeys.SUBMITTED_TIME, ZonedDateTime.now(ZoneId.of("UTC")).toString());
            tapParams.put(VoKeys.STR_KEY_ADQL_QUERY, query);
            tapParams.put(VoKeys.STR_KEY_SIAP_QUERY, buildSiap1QueryText(paramsMap));
            tapParams.put(TapService.STR_KEY_LANG, TapService.STR_ADQL_2_0);
            tapParams.put(TapService.STR_KEY_FORMAT, OutputFormat.VOTABLE.getDefaultContentType());
            tapParams.put(VoKeys.VO_TABLE_HEADING, Siap1Service.CASDA_SIAP1_RESULT_NAME);
            tapParams.put(VoKeys.USER_ID, userId);
            tapParams.put(VoKeys.USER_PROJECTS,
                    paramsMap.get(VoKeys.USER_PROJECTS) == null ? null : paramsMap.get(VoKeys.USER_PROJECTS)[0]);
            tapParams.put(VoKeys.STR_KEY_MAXREC, calcMaxRec(paramsMap));
            tapService.processQuery(writer, tapParams, metaDataMap, new ArrayList<>(), votableFieldMap);
        }
        catch (Exception e)
        {
            logger.info(CasdaVoToolsEvents.E177.messageBuilder()
                    .addAll(Arrays.asList(buildSiap1QueryText(paramsMap),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                            "Unable to run query: " + e.getMessage(), paramsMap.get(VoKeys.USER_ID)))
                    .toString(), e);
            this.reportSiap1Error(writer, "Unable to run query: " + e.getMessage());
            return false;
        }

        return true;
    }

    private String calcMaxRec(Map<String, String[]> paramsMap)
    {
        String maxrec = siap1DefaultMaxrec;
        if (paramsMap.containsKey(VoKeys.STR_KEY_MAXREC))
        {
            int value = Integer.parseInt(paramsMap.get(VoKeys.STR_KEY_MAXREC)[0]);
            if (value > siap1OutputLimit)
            {
                value = siap1OutputLimit;
            }
            maxrec = String.valueOf(value);
        }
        return maxrec;
    }

    private void addParamsToMetdataMap(Map<String, String[]> metaDataMap, Map<String, String[]> paramsMap)
    {
        for (Siap1Param param : Siap1Param.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (ArrayUtils.isNotEmpty(values))
            {
                metaDataMap.put(param.toString(), new String[] { StringUtils.join(values, ","), "" });
            }
        }
    }

    private String buildPosQuery(double ra, double dec, double radius)
    {
        String value = String.format("INTERSECTS(CIRCLE('ICRS GEOCENTER', %s, %s, %s),s_region)=1", ra, dec, radius);
        return value;
    }

    /**
     * Build up an ADQL query based on the provided params. Only recognised parameters will considered in the query.
     * 
     * @param paramsMap
     *            The map of parameters. There may be multiple values in the array per key which will be joined by an
     *            OR.
     * @return The ADQL query.
     */
    String buildQuery(Map<String, String[]> paramsMap)
    {
        String surveyCode = paramsMap.get(Siap1Param.SURVEY.toString().toLowerCase())[0];
        SiapSurvey survey = siapSurveysService.getSurvey(surveyCode);

        // Cross table join
        AdqlQueryBuilder builder = new AdqlQueryBuilder("ivoa.obscore, " + siap1FormatsTable);

        for (Siap1Param param : Siap1Param.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            String clause = "";
            clause = param.getProcessor().buildQuery(param.getField(0), param.getField(1), values);

            if (StringUtils.isNotBlank(clause))
            {
                builder.withSpecificClause(clause);
            }
        }

        // Add cone search criteria
        String[] sizeParam = paramsMap.get(Siap1Param.SIZE.toString().toLowerCase());
        SizeParamProcessor spp = (SizeParamProcessor) Siap1Param.SIZE.getProcessor();
        double radius = spp.getSearchRadius(sizeParam);
        double[] sizeDeg = spp.getSizeDegrees(sizeParam);
        PosParamProcessor ppp = (PosParamProcessor) Siap1Param.POS.getProcessor();
        double[] raDec = ppp.getRaDec(paramsMap.get(Siap1Param.POS.toString().toLowerCase())[0]);
        String clause = buildPosQuery(raDec[0], raDec[1], radius);
        builder.withSpecificClause(clause);
        String sizeString = String.format("%f,%f", sizeDeg[0], sizeDeg[1]);
        String posString = String.format("%f,%f", raDec[0], raDec[1]);

        // order by
        String distance = String.format("DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),POINT('ICRS GEOCENTER',%f, %f))",
                raDec[0], raDec[1]);
        builder.withOutputColumns(String.format("%s as distance_deg",  distance));
        builder.withOutputColumns(String.format("to_char(%s, 'FM990.9999') as \"distance\"",  distance));
        builder.withOutputColumns(String.format("%s.content_type as access_format", siap1FormatsTable));
        builder.withOutputColumns(String.format("%s.pix_flags", siap1FormatsTable));
        builder.setOrderBy("1 ASC, 2 ASC");

        // Add fields to be retrieved
        builder.withOutputColumns(String.format("'%s' as survey", survey.getName()));
        builder.withOutputColumns("'Cutout from '||ivoa.obscore.filename as image_title");
        // Centre of the cutout image
        builder.withOutputColumns(String.format("%f as s_ra", raDec[0]));
        builder.withOutputColumns(String.format("%f as s_dec", raDec[1]));
        builder.withOutputColumns("ivoa.obscore.instrument_name");
        builder.withOutputColumns("ivoa.obscore.t_min", "2 as n_axes");
        // Estimated size of the cutout
        builder.withOutputColumns(String.format(
                "ceil(%f/sqrt(s_fov/(s_xel1*s_xel2)))||' '||ceil(%f/sqrt(s_fov/(s_xel1*s_xel2))) as n_axis", sizeDeg[0],
                sizeDeg[1]));
        builder.withOutputColumns("to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999')||' '||"
                + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999') as image_scale");
        builder.withOutputColumns(String.format(
                "to_char(ceil(ivoa.obscore.access_estsize*1024*(%f/s_fov)), 'FM999999999999') as est_size_bytes",
                sizeDeg[0] * sizeDeg[1]));
        
        String accessUrl = siap1AccessUrl.replaceAll("\\{obs_publisher_did}", "'||ivoa.obscore.obs_publisher_did||'")
                .replaceAll("\\{access_format}", String.format("'||%s.content_type||'", siap1FormatsTable))
                .replaceAll("\\{pos}", posString).replaceAll("\\{size}", sizeString);
        builder.withOutputColumns(String.format("'%s' as access_url", accessUrl));

        // Restrict the query to the survey data
        builder.withSpecificClause(survey.getWhereClause());

        return builder.toString();
    }

    /**
     * Build up a SIAP1 query listing the recognised provided params.
     * 
     * @param paramsMap
     *            The map of parameters. There may be multiple values in the array per key which will be joined by an
     *            OR.
     * @return The SIAP query parameter list.
     */
    String buildSiap1QueryText(Map<String, String[]> paramsMap)
    {
        StringBuilder builder = new StringBuilder();
        for (Siap1Param param : Siap1Param.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (values != null)
            {
                for (String val : values)
                {
                    if (StringUtils.isNotEmpty(val))
                    {
                        if (builder.length() > 0)
                        {
                            builder.append("&");
                        }
                        builder.append(paramName.toUpperCase()).append("=").append(val);
                    }
                }
            }
        }

        return builder.toString();
    }

    /**
     * Read the metadata for the output fields.
     * 
     * @return Map of field names and their definitions.
     */
    Map<String, Siap1Field> readFields()
    {
        Map<String, Siap1Field> fieldMap = new HashMap<>();
        try (InputStream propertiesStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("siap1-fields.properties"))
        {
            Properties fieldProperties = new Properties();
            fieldProperties.load(propertiesStream);
            // Scan for field names
            for (Entry<Object, Object> prop : fieldProperties.entrySet())
            {
                String propKey = (String) prop.getKey();
                if (propKey.endsWith(".key"))
                {
                    String fieldName = propKey.substring(0, propKey.indexOf("."));
                    Siap1Field field = new Siap1Field(fieldName, getMetadataKey((String) prop.getValue()));
                    field.setDatatype(fieldProperties.getProperty(fieldName + ".datatype"));
                    field.setDescription(fieldProperties.getProperty(fieldName + ".description"));
                    field.setOrder(Integer.parseInt(fieldProperties.getProperty(fieldName + ".order")));
                    if (fieldProperties.containsKey(fieldName + ".arraysize"))
                    {
                        field.setArraysize(fieldProperties.getProperty(fieldName + ".arraysize"));
                    }
                    if (fieldProperties.containsKey(fieldName + ".unit"))
                    {
                        field.setUnit(fieldProperties.getProperty(fieldName + ".unit"));
                    }
                    if (fieldProperties.containsKey(fieldName + ".ucd"))
                    {
                        field.setUcd(fieldProperties.getProperty(fieldName + ".ucd"));
                    }
                    fieldMap.put(fieldName, field);
                }
            }
        }
        catch (IOException e)
        {
            logger.error("Could not load siap1 field properties: ", e);
            throw new RuntimeException("Could not load siap1 field properties");
        }
        return fieldMap;

    }

    private String getMetadataKey(String configKey)
    {
        String parts[] = configKey.split("\\|");
        final int FULL_TABLE_KEY_PARTS_COUNT = 3;
        if (parts.length == FULL_TABLE_KEY_PARTS_COUNT)
        {
            String tableMetdataKey = tapService.getTableMetdataKey(parts[0], parts[1]);
            if (StringUtils.isNotBlank(tableMetdataKey))
            {
                return String.format("%s|%s", tableMetdataKey, parts[FULL_TABLE_KEY_PARTS_COUNT - 1]);
            }
        }
        return configKey;
    }

    /**
     * Create a map of field definitions for use in a VOTable response.
     * 
     * @param fieldMap
     *            A map of field definitions.
     * @return A map of field keys to FIELD XML entries.
     */
    Map<String, String> buildVotableFieldMap(Map<String, Siap1Field> fieldMap)
    {
        Map<String, String> votablFieldMap = new HashMap<>();
        for (Siap1Field field : fieldMap.values())
        {
            votablFieldMap.put(field.getKey(), field.getFieldDef());
        }
        return votablFieldMap;
    }
}
