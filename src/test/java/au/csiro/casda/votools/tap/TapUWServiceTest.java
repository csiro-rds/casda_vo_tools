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


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.config.EndPoint;
import uws.UWSException;
import uws.job.JobList;
import uws.job.UWSJob;
import uws.job.manager.QueuedExecutionManager;
import uws.job.parameters.UWSParameters;

/**
 * Tests for the TapUWSService.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class TapUWServiceTest
{
    @Autowired
    private ConfigurationRegistry configRegistry;

    @Mock
    private TapService tapService;

    /**
     * Set up basic configuration before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Mockito.when(tapService.isReady()).thenReturn(true);

        Configuration config = ConfigurationTest.getTestConfiguration();
        EndPoint tapEndPoint = config.getEndPoint("TAP");
        tapEndPoint.put("log.timezone", "UTC");

        tapEndPoint.put("results.dir", "build/tmp/");
        tapEndPoint.put("async.base.url", "tap");
        tapEndPoint.put("async.description", "desc");
        tapEndPoint.put("async.job.list.name", "async");
        tapEndPoint.put("max.running.jobs", "6");

        configRegistry.switchConfiguration(config, false);
    }

    /**
     * Test that a saved file can be restored.
     * 
     * @throws UWSException
     *             if there is a problem with the UWS service
     * @throws IOException
     *             if there is a problem reading the file
     * @throws ServletException
     *             this comes from UWS
     * @throws ConfigurationException
     *             if there is a configuration problem
     */
    @Test
    public void testSaveAndRestoreUwsFromFile() throws UWSException, IOException, ServletException,
            ConfigurationException
    {
        // create a new service - after a clean this will not find existing data so will create new
        TapUWService service = new TapUWService(tapService, configRegistry);
        service.isReady();
        JobList list = service.getJobList("async");
        // Add a new job with the current date
        Map<String, Object> params = new HashMap<String, Object>();
        Date now = new Date();
        params.put("now", now);
        // get the current job list size so it works without a clean.
        int jobListSize = list.getNbJobs();
        UWSJob uwsJob = new UWSJob(new UWSParameters(params));
        list.addNewJob(uwsJob);
        assertThat(list.getNbJobs(), is(jobListSize + 1));
        assertThat(list.getUrl().getBaseURI(), is("/tap"));
        assertThat(list.getUrl().getRequestURI(), is("/tap/async"));
        // force a backup now
        service.getBackupManager().saveAll();

        // create a new service this should load the existing list created above
        // and check that the job we added is present with correct date
        service = new TapUWService(tapService, configRegistry);
        service.isReady();
        list = service.getJobList("async");
        assertThat(list.getUrl().getBaseURI(), is("/tap"));
        assertThat(list.getUrl().getRequestURI(), is("/tap/async"));
        assertThat(list.getNbJobs(), is(jobListSize + 1));
        assertThat(list.getJob(uwsJob.getJobId()).getParameter("now"), is(now.toString()));
    }

    @Test
    public void testMaxNoRunningJobs() throws ServletException, IOException, UWSException, ConfigurationException
    {
        TapUWService service = new TapUWService(tapService, configRegistry);
        service.isReady();
        JobList list = service.getJobList("async");
        assertThat(list.getExecutionManager(), is(instanceOf(QueuedExecutionManager.class)));
        assertThat(((QueuedExecutionManager) list.getExecutionManager()).getMaxRunningJobs(), is(6));
    }
}
