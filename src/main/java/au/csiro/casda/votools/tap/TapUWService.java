package au.csiro.casda.votools.tap;

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


import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.uws.UWSLogger;
import au.csiro.casda.votools.uws.UWServiceInterface;
import uws.UWSException;
import uws.job.ErrorSummary;
import uws.job.JobList;
import uws.job.Result;
import uws.job.UWSJob;
import uws.job.manager.QueuedExecutionManager;
import uws.job.serializer.UWSSerializer;
import uws.job.serializer.XMLSerializer;
import uws.job.user.JobOwner;
import uws.service.UWS;
import uws.service.UWSService;
import uws.service.UWSUrl;
import uws.service.backup.DefaultUWSBackupManager;
import uws.service.backup.UWSBackupManager;
import uws.service.file.LocalUWSFileManager;

/**
 * TAP implementation of UWS service configured to manage tap async queries via the TapThread class.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Service
@Scope("singleton")
public class TapUWService extends Configurable implements UWServiceInterface
{

    private static Logger logger = LoggerFactory.getLogger(TapUWService.class);

    private UWSService uws;

    private TapService tapService;

    private static final String DEFAULT_ASYNC_BASE_URL = "/tap";
    private static final String DEFAULT_RESULTS_DIR = "temp";
    private static final String DEFAULT_ASYNC_DESCRIPTION = "UWS for CASDA";
    private static final String DEFAULT_ASYNC_JOB_LIST_NAME = "async";
    private static final int DEFAULT_MAX_RUNNING_JOBS = 4;

    private boolean ready;

    private Configuration config;

    /**
     * Constructor
     * 
     * @param tapService
     *            the TAP service
     * @param configRegistry
     *            configuration registry holding current configuration and properties values
     * @throws UWSException
     *             if there is a problem setting up the UWS (universal worker service)
     * @throws IOException
     *             if there is a file system problem
     * @throws ServletException
     *             if there is an http problem
     * @throws ConfigurationException
     *             if there is a configuration problem
     */
    @Autowired
    public TapUWService(TapService tapService, ConfigurationRegistry configRegistry) throws UWSException, IOException,
            ServletException, ConfigurationException
    {
        this.tapService = tapService;
        configRegistry.register(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        this.config = config;
        ready = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        if (config != null && !ready && tapService != null && tapService.isReady())
        {
            try
            {
                setupNewUws(config.getEndPoint("TAP"));
            }
            catch (Exception e)
            {
                throw new ConfigurationException(e);
            }
            ready = true;
        }
        return ready;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    private void setupNewUws(EndPoint endPoint) throws UWSException
    {
        String resultsDirName = endPoint.get("results.dir", DEFAULT_RESULTS_DIR);
        File resultsDir = new File(resultsDirName);
        String asyncBaseUrl = endPoint.get("async.base.url", DEFAULT_ASYNC_BASE_URL);
        String asyncDescription = endPoint.get("async.description", DEFAULT_ASYNC_DESCRIPTION);
        String asyncJobListName = endPoint.get("async.job.list.name", DEFAULT_ASYNC_JOB_LIST_NAME);
        int maxRunningJobs = endPoint.getInt("max.running.jobs", DEFAULT_MAX_RUNNING_JOBS);

        if (uws == null) // create new service, changing configuration parameters requires restart
        {
            try
            {
                logger.info("Configuring UWS to write result files to " + resultsDir.getCanonicalPath()
                        + " This can be changed using the tap.results.dir setting.");
            }
            catch (IOException e1)
            {
                String message = "Failed to resolve reference to results.dir of " + resultsDirName;
                logger.error(message, e1);
                throw new UWSException(message);
            }
            LocalUWSFileManager fileManager;
            try
            {
                fileManager = new LocalUWSFileManager(resultsDir, false, false);
            }
            catch (UWSException e)
            {
                logger.error("Failure trying to find or create " + resultsDir.getAbsolutePath());
                throw e;
            }
            
            uws = new UWSService(new TapUWSFactory(tapService), fileManager,
                     new UWSLogger(), new TapUWSUrl(asyncBaseUrl));
            uws.setDescription(asyncDescription);
            JobList jobList = new JobList(asyncJobListName, new QueuedExecutionManager(uws.getLogger(), maxRunningJobs));
            uws.addJobList(jobList);
            uws.addSerializer(new TextXmlSerializer());
            // Taplint prefers this mime type to be default
            uws.setDefaultSerializer("text/xml");
            uws.setBackupManager(new DefaultUWSBackupManager(uws));
            // try and restore previous jobs
            uws.getBackupManager().restoreAll();
        }
    }

    @Override
    public boolean executeRequest(HttpServletRequest request, HttpServletResponse response) throws UWSException,
            IOException
    {
        return this.uws.executeRequest(request, response);
    }

    /**
     * @return the UWS Backup manager - so can manually trigger backups.
     */
    public final UWSBackupManager getBackupManager()
    {
        return uws.getBackupManager();
    }

    /**
     * @param name
     *            the joblist name to get
     * @return the joblist
     */
    public final JobList getJobList(String name)
    {
        return uws.getJobList(name);
    }

    /**
     * This textXmlSerializer wraps the normal UWS xml serializer but uses the text/xml mime type preferred by taplint.
     * 
     * Copyright 2014, CSIRO Australia All rights reserved.
     * 
     */
    private class TextXmlSerializer extends UWSSerializer
    {

        private static final long serialVersionUID = 1L;
        private XMLSerializer xMLSerializer = new XMLSerializer();

        @Override
        public final String getMimeType()
        {
            return "text/xml";
        }

        @Override
        public String getUWS(UWS jobLists, JobOwner jobOwner) throws UWSException
        {
            return xMLSerializer.getUWS(jobLists, jobOwner);
        }

        @Override
        public String getJobList(JobList uwsJobs, JobOwner jobOwner, boolean b) throws UWSException
        {
            return xMLSerializer.getJobList(uwsJobs, jobOwner, b);
        }

        @Override
        public String getJob(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getJob(uwsJob, b);
        }

        @Override
        public String getJobRef(UWSJob uwsJob, UWSUrl uwsUrl) throws UWSException
        {
            return xMLSerializer.getJobRef(uwsJob, uwsUrl);
        }

        @Override
        public String getJobID(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getJobID(uwsJob, b);
        }

        @Override
        public String getRunID(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getRunID(uwsJob, b);
        }

        @Override
        public String getOwnerID(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getOwnerID(uwsJob, b);
        }

        @Override
        public String getPhase(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getPhase(uwsJob, b);
        }

        @Override
        public String getQuote(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getQuote(uwsJob, b);
        }

        @Override
        public String getStartTime(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getStartTime(uwsJob, b);
        }

        @Override
        public String getEndTime(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getEndTime(uwsJob, b);
        }

        @Override
        public String getExecutionDuration(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getExecutionDuration(uwsJob, b);
        }

        @Override
        public String getDestructionTime(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getDestructionTime(uwsJob, b);
        }

        @Override
        public String getErrorSummary(ErrorSummary errorSummary, boolean b) throws UWSException
        {
            return xMLSerializer.getErrorSummary(errorSummary, b);
        }

        @Override
        public String getResults(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getResults(uwsJob, b);
        }

        @Override
        public String getResult(Result result, boolean b) throws UWSException
        {
            return xMLSerializer.getResult(result, b);
        }

        @Override
        public String getAdditionalParameters(UWSJob uwsJob, boolean b) throws UWSException
        {
            return xMLSerializer.getAdditionalParameters(uwsJob, b);
        }

        @Override
        public String getAdditionalParameter(String s, Object o, boolean b) throws UWSException
        {
            return xMLSerializer.getAdditionalParameter(s, o, b);
        }
    }

}
