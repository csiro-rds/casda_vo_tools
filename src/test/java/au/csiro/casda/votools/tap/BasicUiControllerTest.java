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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.votools.result.OutputFormat;

/**
 * Tests the VO TAP Basic UI Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class BasicUiControllerTest
{

    @InjectMocks
    private BasicUiController controller;

    private MockMvc mockMvc;

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
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Tests a GET /tap.html
     * 
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testEndpoint() throws Exception
    {

        List<Object> outputList =
                Arrays.asList(OutputFormat.values()).stream().flatMap(output -> output.getIdentifiers().stream())
                        .collect(Collectors.toList());

        this.mockMvc.perform(get("/tap.html")).andExpect(status().isOk()).andExpect(view().name("tap/show"))
                .andExpect(model().attribute("outputFormats", (outputList)));
    }

}
