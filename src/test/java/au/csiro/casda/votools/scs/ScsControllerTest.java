package au.csiro.casda.votools.scs;

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


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.votools.utils.VoKeys;

/**
 * Tests the VO TAP main endpoint Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class ScsControllerTest
{
    @Mock
    private ScsService mockService;

    @InjectMocks
    private ScsController controller;

    private MockMvc mockMvc;

    /**
     * Set up the controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        doReturn(true).when(mockService).isReady();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Basic test of the SCS endpoint.
     * 
     * @throws Exception
     *             from performing get request
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSCSEndpoint() throws Exception
    {
        String queryString = "ra=1&dec=2&sr=1";

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(true).when(mockService).processQuery(any(Writer.class), mapCaptor.capture());

        // check content type set to a default
        this.mockMvc.perform(get("/scs/obscore?" + queryString)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/xml;content=x-votable"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));

        verify(mockService).trustAuthHeader(any(HttpServletRequest.class));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getValue().get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getValue().get(VoKeys.USER_PROJECTS));
        assertEquals("obscore", mapCaptor.getValue().get(VoKeys.CATALOG));
        assertEquals(queryString, mapCaptor.getValue().get(VoKeys.PARAM_QUERY_STRING));
    }

    /**
     * Test of the sync endpoint. Checks the error handling returns correct content type
     * 
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testSyncViaEndpointErrorMsg() throws Exception
    {
        String queryString = "";

        this.mockMvc.perform(get("/scs/obscore?" + queryString)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/xml;content=x-votable"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSCSEndpointProxiedRequestReadsUserInfo() throws Exception
    {
        String queryString = "ra=1&dec=2&sr=1";
        String userId = "smi12j";
        String loginSystem = "OPAL";
        String userProjects = "ABC123,ABC111";

        doReturn(true).when(mockService).trustAuthHeader(any(HttpServletRequest.class));

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(true).when(mockService).processQuery(any(Writer.class), mapCaptor.capture());

        // check content type set to a default
        this.mockMvc
                .perform(get("/scs/obscore?" + queryString).header(VoKeys.VO_AUTH_HEADER_USER_ID, userId)
                        .header(VoKeys.VO_AUTH_HEADER_LOGIN_SYSTEM, loginSystem)
                        .header(VoKeys.VO_AUTH_HEADER_USER_PROJECTS, userProjects))
                .andExpect(status().isOk()).andDo(print()).andExpect(content().contentType("text/xml;content=x-votable"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));

        verify(mockService).trustAuthHeader(any(HttpServletRequest.class));
        assertEquals(userId, mapCaptor.getValue().get(VoKeys.USER_ID));
        assertEquals(userProjects, mapCaptor.getValue().get(VoKeys.USER_PROJECTS));
        assertEquals(loginSystem, mapCaptor.getValue().get(VoKeys.LOGIN_SYSTEM));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSCSEndpointNotproxiedRequestIgnoresUserInfo() throws Exception
    {
        String queryString = "ra=1&dec=2&sr=1";
        String userId = "smi12j";
        String loginSystem = "OPAL";
        String userProjects = "ABC123,ABC111";

        doReturn(false).when(mockService).trustAuthHeader(any(HttpServletRequest.class));

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(true).when(mockService).processQuery(any(Writer.class), mapCaptor.capture());

        // check content type set to a default
        this.mockMvc
                .perform(get("/scs/obscore?" + queryString).header(VoKeys.VO_AUTH_HEADER_USER_ID, userId)
                        .header(VoKeys.VO_AUTH_HEADER_LOGIN_SYSTEM, loginSystem)
                        .header(VoKeys.VO_AUTH_HEADER_USER_PROJECTS, userProjects))
                .andExpect(status().isOk()).andDo(print()).andExpect(content().contentType("text/xml;content=x-votable"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));

        verify(mockService).trustAuthHeader(any(HttpServletRequest.class));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getValue().get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getValue().get(VoKeys.USER_PROJECTS));
        assertEquals("", mapCaptor.getValue().get(VoKeys.LOGIN_SYSTEM));
    }

}
