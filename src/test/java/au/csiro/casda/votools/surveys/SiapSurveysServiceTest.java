package au.csiro.casda.votools.surveys;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Test class for SiapSurveysService
 * 
 * Copyright 2022, CSIRO Australia All rights reserved.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class SiapSurveysServiceTest
{
    @InjectMocks
    private SiapSurveysService siapSurveysService;

    @Mock
    private ConfigurationRegistry configRegistry;

    private Configuration config;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Setup
     * 
     * @throws ConfigurationException
     *             ConfigurationException
     */
    @Before
    public void setup() throws ConfigurationException
    {
        MockitoAnnotations.initMocks(this);

        // setup Configuration
        config = ConfigurationTest.getTestConfiguration();
        configRegistry.switchConfiguration(config, false);
        siapSurveysService.setConfiguration(config);
    }

    private List<Map<String, String>> getMockSurveys()
    {
        List<Map<String, String>> surveys = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put(SiapSurveysService.SurveyKeys.CODE.getKey(), "RACS-Low");
        map.put(SiapSurveysService.SurveyKeys.NAME.getKey(), "Rapid ASKAP Continuum Survey (Low)");
        map.put(SiapSurveysService.SurveyKeys.DESCRIPTION.getKey(), "Conducted...");
        map.put(SiapSurveysService.SurveyKeys.WHERE_CLAUSE.getKey(),
                "(dataproduct_type = 'cube' and calib_level = 3 and filename LIKE ‘RACS%DR1%A.fits’)");
        surveys.add(map);

        map = new HashMap<>();
        map.put(SiapSurveysService.SurveyKeys.CODE.getKey(), "Survey2");
        map.put(SiapSurveysService.SurveyKeys.NAME.getKey(), "A second survey");
        map.put(SiapSurveysService.SurveyKeys.DESCRIPTION.getKey(), "Some information");
        map.put(SiapSurveysService.SurveyKeys.WHERE_CLAUSE.getKey(), "(a=b)");
        surveys.add(map);
        return surveys;
    }

    @Test
    public void testSurveysFromConfiguration() throws Exception
    {
        config.getEndPoint("SIA1").setSurveys(getMockSurveys());
        assertTrue(siapSurveysService.isReady());

        List<SiapSurvey> surveys = siapSurveysService.getSiapSurveys();
        
        assertNotNull("Expected surveys", surveys);

        SiapSurvey survey = surveys.get(0);
        assertEquals("RACS-Low", survey.getCode());
        assertEquals("Rapid ASKAP Continuum Survey (Low)", survey.getName());
        assertEquals("Conducted...", survey.getDescription());
        assertEquals("(dataproduct_type = 'cube' and calib_level = 3 and filename LIKE ‘RACS%DR1%A.fits’)",
                survey.getWhereClause());
        assertEquals("http://localhost:8040/casda_vo_tools/sia1/query?SURVEY=RACS-Low&amp;", survey.getEndpoint());

        survey = surveys.get(1);
        assertEquals("Survey2", survey.getCode());
        assertEquals("A second survey", survey.getName());
        assertEquals("Some information", survey.getDescription());
        assertEquals("(a=b)",
                survey.getWhereClause());
        assertEquals("http://localhost:8040/casda_vo_tools/sia1/query?SURVEY=Survey2&amp;", survey.getEndpoint());
        assertEquals(2, surveys.size());
    }

    @Test
    public void testLoadSurveysFromXML() throws Exception
    {
        config.put(ConfigKeys.SIA1_SURVEYS_CONFIG_FILE.getKey(), "src/test/resources/testdata/sia1_surveys.xml");
        assertTrue(siapSurveysService.isReady());

        List<SiapSurvey> surveys = siapSurveysService.getSiapSurveys();

        // Verify examples loaded from XML correctly
        assertNotNull(surveys);

        // first survey data check
        SiapSurvey surv1 = surveys.get(0);
        assertEquals("RACS-Low", surv1.getCode());
        assertEquals("RACS Low Epoch 1", surv1.getName());
        assertEquals("RACS DR1 image set", surv1.getDescription());
        assertEquals("(dataproduct_type = 'cube' and calib_level = 3 and filename LIKE 'RACS%DR1%A.fits')",
                surv1.getWhereClause());
        assertEquals("http://localhost:8040/casda_vo_tools/sia1/query?SURVEY=RACS-Low&amp;", surv1.getEndpoint());

        // Second survey data check
        SiapSurvey surv2 = surveys.get(1);
        assertEquals("VAST", surv2.getCode());
        assertEquals("Variables And Slow Transient - Epoch 1", surv2.getName());
        assertEquals("Images from the first observing run of VAST", surv2.getDescription());
        assertEquals("(obs_collection = 'AS107' and dataproduct_type = 'cube' and calib_level = 3)",
                surv2.getWhereClause());
        assertEquals("http://localhost:8040/casda_vo_tools/sia1/query?SURVEY=VAST&amp;", surv2.getEndpoint());

        assertEquals(2, surveys.size());
    }
    
    @Test
    public void testConfigExistsDoesExist() throws ConfigurationException
    {
        config.getEndPoint("SIA1").setSurveys(getMockSurveys());
        assertTrue(siapSurveysService.configurationExists());
    }
    
    @Test
    public void testConfigExistsDoesNotExist() throws ConfigurationException
    {
        config.getEndPoint("SIA1").setSurveys(null);
        assertFalse(siapSurveysService.configurationExists());
    }
    
    @Test
    public void testGetSurvey() throws Exception
    {
        config.getEndPoint("SIA1").setSurveys(getMockSurveys());
        assertTrue(siapSurveysService.isReady());

        SiapSurvey survey = siapSurveysService.getSurvey("Survey2");
        assertEquals("Survey2", survey.getCode());
        assertEquals("A second survey", survey.getName());

        survey = siapSurveysService.getSurvey("RACS-Low");
        assertEquals("RACS-Low", survey.getCode());
        assertEquals("Rapid ASKAP Continuum Survey (Low)", survey.getName());

        assertNull(siapSurveysService.getSurvey("RACS-High"));
        assertNull(siapSurveysService.getSurvey(""));
        assertNull(siapSurveysService.getSurvey(null));
    }

    @Test
    public void testGetSurveyCodeList() throws Exception
    {
        config.getEndPoint("SIA1").setSurveys(getMockSurveys());
        assertTrue(siapSurveysService.isReady());

        List<String> surveyCodeList = siapSurveysService.getSurveyCodeList();
        
        assertThat(surveyCodeList, contains("RACS-Low", "Survey2"));
    }
}
