package au.csiro.casda.votools;

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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.scs.ScsService;
import au.csiro.casda.votools.tap.TapService;

/**
 * Tests the VO Tools UI Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class VoToolsUiControllerTest
{
    @Mock
    private TapService tapService;

    @Mock
    private ScsService scsService;
    
    @Mock
    private ConfigurationRegistry configReg;

    @Mock
    private Configuration config;

    private VoToolsUiController voToolsUiController;

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
        config = new Configuration();
        voToolsUiController = new VoToolsUiController(tapService, scsService, configReg);
        
        config.putDefault("log.timezone", "UTC");
        config.putDefault("application.message", "some message");
        when(tapService.getConfig()).thenReturn(config);
    }

    @Test
    public void testHome() throws Exception
    {
        when(tapService.isReady()).thenReturn(true);
        when(scsService.isReady()).thenReturn(true);
        
        Model model = new ExtendedModelMap();
        String result = voToolsUiController.home(model);

        assertEquals("home", result);
        assertEquals("some message", model.asMap().get("message"));
    }

    @Test
    public void testRefreshConfig() throws Exception
    {
        when(tapService.isReady()).thenReturn(true);
        when(scsService.isReady()).thenReturn(true);
        voToolsUiController.refreshConfig();
        verify(tapService, times(1)).refresh();
        verify(scsService, times(1)).refresh();
    }

    @Test
    public void testRefreshConfigTapServiceFails() throws Exception
    {
        try
        {
            when(tapService.isReady()).thenReturn(false);
            when(scsService.isReady()).thenReturn(true);
            voToolsUiController.refreshConfig();
            fail("Should throw an exception if tap service is not ready");
        }
        catch (Exception e)
        {
            assertEquals(e.getCause().getClass(), ConfigurationException.class);
        }

    }

    @Test
    public void testRefreshConfigScsServiceFails() throws Exception
    {
        try
        {
            when(tapService.isReady()).thenReturn(true);
            when(scsService.isReady()).thenReturn(false);
            voToolsUiController.refreshConfig();
            fail("Should throw an exception if scs service is not ready");
        }
        catch (Exception e)
        {
            assertEquals(e.getCause().getClass(), ConfigurationException.class);
        }
    }

}
