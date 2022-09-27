package au.csiro.casda.votools.datalink;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2018 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * 
 * Tests for Data Link Controller
 * 
 * Copyright 2018, CSIRO Australia All rights reserved.
 *
 */
public class DataLinkControllerTest
{
    @InjectMocks
    DataLinkController dataLinkController;

    @Mock
    private DataLinkService dataLinkService;

    MockMvc mockMvc;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(dataLinkController).build();

        when(dataLinkService.isReady()).thenReturn(true);
    }

    /**
     * Make sure the DataLinkController is working okay.
     * 
     * @throws Exception
     *             Exception
     */
    @Test
    public void testDataLinkRequest() throws Exception
    {
        MvcResult response = mockMvc.perform(get("/datalink/links")).andExpect(status().isOk()).andReturn();
        assertThat(response.getResponse().getContentType(), containsString("text/xml"));
    }

}
