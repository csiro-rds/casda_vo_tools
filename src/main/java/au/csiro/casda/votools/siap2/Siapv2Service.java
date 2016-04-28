package au.csiro.casda.votools.siap2;

import java.io.IOException;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.result.VotableError;
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
public class Siapv2Service extends Configurable
{
    /**
     * Name to use in generated VOTable results
     */
    static final String CASDA_SIAPV2_RESULT_NAME = "CASDA SIAPv2 Result";

    private static Logger logger = LoggerFactory.getLogger(Siapv2Service.class);

    private List<String> authTrustedIp;

    private Configuration config;

    private boolean ready;

    private TapService tapService;

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
    public Siapv2Service(ConfigurationRegistry configRegistry, TapService tapService) throws ConfigurationException
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
    public void reportSiapv2Error(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportError(CASDA_SIAPV2_RESULT_NAME, errorMsg));
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
    protected List<String> validateSiapv2Job(Map<String, String[]> paramsMap) throws IOException
    {
        List<String> errorList = new ArrayList<>();
        for (Siap2Param param : Siap2Param.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            if (!ArrayUtils.isEmpty(values))
            {
                List<String> result = param.getParamType().getProcessor().validate(paramName, values);

                errorList.addAll(result);
            }
        }

        String[] maxrecArray = paramsMap.get("maxrec");
        if (maxrecArray != null && maxrecArray.length > 0)
        {
            if (maxrecArray.length > 1 || maxrecArray[0].trim().split(" ").length > 1)
            {
                errorList.add(String.format(SiapParamProcessor.USAGE_FAULT_MSG,
                        "Query can only contain a single MAXREC value"));
            }
            else if (!StringUtils.isNumeric(maxrecArray[0].trim()))
            {
                errorList.add(String.format(SiapParamProcessor.USAGE_FAULT_MSG,
                        "The maximum amount of records is invalid. MAXREC must be a valid whole number"));
            }
        }

        return errorList;
    }

    /**
     * Process an already validated TAP query and write the result to the supplied writer. If an error is encountered
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
        List<String> siapv2ValidationErrors = this.validateSiapv2Job(paramsMap);
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
            this.reportSiapv2Error(writer, errorMsg);
            logger.info(CasdaVoToolsEvents.E148.messageBuilder()
                    .addAll(Arrays.asList(buildSiapQueryText(paramsMap),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())), errorMsg, userId))
                    .toString());
            return false;
        }

        try
        {
            String query = buildQuery(paramsMap);

            Map<String, String> tapParams = new HashMap<>();
            tapParams.put(VoKeys.SUBMITTED_MODE, TapService.SUBMITTED_MODE_SYNC);
            tapParams.put(VoKeys.SUBMITTED_TIME, ZonedDateTime.now(ZoneId.of("UTC")).toString());
            tapParams.put(VoKeys.STR_KEY_ADQL_QUERY, query);
            tapParams.put(VoKeys.STR_KEY_SIAP_QUERY, buildSiapQueryText(paramsMap));
            tapParams.put(TapService.STR_KEY_LANG, TapService.STR_ADQL_2_0);
            tapParams.put(TapService.STR_KEY_FORMAT, OutputFormat.VOTABLE.getDefaultContentType());
            tapParams.put(VoKeys.VO_TABLE_HEADING, Siapv2Service.CASDA_SIAPV2_RESULT_NAME);
            tapParams.put(VoKeys.USER_ID, userId);
            tapParams.put(VoKeys.USER_PROJECTS,
                    paramsMap.get(VoKeys.USER_PROJECTS) == null ? null : paramsMap.get(VoKeys.USER_PROJECTS)[0]);
            if (paramsMap.containsKey(VoKeys.STR_KEY_MAXREC)
                    && StringUtils.isNumeric(paramsMap.get(VoKeys.STR_KEY_MAXREC)[0]))
            {
                tapParams.put(VoKeys.STR_KEY_MAXREC, paramsMap.get(VoKeys.STR_KEY_MAXREC)[0]);
            }
            tapService.processQuery(writer, tapParams);
        }
        catch (Exception e)
        {
            logger.info(CasdaVoToolsEvents.E142.messageBuilder()
                    .addAll(Arrays.asList(paramsMap.get(VoKeys.CATALOG), paramsMap.get(VoKeys.PARAM_QUERY_STRING),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                            "Unable to run query: " + e.getMessage(), paramsMap.get(VoKeys.USER_ID)))
                    .toString(), e);
            this.reportSiapv2Error(writer, "Unable to run query: " + e.getMessage());
            return false;
        }

        return true;
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
        AdqlQueryBuilder builder = new AdqlQueryBuilder("ivoa.obscore");

        for (Siap2Param param : Siap2Param.values())
        {
            String paramName = param.toString().toLowerCase();
            String[] values = paramsMap.get(paramName);
            String clause = "";
            if (!ArrayUtils.isEmpty(values))
            {
                clause = param.getParamType().getProcessor().buildQuery(param.getField(0), param.getField(1), values);
            }

            if (StringUtils.isNotBlank(clause))
            {
                builder.withSpecificClause(clause);
            }
        }

        // Limit to just images and visibilities
        builder.withSpecificClause("dataproduct_type IN ('cube', 'image', 'visibility')");

        return builder.toString();
    }

    /**
     * Build up a SIAP query listing the recognised provided params.
     * 
     * @param paramsMap
     *            The map of parameters. There may be multiple values in the array per key which will be joined by an
     *            OR.
     * @return The SIAP query parameter list.
     */
    String buildSiapQueryText(Map<String, String[]> paramsMap)
    {
        StringBuilder builder = new StringBuilder();
        for (Siap2Param param : Siap2Param.values())
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
