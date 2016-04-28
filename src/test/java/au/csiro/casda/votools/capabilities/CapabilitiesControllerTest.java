package au.csiro.casda.votools.capabilities;

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


import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.jaxb.capabilities.Capabilities;
import au.csiro.casda.votools.jaxb.conesearch.ConeSearch;
import au.csiro.casda.votools.jaxb.tapregext.Language;
import au.csiro.casda.votools.jaxb.tapregext.TableAccess;
import au.csiro.casda.votools.jaxb.tapregext.Version;
import au.csiro.casda.votools.jaxb.vodataservice.ParamHTTP;
import au.csiro.casda.votools.jaxb.voresource.AccessURL;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * Tests the VO TAP Capabilities Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CapabilitiesControllerTest
{

    @Mock
    private CapabilitiesService mockService;

    @InjectMocks
    private CapabilitiesController capabilitiesController;

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
        this.mockMvc = MockMvcBuilders.standaloneSetup(capabilitiesController).build();

        Capabilities caps = new Capabilities();
        TableAccess ta = new TableAccess();
        ta.setStandardID("someStdId");
        ta.setDescription("bob");
        AccessURL au = new AccessURL();
        au.setValue("http://some.url");
        au.setUse("base");
        ParamHTTP param = new ParamHTTP();
        param.getAccessURL().add(au);
        param.setRole("std");
        ta.getInterface().add(param);
        Language lan = new Language();
        lan.setName("ADQL");
        Version ver = new Version();
        ver.setValue("2.0");
        lan.getVersion().add(ver);
        lan.setDescription("ADQL 2.0");
        ta.getLanguage().add(lan);
        caps.getCapability().add(ta);
        when(mockService.getCapabilities(eq(VoServiceType.tap), anyString())).thenReturn(caps);

        caps = new Capabilities();
        ConeSearch cs = new ConeSearch();
        cs.setStandardID("someStdConeId");
        cs.setDescription("bob_cone");
        cs.setMaxSR(10.2f);
        caps.getCapability().add(cs);
        when(mockService.getCapabilities(eq(VoServiceType.scs), anyString())).thenReturn(caps);
        doReturn(true).when(mockService).isReady();
    }

    /**
     * Basic test
     * 
     * @throws Exception
     *             Problem performing get request
     */
    @Test
    public void testGetCapabilities() throws Exception
    {
        this.mockMvc.perform(get("/tap/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/capabilities/capability/description").string("bob"))
                .andExpect(xpath("/capabilities/capability/@standardID").string("someStdId"))
                .andExpect(xpath("/capabilities/capability/interface/accessURL").string("http://some.url"))
                .andExpect(xpath("/capabilities/capability/language/version").string("2.0"));

        verify(mockService).getCapabilities(eq(VoServiceType.tap), eq(null));
    }

    @Test
    public void testGetCapabilitiesProxyUrlInHeader() throws Exception
    {
        this.mockMvc.perform(
                get("/tap/capabilities").header(VoKeys.VO_HEADER_CAPABILITIES_URL, "http://my.proxy.url"))
                .andExpect(status().isOk());

        verify(mockService).getCapabilities(eq(VoServiceType.tap), eq("http://my.proxy.url"));
    }

    /**
     * Test capabilities with Vo type we don't recognise
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithInvalidVoType() throws Exception
    {
        this.mockMvc.perform(get("/xyz/capabilities")).andExpect(status().is4xxClientError());
    }

    /**
     * Test capabilities with cone Search with Vo type
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithConeSearchVoType() throws Exception
    {
        this.mockMvc.perform(get("/scs/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/capabilities/capability/maxSR").string("10.2"));
    }

}
