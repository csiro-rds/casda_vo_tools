package au.csiro.casda.votools.ssap;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharEncoding;
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
import au.csiro.casda.votools.tap.TapService;
import au.csiro.casda.votools.utils.VoKeys;

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
 * A service to run Simple Image Access (SIAv2) queries against the database.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SsapService extends Configurable
{
    /**
     * Name to use in generated VOTable results
     */
    static final String CASDA_SSAP_RESULT_NAME = "CASDA SSAP Result";

    private static Logger logger = LoggerFactory.getLogger(SsapService.class);

    
    private List<String> authTrustedIp;

    private Configuration config;

    private boolean ready;

    private TapService tapService;

    private String ssapTable;

    private int ssapOutputLimit;

    private String ssapDefaultMaxrec;

    private String ssapMetadataResponse;

    /**
     * Constructor
     * 
     * @param configRegistry
     *            The configuration registry
     * @param tapService
     *            The table access protocol service which will run queries.
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @Autowired
    public SsapService(ConfigurationRegistry configRegistry, TapService tapService) throws ConfigurationException
    {
        this.tapService = tapService;
        // Register for callbacks when configuration changes.
        configRegistry.register(this);
    }

    /**
     * Checks whether the request is from a trusted ip address, and so we can trust the authorisation information in the
     * header
     * 
     * @param request
     *            the user's request
     * @return true if the the source of the request is a trusted ip address
     */
    public boolean trustAuthHeader(HttpServletRequest request)
    {
        boolean trustAuthHeader = this.authTrustedIp.contains(request.getRemoteAddr());
        logger.info("Request from {}, is from trusted ip address {}", request.getRemoteAddr(), trustAuthHeader);
        return trustAuthHeader;
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
        if (!ready && config != null && tapService.isReady())
        {
            authTrustedIp = config.getList("auth.trusted.ip");
            ssapTable = config.get(ConfigKeys.SSAP_TABLE.getKey());
            ssapOutputLimit = Integer.parseInt(config.get(ConfigKeys.SSAP_OUTPUT_LIMIT.getKey()));
            ssapDefaultMaxrec = config.get(ConfigKeys.SSAP_DEFAULT_MAX_REC.getKey());
            ssapMetadataResponse = config.get(ConfigKeys.SSAP_METADATA_RESPONSE.getKey());
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
     * Reports an error in an SCS request by outputting a VOTABLE to the writer.
     * 
     * @param writer
     *            The writer to send the error info to.
     * @param errorMsg
     *            The description of the error(s).
     * @throws IOException
     *             If the error cannot be written.
     */
    public void reportSsapError(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportError(CASDA_SSAP_RESULT_NAME, errorMsg));
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
    protected List<String> validateSsapJob(Map<String, String[]> paramsMap) throws IOException
    {
        List<String> errorList = new ArrayList<>();
        
        if (!isEnabled())
        {
            errorList.add("FatalFault: SSAP is not supported by this service.");
            return errorList;
        }
        
        
        EnumSet<SsapParam> paramsToCheck = EnumSet.allOf(SsapParam.class);
        if (isMetadataRequest(paramsMap))
        {
            paramsToCheck = EnumSet.of(SsapParam.REQUEST, SsapParam.FORMAT, SsapParam.VERSION);
        }
        
        
        for (SsapParam param : paramsToCheck)
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (!ArrayUtils.isEmpty(values))
            {
                if (values.length > 1)
                {
                    errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
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
                errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                        "Parameter " + paramName.toUpperCase() + " is required"));
            }
        }

        return errorList;
    }

    
    /**
     * @return True if the SSAP service is enabled in this deployment
     */
    public boolean isEnabled()
    {
        try
        {
            return isReady() && !StringUtils.isEmpty(ssapTable);
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
        String[] values = paramsMap.get(SsapParam.FORMAT.toString().toLowerCase());
        if (values != null)
        {
            for (String value : values)
            {
                if ("metadata".equals(value.toLowerCase()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processMetadataQuery(Writer writer) throws IOException
    {
        String metadataString;
        if (StringUtils.isNotBlank(ssapMetadataResponse))
        {
            File responseFile = new File(ssapMetadataResponse);
            metadataString = FileUtils.readFileToString(responseFile);
        }
        else
        {
            metadataString =
                    IOUtils.toString(this.getClass().getResource("/templates/ssap-metadata.xml"), CharEncoding.UTF_8);
        }

        metadataString = metadataString.replaceAll("\\$\\{maxSizeDegrees\\}",
                String.valueOf(SizeParamProcessor.getMaxSizeDegrees()));

        writer.write(metadataString);

        return true;
    }

    /**
     * Validate and process a SSAP query and write the result to the supplied writer. If an error is encountered
     * the error will be written in VOTABLE format to the writer.
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
        List<String> siapv2ValidationErrors = this.validateSsapJob(paramsMap);
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
            this.reportSsapError(writer, errorMsg);
            logger.info(CasdaVoToolsEvents.E148.messageBuilder()
                    .addAll(Arrays.asList(buildSsapQueryText(paramsMap),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())), errorMsg, userId))
                    .toString());
            return false;
        }

        if (isMetadataRequest(paramsMap))
        {
            return processMetadataQuery(writer);
        }
        
        try
        {
            String query = buildQuery(paramsMap);

            LinkedHashMap<String, String[]> metaDataMap = new LinkedHashMap<String, String[]>();
            metaDataMap.put(VoKeys.STR_KEY_SERVICE_PROTOCOL, new String[] { "1.1", "SSAP" });
            addParamsToMetdataMap(metaDataMap, paramsMap);

            Map<String, String> tapParams = new HashMap<>();
            tapParams.put(VoKeys.SUBMITTED_MODE, TapService.SUBMITTED_MODE_SYNC);
            tapParams.put(VoKeys.SUBMITTED_TIME, ZonedDateTime.now(ZoneId.of("UTC")).toString());
            tapParams.put(VoKeys.STR_KEY_ADQL_QUERY, query);
            tapParams.put(VoKeys.STR_KEY_SSAP_QUERY, buildSsapQueryText(paramsMap));
            tapParams.put(TapService.STR_KEY_LANG, TapService.STR_ADQL_2_0);
            tapParams.put(TapService.STR_KEY_FORMAT, OutputFormat.VOTABLE.getDefaultContentType());
            tapParams.put(VoKeys.VO_TABLE_HEADING, SsapService.CASDA_SSAP_RESULT_NAME);
            tapParams.put(VoKeys.USER_ID, userId);
            tapParams.put(VoKeys.USER_PROJECTS,
                    paramsMap.get(VoKeys.USER_PROJECTS) == null ? null : paramsMap.get(VoKeys.USER_PROJECTS)[0]);
            tapParams.put(VoKeys.STR_KEY_MAXREC, calcMaxRec(paramsMap));
            tapService.processQuery(writer, tapParams, metaDataMap);
        }
        catch (Exception e)
        {
            logger.info(CasdaVoToolsEvents.E142.messageBuilder()
                    .addAll(Arrays.asList(paramsMap.get(VoKeys.CATALOG), paramsMap.get(VoKeys.PARAM_QUERY_STRING),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                            "Unable to run query: " + e.getMessage(), paramsMap.get(VoKeys.USER_ID)))
                    .toString(), e);
            this.reportSsapError(writer, "Unable to run query: " + e.getMessage());
            return false;
        }

        return true;
    }

    private String calcMaxRec(Map<String, String[]> paramsMap)
    {
        String maxrec = ssapDefaultMaxrec; 
        if (paramsMap.containsKey(VoKeys.STR_KEY_MAXREC))
        {
            int value = Integer.parseInt(paramsMap.get(VoKeys.STR_KEY_MAXREC)[0]);
            if (value > ssapOutputLimit)
            {
                value = ssapOutputLimit;
            }
            maxrec = String.valueOf(value);
        }
        return maxrec;
    }

    private void addParamsToMetdataMap(LinkedHashMap<String, String[]> metaDataMap, Map<String, String[]> paramsMap)
    {
        for (SsapParam param : SsapParam.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (ArrayUtils.isNotEmpty(values))
            {
                metaDataMap.put(param.toString(), new String[] {StringUtils.join(values, ","), ""});
            }
        }
    }

    private String buildPosQuery(double ra, double dec, String size)
    {
        double radius = new BigDecimal(size).divide(new BigDecimal(2)).doubleValue();
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
        AdqlQueryBuilder builder = new AdqlQueryBuilder(ssapTable);

        for (SsapParam param : SsapParam.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            String clause = "";
            if (ArrayUtils.isNotEmpty(values))
            {
                clause = param.getProcessor().buildQuery(param.getField(0), param.getField(1), values);
            }

            if (StringUtils.isNotBlank(clause))
            {
                builder.withSpecificClause(clause);
            }
        }

        if (ArrayUtils.isNotEmpty(paramsMap.get(SsapParam.POS.toString().toLowerCase())))
        {
            String[] sizeParam =  paramsMap.get(SsapParam.SIZE.toString().toLowerCase());
            String radius =  ArrayUtils.isNotEmpty(sizeParam) ? sizeParam[0] : "0.05";
            PosParamProcessor ppp = (PosParamProcessor) SsapParam.POS.getProcessor();
            double[] raDec = ppp.getRaDec(paramsMap.get(SsapParam.POS.toString().toLowerCase())[0]); 
            String clause = buildPosQuery(raDec[0], raDec[1], radius);
            builder.withSpecificClause(clause);
        }

        return builder.toString();
    }

    /**
     * Build up a SSAP query listing the recognised provided params.
     * 
     * @param paramsMap
     *            The map of parameters. There may be multiple values in the array per key which will be joined by an
     *            OR.
     * @return The SIAP query parameter list.
     */
    String buildSsapQueryText(Map<String, String[]> paramsMap)
    {
        StringBuilder builder = new StringBuilder();
        for (SsapParam param : SsapParam.values())
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

}
