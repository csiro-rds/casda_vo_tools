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

import uws.UWSException;
import uws.job.JobList;
import uws.service.UWSService;
import uws.service.file.LocalUWSFileManager;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.uws.UWServiceInterface;

/**
 * Dummy UWS service that uses DummyThread for testing the UWS library and tap controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class DummyUWService extends UWSService implements UWServiceInterface
{

    public DummyUWService() throws UWSException
    {
        super(new DummyUWSFactory(), new LocalUWSFileManager(new File("temp")), "/tap");

        this.setDescription("desc");
        this.addJobList(new JobList("async"));
    }

    /* (non-Javadoc)
     * @see au.csiro.casda.votools.uws.UWServiceInterface#isReady()
     */
    @Override
    public boolean isReady() throws ConfigurationException
    {
        return true;
    }

}
