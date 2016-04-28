package au.csiro.casda.votools.tap;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;

import uws.UWSException;
import uws.UWSToolBox;
import uws.job.ErrorSummary;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;
import uws.job.user.JobOwner;
import uws.service.UWSFactory;

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
 * Factory class used to provide UWS service with instances of UWSJobs and JobThreads for Tap
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class TapUWSFactory implements UWSFactory
{

    private final TapService tapService;

    /**
     * Constructor
     * 
     * @param tapService
     *            the TAP Service
     */
    public TapUWSFactory(TapService tapService)
    {
        this.tapService = tapService;
    }

    @Override
    public UWSJob createJob(HttpServletRequest request, JobOwner jobOwner) throws UWSException
    {
        UWSJob job = new UWSJob(this.createUWSParameters(request));
        // set job destruction date - when job and results will be deleted
        Date destructionDate = new Date();
        destructionDate =
                DateUtils.addSeconds(destructionDate, tapService.getRetentionPeriodDefault());
        job.setDestructionTime(destructionDate);
        // set maximum execution time
        job.setExecutionDuration(tapService.getExecutionDurationDefault());
        return job;
    }

    @Override
    public UWSJob createJob(String jobID, JobOwner jobOwner, UWSParameters entries, long quote, long startTime,
            long endTime, List<Result> results, ErrorSummary errorSummary) throws UWSException
    {
        return new UWSJob(jobID, jobOwner, entries, quote, startTime, endTime, results, errorSummary);
    }

    @Override
    public JobThread createJobThread(UWSJob uwsJob) throws UWSException
    {
        return new TapThread(uwsJob, tapService);
    }

    @Override
    public UWSParameters createUWSParameters(HttpServletRequest request) throws UWSException
    {
        // make sure parameters are case insensitive as per tap spec
        Map<String, String> caseInsensitiveParams = UWSToolBox.getParamsMap(request);
        Map<String, Object> params = new HashMap<>();
        params.putAll(caseInsensitiveParams);
        return this.createUWSParameters(params);
    }

    @Override
    public UWSParameters createUWSParameters(Map<String, Object> stringObjectMap) throws UWSException
    {
        return new UWSParameters(stringObjectMap);
    }
}
