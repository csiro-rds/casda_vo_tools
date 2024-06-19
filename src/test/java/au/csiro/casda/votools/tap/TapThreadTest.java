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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import au.csiro.BaseTest;
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
public class TapThreadTest extends BaseTest
{

    @Mock
    private TapService mockService;

    /**
     * Set up the mocks before each test.
     *
     * @throws Exception
     *             any exception thrown during set up
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        Mockito.when(mockService.processQuery((Writer) any(), any())).thenReturn(false);
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
        Mockito.verify(mockService, Mockito.atLeastOnce()).processQuery((Writer) any(), any());

    }
}
