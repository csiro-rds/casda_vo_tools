package au.csiro.casda.votools.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import au.csiro.BaseTest;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Test class for Tap Examples
 * 
 * Copyright 2017, CSIRO Australia All rights reserved.
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class TapExamplesServiceTest extends BaseTest
{
    @InjectMocks
    private TapExamplesService tapExamplesService;

    @Mock
    private ConfigurationRegistry configRegistry;

    private TapExamples tapExamples;
    private Configuration config;

    private HttpServletResponse mockedResponse;
    private MockHttpServletRequest mockedRequest;

    /*
     * Test Data
     */
    private String redirectTapExamplesUrl = "http://gaia.ari.uni-heidelberg.de/tap/examples";

    /**
     * Setup
     * 
     * @throws ConfigurationException
     *             ConfigurationException
     */
    @BeforeEach
    public void setup() throws ConfigurationException
    {

        // setup Configuration
        config = ConfigurationTest.getTestConfiguration();
        configRegistry.switchConfiguration(config, false);
        tapExamplesService.setConfiguration(config);

        tapExamples = new TapExamples();

        mockedResponse = Mockito.mock(HttpServletResponse.class);
        mockedRequest = new MockHttpServletRequest();
        mockedRequest.setServerName("casda-dev-app.csiro.au");
        mockedRequest.setSession(mockedRequest.getSession());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockedRequest));
    }

    private Set<Map<String, String>> getMockExamples()
    {
        Set<Map<String, String>> examples = new HashSet<Map<String, String>>();
        HashMap<String, String> map = new HashMap<>();
        map.put(TapExamplesService.ExampleKeys.NAME.getKey(), "Test Tap Example");
        map.put(TapExamplesService.ExampleKeys.DESCRIPTION.getKey(), "This is a test tap example");
        map.put(TapExamplesService.ExampleKeys.QUERY.getKey(),
                "SELECT TOP 1000 table_name, column_name, description FROM TAP_SCHEMA.columns");
        map.put(TapExamplesService.ExampleKeys.TABLES.getKey(), "TAP_SCHEMA.columns");
        examples.add(map);
        return examples;
    }

    /**
     * Tests redirect to another tap /examples page works.
     * 
     * @throws Exception
     *             Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExamplesFromConfiguration() throws Exception
    {
        config.getEndPoint("TAP").setExamples(getMockExamples());
        assertTrue(tapExamplesService.isReady());
        ModelAndView model = tapExamplesService.buildResponse(mockedResponse);
        List<TapExample> examples = (List<TapExample>) model.getModel().get("examples");

        assertNotNull(examples);
        
        TapExample example = examples.get(0);
        assertEquals(example.getName(), "Test Tap Example");
        assertEquals(example.getDescription(), "This is a test tap example");
        assertEquals(example.getQuery(),
                "SELECT TOP 1000 table_name, column_name, description FROM TAP_SCHEMA.columns");
        assertEquals(example.getTables().size(), 1);
        assertEquals(example.getTables().stream().findFirst().get(), "TAP_SCHEMA.columns");
        
        assertEquals(examples.size(), 1);
    }

    @Test
    public void testLoadExamplesFromXML()
    {
        tapExamples.loadFromXmlConfig(new File("src/test/resources/testdata/tap_examples.xml"));

        // Verify examples loaded from XML correctly
        assertNotNull(tapExamples.getTapExamples());
        
        // first example data check
        TapExample ex1 = tapExamples.getTapExamples().get(0);
        assertEquals(ex1.getName(), "Full Table Example");
        assertEquals(ex1.getQuery(),
                "SELECT TOP 1000 * FROM TAP_SCHEMA.columns");
        assertEquals(ex1.getTables().size(), 1);
        assertEquals(ex1.getTables().stream().findFirst().get(), "TAP_SCHEMA.columns");
        
        // Second example data check
        TapExample ex2 = tapExamples.getTapExamples().get(1);
        assertEquals(ex2.getName(), "Full Table Example 2");
        assertEquals(ex2.getDescription(), "Sample Tap Example Description.");
        assertEquals(ex2.getQuery(),
                "SELECT TOP 10 * FROM TAP_SCHEMA.columns");
        
        assertEquals(tapExamples.getTapExamples().size(), 2);
    }

    @Test
    public void testTapExamplesConstructor()
    {
        tapExamples = new TapExamples(getMockExamples());
        assertNotNull(tapExamples.getTapExamples());
        for (TapExample example : tapExamples.getTapExamples())
        {
            assertEquals(example.getName(), "Test Tap Example");
            assertEquals(example.getDescription(), "This is a test tap example");
            assertEquals(example.getQuery(),
                    "SELECT TOP 1000 table_name, column_name, description FROM TAP_SCHEMA.columns");
            assertEquals(example.getTables().size(), 1);
            assertEquals(example.getTables().stream().findFirst().get(), "TAP_SCHEMA.columns");
        }
        assertEquals(tapExamples.getTapExamples().size(), 1);
    }

    /**
     * Test that a TAP Redirect to an external Tap examples page works.
     * 
     * @throws ConfigurationException ConfigurationException
     * @throws IOException IOException
     */
    @Test
    public void testTapExamplesRedirectURL() throws ConfigurationException, IOException
    {
        // load tap examples URL into Configuration
        config.getEndPoint("TAP").put(ConfigKeys.TAP_EXAMPLES_URL.getKey(), 
                this.redirectTapExamplesUrl);
        
        assertTrue(tapExamplesService.isReady());
        
        ModelAndView model = tapExamplesService.buildResponse(mockedResponse);
        assertNotNull(model);
        
        // verify the redirect worked
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(mockedResponse).sendRedirect(ac.capture());
        assertEquals(redirectTapExamplesUrl, ac.getValue());

        // test 200 ok
        ArgumentCaptor<Integer> acHttpStatus = ArgumentCaptor.forClass(Integer.class);
        verify(mockedResponse).setStatus(acHttpStatus.capture());
        assertEquals((Integer)HttpServletResponse.SC_OK, acHttpStatus.getValue());
    }

    
    /**
     * Test scenario where no config exists.
     * 
     * As per DALI Spec a 404 should be provided.
     * @throws ConfigurationException 
     */
    @Test
    public void testNoConfigurationExists() throws ConfigurationException
    {
        assertTrue(tapExamplesService.isReady());

        ModelAndView model = tapExamplesService.buildResponse(mockedResponse);
        String err = (String) model.getModel().get("error");
        assertEquals(err, "No examples configured in this environment.");
        
        // test 404 not found
        ArgumentCaptor<Integer> ac = ArgumentCaptor.forClass(Integer.class);
        verify(mockedResponse).setStatus(ac.capture());
        assertEquals((Integer)HttpServletResponse.SC_NOT_FOUND, ac.getValue());
    }
    
    @Test
    public void testConfigExistsDoesExist() throws ConfigurationException
    {
        config.getEndPoint("TAP").setExamples(getMockExamples());
        assertTrue(tapExamplesService.configurationExists());
    }

    @Test
    public void testConfigExistsDoesNotExist() throws ConfigurationException
    {
        // clear tap examples from end point
        config.getEndPoint("TAP").setExamples(null);
        
        assertFalse(tapExamplesService.configurationExists());
    }
}
