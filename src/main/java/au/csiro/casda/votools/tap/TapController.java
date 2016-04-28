package au.csiro.casda.votools.tap;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uws.UWSException;
import uws.UWSToolBox;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.Utils;
import au.csiro.casda.votools.utils.VoKeys;
import au.csiro.casda.votools.uws.UWServiceInterface;

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
 * Controller that manages the web end-point for Table Access Protocol queries. Both synchronous and asynchronous
 * requests are managed from this end-point.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Controller
public class TapController
{
    private static final String CONTENT_DISPOSITION = "content-disposition";

    private static Logger logger = LoggerFactory.getLogger(TapController.class);

    private static final String CONTENT_DISPOSITION_HEADER_FORMAT = "attachment; filename=\"%s.%s\"";

    @Autowired
    private UWServiceInterface uwService;

    @Autowired
    private TapService tapService;

    /**
     * Perform a TAP request immediately
     * 
     * @param request
     *            the http request - this contains the params for the TAP request
     * @param response
     *            the http response
     */
    @RequestMapping(value = { "/tap/sync" }, method = { RequestMethod.GET, RequestMethod.POST })
    public void syncTapRequest(HttpServletRequest request, HttpServletResponse response)
    {
        logger.info("Hit the controller for the '/tap/sync' url mapping - servicing {} request.", request.getMethod());
        checkReady();

        request = new TimedRequest(request, TapService.SUBMITTED_MODE_SYNC, tapService.trustAuthHeader(request));
        Map<String, String> paramsMap = UWSToolBox.getParamsMap(request);

        try
        {
            String requestParam = paramsMap.get("request");
            if ("doQuery".equals(requestParam))
            {
                String formatParam = paramsMap.get("format");
                OutputFormat outputFormat = tapService.getFormat(formatParam);
                if (outputFormat != null)
                {
                    // content type needs to be set before writing to the response
                    String contentType = outputFormat.getDefaultContentType();
                    // if one of the votable formats was asked for then use it in the response
                    if (StringUtils.isNotBlank(formatParam) && outputFormat == OutputFormat.VOTABLE
                            && !OutputFormat.VOTABLE.toString().equalsIgnoreCase(formatParam))
                    {
                        contentType = formatParam;
                    }
                    response.setContentType(contentType);
                    // set this header as a hint to the browser for the filename and extension
                    response.setHeader(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_HEADER_FORMAT, "result",
                            outputFormat.getFileExtension()));
                }

                PrintWriter writer = response.getWriter();
                if (!tapService.processQuery(writer, paramsMap))
                {
                    // job.processQuery returns false is error occured and writes error to the writer
                    // errors are returned in votable format. - May be too late to change the content type...
                    response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
                    return;
                }
            }
            else if ("getCapabilities".equals(requestParam))
            {
                if (StringUtils.isNotBlank(request.getHeader(VoKeys.VO_HEADER_CAPABILITIES_URL)))
                {
                    response.setHeader(VoKeys.VO_HEADER_CAPABILITIES_URL,
                            request.getHeader(VoKeys.VO_HEADER_CAPABILITIES_URL));
                }
                response.sendRedirect("capabilities");
                return;
            }
            else if (requestParam == null)
            {
                response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
                PrintWriter writer = response.getWriter();
                tapService.reportTapError(writer, "Missing REQUEST paramater.");
                return;
            }
            else
            {
                response.setContentType(OutputFormat.VOTABLE.getDefaultContentType());
                PrintWriter writer = response.getWriter();
                tapService.reportTapError(writer, "Unsupported request: " + requestParam);
                return;
            }
        }
        catch (IOException e)
        {
            logger.error("Failed to process sync request: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (DataAccessException e)
        {
            logger.error("Unable to run query: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (ConfigurationException e)
        {
            logger.error("Configuration problem: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (Exception e)
        {
            logger.error("Failed to process sync request: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Async endpoint as per the TAP standard.
     * 
     * These requests are passed off to the UWS library that manages async jobs. Tap joblist :
     * http://example.com/tap/async Post to joblist to create new job - params as per sync request + phase=RUN to run
     * immediately Tap job detaild : http://example.com/tap/async/42
     * 
     * Other resources available: http://example.com/tap/async/42/phase http://example.com/tap/async/42/quote
     * http://example.com/tap/async/42/executionduration http://example.com/tap/async/42/destruction
     * http://example.com/tap/async/42/error http://example.com/tap/async/42/parameters
     * http://example.com/tap/async/42/results http://example.com/tap/async/42/owner
     * 
     * @param request
     *            http request containing query parameters
     * @param response
     *            should return a 303 seeOther response to the created job
     * @throws IOException
     *             if problems occur writing the request
     */
    private void asyncTapRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("Hit the controller for the servicing request. Method: " + request.getMethod() + ", url mapping: "
                + request.getRequestURI() + ", parameter:" + request.getQueryString());
        checkReady();
        try
        {
            // Forward the request to the uws [required]:
            uwService.executeRequest(
                    new TimedRequest(request, TapService.SUBMITTED_MODE_ASYNC, tapService.trustAuthHeader(request)),
                    response);
        }
        catch (UWSException uwsEx)
        {
            // a lot of UWS Exceptions are swallowed within the uws and it writes the error to the response directly
            // Display properly the caught UWSException:
            response.sendError(uwsEx.getHttpErrorCode(), uwsEx.getMessage());
        }
    }

    /**
     * Async endpoint as per the TAP standard. For create job
     * 
     * 
     * @param request
     *            http request containing query parameters
     * @param response
     *            should return a 303 seeOther response to the created job
     * @throws IOException
     *             if problems occur writing the request
     * 
     * @see asyncTapRequest
     */
    @RequestMapping(value = "/tap/async/**", method = { RequestMethod.POST })
    public void asyncJobCreateRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        asyncTapRequest(request, response);
    }

    /**
     * Async endpoint as per the TAP standard. For GET & DELETE method with jobID
     * 
     * 
     * @param request
     *            http request containing query parameters
     * @param response
     *            should return a 303 seeOther response to the created job
     * @throws IOException
     *             if problems occur writing the request
     * 
     * @see asyncTapRequest
     */
    @RequestMapping(value = "/tap/async/{jobId}/**", method = { RequestMethod.GET, RequestMethod.DELETE })
    public void asyncJobRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        asyncTapRequest(request, response);
    }

    /**
     * Async endpoint as per the TAP standard. For admin to GET jobID list
     *
     * 
     * @param request
     *            http request containing query parameters
     * @param response
     *            should return a 303 seeOther response to the created job
     * @throws IOException
     *             if problems occur writing the request
     * 
     * @see asyncTapRequest
     */
    @RequestMapping(value = "/tap/async", method = { RequestMethod.GET })
    public void asyncJobListRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        checkReady();
        if (tapService.isTrustedUserId(request))
        {
            asyncTapRequest(request, response);
        }
        else
        {
            logger.debug("UNAUTHORIZED to process async request: " + request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Request that the TAP metadata is refreshed from the database.
     * 
     * @param request
     *            the http request
     * @param response
     *            the http response
     * @return A result message to be displayed to the user.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    @RequestMapping(value = { "/tap/reset" }, method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public MessageDTO resetTap(HttpServletRequest request, HttpServletResponse response) throws ConfigurationException
    {
        logger.info("Hit the controller for the '/tap/reset' url mapping - servicing {} request.", request.getMethod());

        tapService.refresh();
        checkReady();

        return new MessageDTO(MessageCode.SUCCESS, "Table Access Protocol metadata reset completed.");
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
            if (tapService == null || !tapService.isReady() || uwService == null || !uwService.isReady())
            {
                throw new ConfigurationException("TapController is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * 
     * Wraps an HttpServletRequest with additional parameter information - start time
     * 
     * Copyright 2014, CSIRO Australia All rights reserved.
     * 
     */
    public static class TimedRequest extends HttpServletRequestWrapper
    {
        /** Parameters container replacement */
        private Map<String, String[]> parameters;

        /**
         * Create a new request wrapper that will add submission time to the original request parameters.
         * 
         * @param request
         *            the request to be timed
         * @param mode
         *            processing mode: "sync" or "async"
         * @param trustAuthHeader
         *            true if the request has been received from a trusted IP address
         */
        public TimedRequest(HttpServletRequest request, String mode, boolean trustAuthHeader)
        {
            super(request);
            parameters = new TreeMap<String, String[]>();
            parameters.putAll(request.getParameterMap());
            parameters.put(VoKeys.KEY_REQUESTER_IP_ADDRESS, new String[] { request.getRemoteAddr() });
            parameters.put(VoKeys.SUBMITTED_TIME, new String[] { ZonedDateTime.now(ZoneId.of("UTC")).toString() });
            parameters.put(VoKeys.SUBMITTED_MODE, new String[] { mode });
            Utils.addParamsAsStringArray(parameters, Utils.getAuthParams(request, trustAuthHeader));
        }

        @Override
        public String getParameter(String name)
        {
            String[] strings = parameters.get(name);
            if (strings != null)
            {
                return strings[0];
            }
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap()
        {
            return Collections.unmodifiableMap(parameters);
        }

        @Override
        public Enumeration<String> getParameterNames()
        {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public String[] getParameterValues(String name)
        {
            return parameters.get(name);
        }
    }

}
