package au.csiro.casda.votools.availability;

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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.jaxb.availability.Availability;

/**
 * Tests the VO TAP Availability Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class AvailabilityControllerTest
{
    @Mock
    private AvailabilityService mockService;

    @InjectMocks
    private AvailabilityController availabilityController;

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
        this.mockMvc = MockMvcBuilders.standaloneSetup(availabilityController).build();
    }

    /**
     * Basic test via endpoint
     * 
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAvailabilityViaEndpoint() throws Exception
    {
        Availability available = new Availability();
        available.setAvailable(true);
        Mockito.when(mockService.getAvailability(VoServiceType.tap)).thenReturn(available);
        this.mockMvc.perform(get("/tap/availability")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/availability/available").booleanValue(Boolean.TRUE));
    }

    /**
     * Test availability with Vo type we don't recognise
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAvailabilityWithInvalidVoType() throws Exception
    {
        this.mockMvc.perform(get("/xyz/availability")).andExpect(status().is4xxClientError());
    }

}
