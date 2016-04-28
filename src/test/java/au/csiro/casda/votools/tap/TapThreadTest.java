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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.utils.VoKeys;
import uws.UWSException;
import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;

/**
 * Unit tests of the TapThread functions.
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class TapThreadTest
{

    @Mock
    private TapService mockService;

    /**
     * Set up the mocks before each test.
     *
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockService.processQuery((Writer) anyObject(), anyObject())).thenReturn(false);
    }

    @Test
    public void testJobWork() throws UWSException, IOException, InterruptedException, ConfigurationException
    {
        Map<String, Object> map = new HashMap<>();
        map.put(TapService.STR_KEY_FORMAT, "XXX");
        map.put(VoKeys.SUBMITTED_TIME, "1");
        UWSParameters params = new UWSParameters(map);
        UWSJob job = new UWSJob(params);
        TapThread thread = new TapThread(job, mockService);
        StringWriter writer = new StringWriter();
        assertThat(thread.processQuery(writer), is(false));
        Mockito.verify(mockService, Mockito.atLeastOnce()).processQuery((Writer) anyObject(), anyObject());

    }
}
