package au.csiro.casda.votools.siap2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.EnumUtils;
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
 * Controller that manages the web end-point for Simple Image Access Protocol v2 queries. Note all siap v2 queries are
 * synchronous.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Controller
public class Siapv2Controller
{

    private static final String RESULTS_FILENAME = "sia2_results";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    private static Logger logger = LoggerFactory.getLogger(Siapv2Controller.class);

    @Autowired
    private Siapv2Service siapv2Service;

    /**
     * Perform a Simple Image Access Protocol v2 query immediately
     * 
     * @param request
     *            the http request - this contains the params for the SIAP request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/sia2/query" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void syncSiapv2Request(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/sia2/query' url mapping - servicing {} request.",
                request.getMethod());
        checkReady();
        try
        {
            PrintWriter writer = response.getWriter();
            Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
            for (String key : paramsMap.keySet())
            {
                if (!EnumUtils.isValidEnum(Siap2Param.class, key.toUpperCase())
                        && !EnumUtils.isValidEnum(Siap2OutputParamType.class, key.toUpperCase()))
                {
                    logger.info("Invalid request parameter: " + key.toUpperCase());
                    response.setContentType("text/xml");
                    siapv2Service.reportSiapv2Error(writer, String.format(SiapParamProcessor.USAGE_FAULT_MSG,
                            "Invalid parameter name " + key.toUpperCase()));
                    return;
                }
            }

            Utils.addParamsAsStringArray(paramsMap,
                    Utils.getAuthParams(request, siapv2Service.trustAuthHeader(request)));

            // Unless they have asked for it in a particular format send the response back in line.
            if (paramsMap.containsKey("responseformat") || RequestMethod.PUT.toString().equals(request.getMethod()))
            {
                response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
                // set this header as a hint to the browser for the filename and extension
                response.setHeader(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER_FORMAT,
                        RESULTS_FILENAME, OutputFormat.VOTABLE.getFileExtension()));
            }
            else
            {
                response.setContentType("text/xml");
            }
            siapv2Service.processQuery(writer, paramsMap);
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
     * Checks is this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    private void checkReady()
    {
        try
        {
            if (!siapv2Service.isReady())
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
