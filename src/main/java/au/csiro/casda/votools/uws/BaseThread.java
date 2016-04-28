package au.csiro.casda.votools.uws;

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


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.dao.DataAccessException;

import uws.UWSException;
import uws.job.ErrorType;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;
import au.csiro.casda.votools.result.OutputFormat;

/**
 * Base UWS Thread that will handle the file level operations of saving and deleting the results while allowing
 * subclasses to implement the query logic.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public abstract class BaseThread extends JobThread
{

    /**
     * Constructor
     * 
     * @param uwsJob
     *            uws job
     * @throws UWSException
     *             if there is a problem with the job
     */
    public BaseThread(UWSJob uwsJob) throws UWSException
    {
        super(uwsJob);
    }

    /**
     * This is where the specific thread implementation performs it's work ie (query) Override this method in job
     * subclasses to perform different types of job or queries.
     * 
     * @param writer
     *            This is where the threads output should be written
     * @return true if the query was successful, false if an error was generated.
     * @throws UWSException
     *             If a UWS problem occurs
     * @throws InterruptedException
     *             If the thread is interrupted.
     * @throws DataAccessException
     *             If there is a problem accessing the database
     * @throws IOException
     *             If there is a problem using the writer
     */
    public abstract boolean processQuery(Writer writer) throws UWSException, InterruptedException, DataAccessException,
            IOException;

    @Override
    protected void jobWork() throws UWSException, InterruptedException
    {
        try
        {
            Result result = this.createResult();
            // set the mime type if we have it / must set before writing results as mimetype used in filename
            if (this.getOutputFormat() != null)
            {
                result.setMimeType(this.getOutputFormat().getDefaultContentType());
            }
            else
            {
                result.setMimeType(OutputFormat.VOTABLE.getDefaultContentType());
            }
            try (OutputStream outStream = this.getResultOutput(result))
            {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));
                this.processQuery(writer);
                writer.flush();
                outStream.close();
            }
            this.publishResult(result);

        }
        catch (IOException e)
        {
            // If there is an error, encapsulate it in an UWSException so that an error summary can be published:
            throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e, "Impossible to write the result file !",
                    ErrorType.TRANSIENT);
        }
    }

    /**
     * @return the OutputFormat that this thread is using.
     */
    public abstract OutputFormat getOutputFormat();
}