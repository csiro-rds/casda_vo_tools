package au.csiro.casda.votools.ssap;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.datalink.DataLinkService;
import au.csiro.casda.votools.datalink.DataLinkVoTableBuilder;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.siap2.SiapParamProcessor;
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
 * Controller that manages the web end-point for Simple Image Access Protocol v2 queries. Note all siap v2 queries are
 * synchronous.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Controller
public class SsapController
{

    private static final String RESULTS_FILENAME = "ssa_results";

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    private static Logger logger = LoggerFactory.getLogger(SsapController.class);

    @Autowired
    private SsapService ssapService;
    
    @Autowired
    private DataLinkService dataLinkService;

    /**
     * Perform a Simple Image Access Protocol v2 query immediately
     * 
     * @param request
     *            the http request - this contains the params for the SIAP request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/ssa/query" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void syncSsapv2Request(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/ssa/query' url mapping - servicing {} request.",
                request.getMethod());
        checkReady();
        try
        {
            PrintWriter writer = response.getWriter();
            Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
//            for (String key : paramsMap.keySet())
//            {
//                if (!EnumUtils.isValidEnum(SsapParam.class, key.toUpperCase())
//                        && !EnumUtils.isValidEnum(SsapOutputParamType.class, key.toUpperCase()))
//                {
//                    logger.info("Invalid request parameter: " + key.toUpperCase());
//                    response.setContentType("text/xml");
//                    ssapService.reportSsapError(writer, String.format(SsapParamProcessor.USAGE_FAULT_MSG,
//                            "Invalid parameter name " + key.toUpperCase()));
//                    return;
//                }
//            }

            Utils.addParamsAsStringArray(paramsMap,
                    Utils.getAuthParams(request, ssapService.trustAuthHeader(request)));

            // Unless they have asked for it in a particular format send the response back in line.
            if (ssapService.isMetadataRequest(paramsMap))
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
            ssapService.processQuery(writer, paramsMap);
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
     * Process a Download request immediately
     * 
     * @param request
     *            the http request - this contains the params for the download request
     * @param response
     *            the http response
     * @throws IOException 
     * 			  thrown if url is invalid
     */
    @RequestMapping(value = { "/ssa/download" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void downloadRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Hit the controller for the '/download' url mapping - servicing {} request.",
                request.getMethod());
        checkReady();
        
        ZonedDateTime start = ZonedDateTime.now();
        
        Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
        
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
        PrintWriter writer = response.getWriter();
        
        Object linkOrBuilder = dataLinkService.processDownload(paramsMap.get("id")[0], paramsMap.get(VoKeys.USER_ID)[0],
                paramsMap.get(VoKeys.LOGIN_SYSTEM)[0], projectCodes, casdaAdmin, new Date());
        
        if(linkOrBuilder instanceof DataLinkVoTableBuilder)
        {
        	try 
        	{
				writer.append(((DataLinkVoTableBuilder) linkOrBuilder).getXml());
			} 
        	catch (JAXBException e) 
        	{
                logger.error(CasdaVoToolsEvents.E150.messageBuilder()
                        .addAll(Arrays.asList(paramsMap.get("id")[0],
                               CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                               "failed to build AccessData URI's: " + e.getMessage(), paramsMap.get(VoKeys.USER_ID)[0]))
                        .toString(), e);
                dataLinkService.reportDataLinkError(writer, String.format(SiapParamProcessor.TRANSIENT_FAULT_MSG));
			}
        }
        else if(linkOrBuilder instanceof String)
        {
        	response.sendRedirect((String)linkOrBuilder);   
        }
        else
        {
            logger.error("Failed to process request: datalink service returned on object not of type: "
            		+ "'String' or 'DataLinkVoTableBuilder'");
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
            if (!ssapService.isReady())
            {
                throw new ConfigurationException
                ("SsapController is not ready to process requests, due to SsapService not being ready.");
            }
            if (!dataLinkService.isReady())
            {
                throw new ConfigurationException
                ("SsapController is not ready to process requests, due to DatalinkService not being ready.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }

}
