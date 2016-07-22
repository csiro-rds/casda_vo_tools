package au.csiro.casda.votools.scs;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.Utils;
import au.csiro.casda.votools.utils.VoKeys;
import uws.UWSToolBox;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Controller that manages the web end-point for Simple Cone Search queries.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Controller
public class ScsController
{
    private static final String RESULTS_FILENAME = "cone_search_results";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static Logger logger = LoggerFactory.getLogger(ScsController.class);

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    @Autowired
    private ScsService scsService;

    /**
     * Request a simple cone search, this reads the parameters from the request (see
     * {@link UWSToolBox#getParamsMap(HttpServletRequest)})
     * 
     * @param catalog
     *            the catalogue type, eg continuum_component, continuum_island etc
     * @param request
     *            the http request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/scs/{catalog}" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void scsRequest(@PathVariable String catalog, HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/scs' url mapping - servicing {} request.", request.getMethod());
        checkReady();
        Map<String, String> paramsMap = UWSToolBox.getParamsMap(request);
        paramsMap.put(VoKeys.CATALOG, catalog);
        paramsMap.put(VoKeys.PARAM_QUERY_STRING, request.getQueryString()); // for logging
        paramsMap.putAll(Utils.getAuthParams(request, scsService.trustAuthHeader(request)));
        try
        {
            // all responses are in votable format
            response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
            // set this header as a hint to the browser for the filename and extension
            response.setHeader(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER_FORMAT, RESULTS_FILENAME,
                    OutputFormat.VOTABLE.getFileExtension()));
            response.setContentType("text/xml;content=x-votable");
            PrintWriter writer = response.getWriter();
            scsService.processQuery(writer, paramsMap);
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
     * Request that the cone search metadata is refreshed from the database.
     * 
     * @param request
     *            the http request
     * @param response
     *            the http response
     * @return A result message to be displayed to the user.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    @RequestMapping(value = { "/scs/reset" }, method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public MessageDTO resetScs(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException
    {
        logger.info("Hit the controller for the '/scs/reset' url mapping - servicing {} request.", request.getMethod());

        scsService.refresh();
        checkReady();

        return new MessageDTO(MessageCode.SUCCESS, "Simple Cone Search metadata reset completed.");
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
            if (scsService == null || !scsService.isReady())
            {
                throw new ConfigurationException("ScsController is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }

}
