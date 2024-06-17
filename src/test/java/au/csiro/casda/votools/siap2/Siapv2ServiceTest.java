package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import au.csiro.BaseTest;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.tap.TapService;
import au.csiro.casda.votools.utils.VoKeys;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Check the Siapv2Service class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class Siapv2ServiceTest
{

    /**
     * Check the buildQuery method
     */
    public static class ValidateBuildQuery extends BaseTest
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private TapService tapService;

        private Siapv2Service siapv2Service;

        @BeforeEach
        public void setup() throws Exception
        {
            siapv2Service = new Siapv2Service(configRegistry, tapService);
        }

        @Test
        public void testBuildQuery()
        {
            String query = siapv2Service.buildQuery(new HashMap<String, String[]>());
            assertEquals("SELECT * FROM ivoa.obscore WHERE (dataproduct_type IN ('cube', 'image', 'visibility'))",
                    query);
        }

        @Test
        public void testBuildQueryNumeric()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT * FROM ivoa.obscore WHERE ((em_min <= 300 AND em_max >= 300)) AND "
                    + "(dataproduct_type IN ('cube', 'image', 'visibility'))", query);
        }

        @Test
        public void testBuildQueryDate()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("time", new String[] { "56658.0 57387.99999" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT * FROM ivoa.obscore WHERE ((t_min >= 56658.0 AND t_max <= 57387.99999)) AND "
                    + "(dataproduct_type IN ('cube', 'image', 'visibility'))", query);
        }

        @Test
        public void testBuildQueryText()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("instrument", new String[] { "ASKAP" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT * FROM ivoa.obscore WHERE ((lower(instrument_name) = 'askap')) AND "
                    + "(dataproduct_type IN ('cube', 'image', 'visibility'))", query);
        }

        @Test
        public void testBuildQueryMaxrec()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300" });
            paramsMap.put("maxrec", new String[] { "10" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT * FROM ivoa.obscore WHERE ((em_min <= 300 AND em_max >= 300)) AND "
                    + "(dataproduct_type IN ('cube', 'image', 'visibility'))", query);
        }

        @Test
        public void testBuildQueryConeSearch()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("pos", new String[] { "CIRCLE 10.0 -12.0 0.125" });
            paramsMap.put("maxrec", new String[] { "10" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT "
                    + "DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),POINT('ICRS GEOCENTER',10.0, -12.0)) as \"distance\", "
                    + "* FROM ivoa.obscore "
                    + "WHERE ((INTERSECTS(CIRCLE('ICRS GEOCENTER', 10.0, -12.0, 0.125),s_region)=1)) AND "
                    + "(dataproduct_type IN ('cube', 'image', 'visibility')) " + "ORDER BY 1 ASC", query);
        }

        @Test
        public void testBuildQueryPolygon()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("pos", new String[] { "POLYGON 10.0 -12.0 10.5 -12.0 10.5 -12.5 10.0 -12.5" });
            paramsMap.put("maxrec", new String[] { "10" });
            String query = siapv2Service.buildQuery(paramsMap);
            assertEquals("SELECT " + "* FROM ivoa.obscore "
                    + "WHERE ((INTERSECTS(POLYGON('ICRS GEOCENTER', 10.0, -12.0, 10.5, -12.0, 10.5, -12.5, 10.0, -12.5)"
                    + ",s_region)=1)) AND " //
                    + "(dataproduct_type IN ('cube', 'image', 'visibility'))", query);
        }
        
    }

    /**
     * Check the buildQuery method
     */
    public static class ValidateBuildSiapQueryText extends BaseTest
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private TapService tapService;

        private Siapv2Service siapv2Service;

        @BeforeEach
        public void setup() throws Exception
        {
            siapv2Service = new Siapv2Service(configRegistry, tapService);
        }

        @Test
        public void testBuildSiapQueryText()
        {
            String queryText = siapv2Service.buildSiapQueryText(new HashMap<String, String[]>());
            assertEquals("", queryText);
        }

        @Test
        public void testBuildSiapQueryTextNumeric()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300" });
            String queryText = siapv2Service.buildSiapQueryText(paramsMap);
            assertEquals("BAND=300", queryText);
        }

        @Test
        public void testBuildSiapQueryTextNumericMultiple()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300", "500" });
            String queryText = siapv2Service.buildSiapQueryText(paramsMap);
            assertEquals("BAND=300&BAND=500", queryText);
        }

        @Test
        public void testBuildSiapQueryTextDifferentKeys()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300", "500" });
            paramsMap.put("pos", new String[] { "CIRCLE 20 -40 5" });
            String queryText = siapv2Service.buildSiapQueryText(paramsMap);
            assertEquals("POS=CIRCLE 20 -40 5&BAND=300&BAND=500", queryText);
        }
    }

    /**
     * Check the validateSiapv2Job method
     */
    public static class ValidateValidateSiap2Job extends BaseTest
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private TapService tapService;

        private Siapv2Service siapv2Service;

        @BeforeEach
        public void setup() throws Exception
        {
            siapv2Service = new Siapv2Service(configRegistry, tapService);
        }

        @Test
        public void testValidateSiap2JobInvalid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("pos", new String[] { "foo" });
            List<String> result = siapv2Service.validateSiapv2Job(params);
            assertEquals("UsageFault: Invalid POS value foo", result.get(0));
        }

        @Test
        public void testValidateSiap2JobMultipleInvalid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("pos", new String[] { "foo", "RANGE 50.0/60.0 -24.0/-30.0" });
            List<String> result = siapv2Service.validateSiapv2Job(params);
            assertEquals("UsageFault: Invalid POS value foo", result.get(0));
            assertEquals("UsageFault: Invalid POS value RANGE 50.0/60.0 -24.0/-30.0", result.get(1));
        }

        @Test
        public void testValidateSiap2JobValid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("pos", new String[] { "CIRCLE 10 20 2" });
            List<String> result = siapv2Service.validateSiapv2Job(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSiap2JobIgnoreUnknown() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("foo", new String[] { "bar" });
            List<String> result = siapv2Service.validateSiapv2Job(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateMaxRecParam() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("maxrec", new String[] { "100" });
            List<String> result = siapv2Service.validateSiapv2Job(params);
            assertThat(result, is(empty()));

            params = new HashMap<>();
            params.put("maxrec", new String[] { "   100    " });
            result = siapv2Service.validateSiapv2Job(params);
            assertThat(result, is(empty()));

            params = new HashMap<>();
            params.put("maxrec", new String[] { "xx" });
            result = siapv2Service.validateSiapv2Job(params);
            assertEquals(result.get(0),
                    "UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number");

            params = new HashMap<>();
            params.put("maxrec", new String[] { "    xx    " });
            result = siapv2Service.validateSiapv2Job(params);
            assertEquals(result.get(0),
                    "UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number");

            params = new HashMap<>();
            params.put("maxrec", new String[] { "100", "200" });
            result = siapv2Service.validateSiapv2Job(params);
            assertEquals(result.get(0), "UsageFault: Query can only contain a single MAXREC value");

            params = new HashMap<>();
            params.put("maxrec", new String[] { "100 200" });
            result = siapv2Service.validateSiapv2Job(params);
            assertEquals(result.get(0), "UsageFault: Query can only contain a single MAXREC value");
        }
    }

    /**
     * Check params passed to Tap service for siap 2 job
     */
    public static class CheckParamsPassedToTapServiceForSiap2Job extends BaseTest
    {

        private static final int NUM_STANDARD_JOB_PARAMS = 9;

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private TapService tapService;

        private Siapv2Service siapv2Service;

        @BeforeEach
        public void setup() throws Exception
        {
            siapv2Service = new Siapv2Service(configRegistry, tapService);
            siapv2Service.setDistanceFieldKey("SCS|Distance");
            siapv2Service.setDistanceFieldHeader("header");
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        public void testCheckVoTableHeadingPassedToTapServiceForSiap2Job() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            siapv2Service.processQuery(writer, paramsMap);

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> fieldMapParamsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), eq(null), any(ArrayList.class),
                    fieldMapParamsCaptor.capture());
            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(Siapv2Service.CASDA_SIAPV2_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertEquals(NUM_STANDARD_JOB_PARAMS, tapJobParams.size());
            Map<String, String> fieldMap = (Map<String, String>) fieldMapParamsCaptor.getValue();
            assertEquals("header", fieldMap.get("SCS|Distance"));
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        public void testCheckMaxrecPassedToTapServiceForSiap2Job() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put(VoKeys.STR_KEY_MAXREC, new String[] { "7" });
            siapv2Service.processQuery(writer, paramsMap);

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), eq(null), any(List.class),
                    any(Map.class));
            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(Siapv2Service.CASDA_SIAPV2_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertEquals("7", tapJobParams.get(VoKeys.STR_KEY_MAXREC));
            assertEquals(NUM_STANDARD_JOB_PARAMS + 1, tapJobParams.size());
        }
    }
    
    /**
     * Tests for the createDistanceFieldHeader method.
     */
    public static class CheckCreateDistanceFieldHeader extends BaseTest
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private TapService tapService;

        private Siapv2Service siapv2Service;

        @BeforeEach
        public void setup() throws ConfigurationException
        {
            siapv2Service = new Siapv2Service(configRegistry, tapService);
        }
        

        @Test
        public void testCreateDistanceFieldHeader()
        {
            siapv2Service.createDistanceFieldHeader();
            assertEquals("scs|distance", siapv2Service.getDistanceFieldKey());
            assertEquals("<FIELD name=\"distance\" ID=\"distance\" datatype=\"double\" unit=\"deg\" "
                    + "ucd=\"pos.angDistance\" >\r\n"
                    + " <DESCRIPTION>The angular distance of the centre of the parent image from the requested "
                    + "position</DESCRIPTION>\r\n" //
                    + "</FIELD>\r\n", siapv2Service.getDistanceFieldHeader());
        }
    }
}
