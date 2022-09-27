package au.csiro.casda.votools.siap1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;
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
 * Controller that manages the web end-point for Simple Image Access Protocol v1 queries. Note all siap v1 queries are
 * synchronous.
 * <p>
 * Copyright 2021, CSIRO Australia All rights reserved.
 */
@Controller
public class Siap1Controller
{

    private static final String RESULTS_FILENAME = "sia1_results";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    private static Logger logger = LoggerFactory.getLogger(Siap1Controller.class);

    @Autowired
    private Siap1Service siap1Service;

    /**
     * Perform a Simple Image Access Protocol v1 query immediately
     * 
     * @param request
     *            the http request - this contains the params for the SIA1 request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/sia1/query" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void syncSiap1v2Request(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/sia1/query' url mapping - servicing {} request.",
                request.getMethod());
        checkReady();
        try
        {
            PrintWriter writer = response.getWriter();
            Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());

            // Unless they have asked for it in a particular format send the response back in line.
            if (siap1Service.isMetadataRequest(paramsMap))
            {
                response.setContentType("text/xml");
            }
            else if (paramsMap.containsKey("responseformat")
                    || RequestMethod.PUT.toString().equals(request.getMethod()))
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
            siap1Service.processQuery(writer, paramsMap);
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
     * Request that the SIAP1 metadata is refreshed from the filesystem.
     * 
     * @param request
     *            the http request
     * @param response
     *            the http response
     * @return A result message to be displayed to the user.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    @RequestMapping(value = { "/sia1/reset" }, method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public MessageDTO resetSiap1(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException
    {
        logger.info("Hit the controller for the '/sia1/reset' url mapping - servicing {} request.", request.getMethod());

        siap1Service.refresh();
        checkReady();

        return new MessageDTO(MessageCode.SUCCESS, "Simple Image Access v1 metadata reset completed.");
    }

    /**
     * Checks if this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    private void checkReady()
    {
        try
        {
            if (!siap1Service.isReady())
            {
                throw new ConfigurationException
                ("Siap1Controller is not ready to process requests, due to Siap1Service not being ready.");
            }
        }
        catch (ConfigurationException e)
        {
            logger.error("Failed to ready Siap1Controller: ", e);
            throw new RuntimeException(e);
        }

    }

}
