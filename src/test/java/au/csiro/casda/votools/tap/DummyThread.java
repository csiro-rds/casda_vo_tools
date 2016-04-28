package au.csiro.casda.votools.tap;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import uws.UWSException;
import uws.job.ErrorType;
import uws.job.UWSJob;
import au.csiro.casda.votools.result.OutputFormat;
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
 * A Universal Worker Service job to process a test the UWS service.
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class DummyThread extends BaseThread
{

    /**
     * @param uwsJob
     *            parameters for this job
     * @throws UWSException
     *             any exception from uws
     */
    public DummyThread(UWSJob uwsJob) throws UWSException
    {
        super(uwsJob);
    }

    public boolean processQuery(Writer writer) throws UWSException, InterruptedException
    {
        String doError = (String) this.getJob().getParameter("doerror");
        if (StringUtils.equalsIgnoreCase(doError, "true"))
        {
            throw new UWSException("Bad Happened");
        }
        // wait if necessary
        String waitFor = (String) this.getJob().getParameter("waitfor");
        if (waitFor != null)
        {
            long waitSeconds = Long.parseLong(waitFor);
            TimeUnit.SECONDS.sleep(waitSeconds);
        }

        try
        {
            writer.write(this.getJob().getJobId() + " job name");
        }
        catch (IOException e)
        {
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "Unable to write query result: "
                    + e.getMessage(), ErrorType.TRANSIENT);
        }
        return true;
    }

    @Override
    public OutputFormat getOutputFormat()
    {
        return OutputFormat.TSV;
    }

}
