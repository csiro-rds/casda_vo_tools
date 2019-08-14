package au.csiro.casda.votools.datalink;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.Utils;
import au.csiro.casda.votools.utils.VoKeys;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Controller that manages the web end-point for the DataLink service providing links to data access services for
 * specific data products.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Controller
public class DataLinkController
{

    private static final String RESULTS_FILENAME = "datalink_results";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    private static Logger logger = LoggerFactory.getLogger(DataLinkController.class);

    @Autowired
    private DataLinkService dataLinkService;

    /**
     * Process a DataLink link request immediately
     * 
     * @param request
     *            the http request - this contains the params for the DataLink request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/datalink/links" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void dataLinkRequest(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/datalink/links' url mapping - servicing {} request.",
                request.getMethod());
        checkReady();
        
        Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
        
        String[] responseFormat = request.getParameterValues("responseformat");
        String errorMsg = validateResponseFormat(responseFormat);
        
        List<String> projectCodes = new ArrayList<String>();
        boolean casdaAdmin = false;

        Utils.addParamsAsStringArray(paramsMap, Utils.getAuthParams(request, dataLinkService.trustAuthHeader(request)));

        String userProjects = paramsMap.get(VoKeys.USER_PROJECTS)[0];
        if (StringUtils.isNotBlank(userProjects))
        {
            casdaAdmin = userProjects.contains(VoKeys.STR_PROJECT_CODES_ALL);
            if (!casdaAdmin)
            {
                projectCodes.addAll(Arrays.asList(userProjects.replace(" ", "").split(",")));
            }
        }

        try
        {
            // Unless they have asked for it in a particular format send the response back in line.
            if (paramsMap.containsKey("responseformat") || RequestMethod.PUT.toString().equals(request.getMethod()))
            {
                // all responses are in votable format
                response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
                // set this header as a hint to the browser for the filename and extension
                response.setHeader(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER_FORMAT,
                        RESULTS_FILENAME, OutputFormat.VOTABLE.getFileExtension()));
            }
            else
            {
                response.setContentType("text/xml");
            }

            
            PrintWriter writer = response.getWriter();
            if (StringUtils.isNotBlank(errorMsg))
            {
                dataLinkService.reportDataLinkError(response.getWriter(), errorMsg);
                return;
            }

            boolean casdaLargeWebDownload = Boolean.parseBoolean(paramsMap.get(VoKeys.LARGE_WEB_DOWNLOAD)[0]);

            dataLinkService.processQuery(writer, paramsMap.get("id"), paramsMap.get(VoKeys.USER_ID)[0],
                    paramsMap.get(VoKeys.LOGIN_SYSTEM)[0], projectCodes, casdaAdmin, casdaLargeWebDownload, new Date());
        }
        catch (IOException e)
        {
            logger.error("Failed to process request: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (DataAccessException e)
        {
            logger.error("Unable to run query: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (InterruptedException e)
        {
            logger.info("Query interrupted.");
        }
        catch (Exception e)
        {
            logger.error("Failed to process request: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Check that the requested format is the only supported format of VOTABLE.
     * 
     * @param responseFormat
     *            The param(s) passed in on the request.
     * @return An erro message, or null if the format is supported.
     */
    private String validateResponseFormat(String[] responseFormat)
    {
        if (ArrayUtils.isEmpty(responseFormat))
        {
            return null;
        }

        for (String formatStr : responseFormat)
        {
            if (OutputFormat.VOTABLE != getFormat(formatStr))
            {
                return "Unsupported RESPONSEFORMAT of '" + formatStr + "'.";
            }
        }
        return null;
    }

    /**
     * Determines the output format to be used based on the user supplied format string. This allows for mime types as
     * well as simple names to be provided by the user.
     * 
     * @param formatStr
     *            The requested format.
     * @return The format to be used.
     */
    public OutputFormat getFormat(String formatStr)
    {
        if (StringUtils.isBlank(formatStr))
        {
            return OutputFormat.VOTABLE;
        }
        formatStr = formatStr.toLowerCase();

        return OutputFormat.findMatchingFormat(formatStr);
    }

    /**
     * Checks is this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    private void checkReady()
    {
        try
        {
            if (!dataLinkService.isReady())
            {
                throw new ConfigurationException("Siapv2Controller is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }

}
