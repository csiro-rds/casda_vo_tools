package au.csiro.casda.votools.tap;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uws.UWSException;
import uws.job.UWSJob;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.VoKeys;
import au.csiro.casda.votools.uws.BaseThread;

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
 * A Universal Worker Service job to process a Table Access Protocol query and either store the output for later
 * retrieval or send it back to the caller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class TapThread extends BaseThread
{
    private static Logger logger = LoggerFactory.getLogger(TapThread.class);

    private final TapService tapService;
    private OutputFormat outputFormat;

    /**
     * @param uwsJob
     *            Parameters passed through from the request.
     * @param tapService
     *            The service to use to execute the tap query.
     * @throws UWSException
     *             If a problem occurs in the uws
     */
    public TapThread(UWSJob uwsJob, TapService tapService) throws UWSException
    {
        super(uwsJob);
        this.tapService = tapService;
        String formatParam = (String) uwsJob.getParameter("format");
        outputFormat = tapService.getFormat(formatParam);
    }

    /**
     * Process an already validated TAP query and write the result to the supplied writer. If an error is encountered
     * the error will be written in VOTABLE format to the writer.
     * 
     * @param writer
     *            The destination for the query output.
     * @return true if the query was successful, false if an error occurred
     * @throws InterruptedException
     *             if the job is interrupted
     * @throws IOException
     *             if an error occurs using writer
     */
    public boolean processQuery(Writer writer) throws InterruptedException, IOException
    {
        String query = (String) this.getJob().getParameter("query");
        String maxRecValue = (String) this.getJob().getParameter("maxrec");
        String format = (String) this.getJob().getParameter("format");
        String startTime = (String) this.getJob().getParameter(VoKeys.SUBMITTED_TIME);
        String mode = (String) this.getJob().getParameter(VoKeys.SUBMITTED_MODE);
        String requesterIpAddress = (String) this.getJob().getParameter(VoKeys.KEY_REQUESTER_IP_ADDRESS);
        String userId = (String) this.getJob().getParameter(VoKeys.USER_ID);
        String userProjects = (String) this.getJob().getParameter(VoKeys.USER_PROJECTS);
        
        // Parameters to be validated
        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.STR_KEY_ADQL_QUERY, query);
        params.put(VoKeys.STR_KEY_MAXREC, maxRecValue);
        params.put(TapService.STR_KEY_FORMAT, format);
        params.put(TapService.STR_KEY_VERSION, (String) this.getJob().getParameter("version"));
        params.put(TapService.STR_KEY_LANG, (String) this.getJob().getParameter("lang"));
        params.put(VoKeys.SUBMITTED_TIME, startTime);
        params.put(VoKeys.SUBMITTED_MODE, mode);
        params.put(VoKeys.KEY_REQUESTER_IP_ADDRESS, requesterIpAddress);
        params.put(VoKeys.USER_ID, userId);
        params.put(VoKeys.USER_PROJECTS, userProjects);
        
        try
        {
            return this.tapService.processQuery(writer, params);
        }
        catch (ConfigurationException e) // not going to happen because by this time
        {                                // the configuration has been used and proven to be OK
           logger.error("Unexpected exception",e);
           return false ;
        }
    }

    @Override
    public OutputFormat getOutputFormat()
    {
        return outputFormat;
    }

}
