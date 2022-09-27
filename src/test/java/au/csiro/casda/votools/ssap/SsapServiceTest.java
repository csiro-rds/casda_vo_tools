package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.unitils.util.FileUtils;

import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configuration;
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
 * Check the SsapService class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class SsapServiceTest
{

    /**
     * Check the buildQuery method
     */
    public static class ValidateBuildQuery
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public ValidateBuildQuery() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @Test
        public void testBuildQuery()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("request", new String[] { "queryData" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm", query);
        }

        @Test
        public void testBuildQueryNumeric()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300" });
            paramsMap.put("request", new String[] { "queryData" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE ((em_min <= 300 AND em_max >= 300))", query);
        }

        @Test
        @Ignore(value="Ignored until we implement date SSAP parameters")
        public void testBuildQueryDate()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("request", new String[] { "queryData" });
            paramsMap.put("time", new String[] { "56658.0 57387.99999" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE ((t_min >= 56658.0 AND t_max <= 57387.99999))", query);
        }

        @Test
        @Ignore(value="Ignored until we implement text SSAP parameters")
        public void testBuildQueryText()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("request", new String[] { "queryData" });
            paramsMap.put("instrument", new String[] { "ASKAP" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE ((lower(instrument_name) = 'askap'))", query);
        }

        @Test
        public void testBuildQueryPosNoSize()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("pos", new String[] { "13.15835,-72.80032" });
            paramsMap.put("request", new String[] { "queryData" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE "
                    + "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 13.15835, -72.80032, 0.025),s_region)=1)", query);
        }

        @Test
        public void testBuildQueryPosSize()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("pos", new String[] { "13.15835,-72.80032" });
            paramsMap.put("size", new String[] { "3" });
            paramsMap.put("request", new String[] { "queryData" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE "
                    + "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 13.15835, -72.80032, 1.5),s_region)=1)", query);
        }

        @Test
        public void testBuildQueryMaxrec()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("request", new String[] { "queryData" });
            paramsMap.put("band", new String[] { "300" });
            paramsMap.put("maxrec", new String[] { "10" });
            String query = ssapService.buildQuery(paramsMap);
            assertEquals("SELECT * FROM casda.specdm WHERE ((em_min <= 300 AND em_max >= 300))", query);
        }
        
    }

    /**
     * Check the buildQuery method
     */
    public static class ValidateBuildSiapQueryText
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public ValidateBuildSiapQueryText() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @Test
        public void testBuildSiapQueryText()
        {
            String queryText = ssapService.buildSsapQueryText(new HashMap<String, String[]>());
            assertEquals("", queryText);
        }

        @Test
        public void testBuildSiapQueryTextNumeric()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300" });
            String queryText = ssapService.buildSsapQueryText(paramsMap);
            assertEquals("BAND=300", queryText);
        }

        @Test
        public void testBuildSiapQueryTextNumericMultiple()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300", "500" });
            String queryText = ssapService.buildSsapQueryText(paramsMap);
            assertEquals("BAND=300&BAND=500", queryText);
        }

        @Test
        public void testBuildSiapQueryTextDifferentKeys()
        {
            HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
            paramsMap.put("band", new String[] { "300", "500" });
            paramsMap.put("pos", new String[] { "CIRCLE 20 -40 5" });
            String queryText = ssapService.buildSsapQueryText(paramsMap);
            assertEquals("POS=CIRCLE 20 -40 5&BAND=300&BAND=500", queryText);
        }
    }

    /**
     * Check the validateSsapJob method
     */
    public static class ValidateValidateSsapJob
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public ValidateValidateSsapJob() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @Test
        public void testValidateSsapJobInvalid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("pos", new String[] { "foo" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("UsageFault: Invalid POS value foo"));
        }

        @Test
        public void testValidateSsapJobMultipleRejected() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("pos", new String[] { "foo", "RANGE 50.0/60.0 -24.0/-30.0" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("UsageFault: Only a single POS value may be specified"));
        }

        @Test
        public void testValidateSsapJobInvalidRequestRejected() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"makeTea"});
            params.put("pos", new String[] { "50.0,60.0" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("UsageFault: REQUEST value must be queryData"));
        }

        @Test
        public void testValidateSsapJobRequestCaseInsensitive() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"QUERYDATA"});
            params.put("pos", new String[] { "50.0,60.0" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSsapJobMultipleRequestRejected() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData", "queryData"});
            params.put("pos", new String[] { "50.0,60.0" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("UsageFault: Only a single REQUEST value may be specified"));
        }

        @Test
        public void testValidateSsapJobValid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("pos", new String[] { "10,20" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSsapJobRequireRequest() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("UsageFault: Parameter REQUEST is required"));
        }

        @Test
        public void testValidateSsapJobIgnoreUnknown() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("foo", new String[] { "bar" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateMaxRecParam() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "100" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));

            params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "   100    " });
            result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));

            params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "xx" });
            result = ssapService.validateSsapJob(params);
            assertEquals("UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number",
                    result.get(0));

            params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "    xx    " });
            result = ssapService.validateSsapJob(params);
            assertEquals("UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number",
                    result.get(0));

            params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "100", "200" });
            result = ssapService.validateSsapJob(params);
            assertEquals("UsageFault: Only a single MAXREC value may be specified", result.get(0));

            params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("maxrec", new String[] { "100 200" });
            result = ssapService.validateSsapJob(params);
            assertEquals("UsageFault: Query can only contain a single MAXREC value", result.get(0));
        }

        @Test
        public void testValidateSsapJobVersionMatch() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});

            String[] supportedVersions = new String[] {"1.0", "1.1"};
            for (String version : supportedVersions)
            {
                params.put("version", new String[] { version });
                List<String> result = ssapService.validateSsapJob(params);
                assertThat(result, is(empty()));
            }
        }

        @Test
        public void testValidateSsapJobVersionMismatch() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            
            String[] unsupportedVersions = new String[] {"0.9", "1.1.2", "1.12", "1.2", "2.0"};
            for (String version : unsupportedVersions)
            {
                params.put("version", new String[] { version });
                List<String> result = ssapService.validateSsapJob(params);
                assertEquals(result.get(0), "UsageFault: Version mismatch error");
            }
        }

        @Test
        public void testValidateSsapJobMetadata() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("format", new String[] { "metaData" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSsapJobMetadataIgnoreOtherInvalid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            params.put("format", new String[] { "metaData" });
            params.put("pos", new String[] { "foo" });
            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSsapNotSupported() throws IOException, ConfigurationException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("request", new String[] {"queryData"});
            Mockito.when(configuration.get(ConfigKeys.SSAP_TABLE.getKey())).thenReturn(null);
            ssapService.setConfiguration(configuration);
            ssapService.isReady();

            List<String> result = ssapService.validateSsapJob(params);
            assertThat(result, contains("FatalFault: SSAP is not supported by this service."));
        }
    }

    /**
     * Check params passed to Tap service for siap 2 job
     */
    public static class CheckParamsPassedToTapServiceForSsapJob
    {

        private static final int NUM_STANDARD_JOB_PARAMS = 10;

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public CheckParamsPassedToTapServiceForSsapJob() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        public void testCheckVoTableHeadingPassedToTapServiceForSsapJob() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put("request", new String[] {"queryData"});
            ssapService.processQuery(writer, paramsMap);

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> tapJobMetadataCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<List> tapJobUploadedTablesCaptor = ArgumentCaptor.forClass(List.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), tapJobMetadataCaptor.capture(),
                    tapJobUploadedTablesCaptor.capture());
            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(SsapService.CASDA_SSAP_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertEquals(NUM_STANDARD_JOB_PARAMS, tapJobParams.size());
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        public void testCheckMaxrecPassedToTapServiceForSsapJob() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put(VoKeys.STR_KEY_MAXREC, new String[] { "7" });
            paramsMap.put("request", new String[] {"queryData"});
            boolean result = ssapService.processQuery(writer, paramsMap);

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> tapJobMetadataCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<List> tapJobUploadedTablesCaptor = ArgumentCaptor.forClass(List.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), tapJobMetadataCaptor.capture(),
                    tapJobUploadedTablesCaptor.capture());
            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(SsapService.CASDA_SSAP_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertEquals("7", tapJobParams.get(VoKeys.STR_KEY_MAXREC));
            assertEquals(NUM_STANDARD_JOB_PARAMS, tapJobParams.size());
            
            assertTrue("processQuery should have returned success", result);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Test
        public void testCheckMaxrecCappedForSsapJob() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put(VoKeys.STR_KEY_MAXREC, new String[] { "700000000" });
            paramsMap.put("request", new String[] {"queryData"});
            boolean result = ssapService.processQuery(writer, paramsMap);

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> tapJobMetadataCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<List> tapJobUploadedTablesCaptor = ArgumentCaptor.forClass(List.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), tapJobMetadataCaptor.capture(),
                    tapJobUploadedTablesCaptor.capture());
            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(SsapService.CASDA_SSAP_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertEquals("200", tapJobParams.get(VoKeys.STR_KEY_MAXREC));
            assertEquals(NUM_STANDARD_JOB_PARAMS, tapJobParams.size());
            
            assertTrue("processQuery should have returned success", result);
        }
    }

    /**
     * Check validation errors are correctly handled for SSAP jobs
     */
    public static class CheckErrorsReportedForSsapJob
    {

        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public CheckErrorsReportedForSsapJob() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @Test
        public void testErrorsReportedForSsapJob() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put("request", new String[] {"invalid"});
            boolean result = ssapService.processQuery(writer, paramsMap);

            verify(tapService, never()).processQuery(anyObject(), anyObject());
            
            String errorResult = writer.toString();
            //System.out.println(errorResult);
            assertThat(errorResult, containsString("UsageFault: REQUEST value must be queryData"));
            assertThat(errorResult, containsString("<INFO name=\"QUERY_STATUS\" value=\"ERROR\">"));
            assertThat(errorResult, containsString("<VOTABLE"));
            assertThat(errorResult, containsString("</VOTABLE>"));

            assertFalse("processQuery should have returned failure", result);
        }
    }

    /**
     * Check metadata request processing
     */
    public static class CheckMetadataRequest
    {
        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Rule
        public TemporaryFolder tempFolder = new TemporaryFolder();
        
        @Mock
        private ConfigurationRegistry configRegistry;

        @Mock
        private Configuration configuration;

        @Mock
        private TapService tapService;

        private SsapService ssapService;

        public CheckMetadataRequest() throws Exception
        {
            MockitoAnnotations.initMocks(this);
            ssapService = prepareSsapService(tapService, configuration, configRegistry);
        }

        @Test
        public void testDefaultMetadataResponse() throws Exception
        {
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put("request", new String[] {"querydata"});
            paramsMap.put("format", new String[] {"metadata"});
            boolean result = ssapService.processQuery(writer, paramsMap);

            verify(tapService, never()).processQuery(anyObject(), anyObject());
            
            String metadataResult = writer.toString();
            //System.out.println(metadataResult);
            assertThat(metadataResult, containsString("<RESOURCE name=\"CASDA SSAP Result\" type='results'>"));
            assertThat(metadataResult, containsString("<INFO name=\"QUERY_STATUS\" value=\"OK\">"));
            assertThat(metadataResult, containsString("<INFO name=\"SERVICE_PROTOCOL\" value=\"1.1\">"));
            assertThat(metadataResult,
                    containsString("<PARAM name=\"INPUT:SIZE\" value=\"0.05\" datatype=\"double\" unit=\"deg\">"));
            
            assertThat(metadataResult, containsString(
                    "<PARAM name=\"OUTPUT:access_url\" ID=\"access_url\" value=\"\" datatype=\"char\""));

            assertTrue("processQuery should have returned success", result);
        }

        @Test
        public void testMissingMetadataResponse() throws Exception
        {
            thrown.expect(FileNotFoundException.class);
            
            Mockito.when(configuration.get(ConfigKeys.SSAP_METADATA_RESPONSE.getKey())).thenReturn("foo");
            ssapService.setConfiguration(configuration);
            ssapService.isReady();
            
            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put("request", new String[] {"querydata"});
            paramsMap.put("format", new String[] {"metadata"});
            ssapService.processQuery(writer, paramsMap);
        }

        @Test
        public void testNonDefaultMetadataResponse() throws Exception
        {
            String expectedResponse = "<xml>This is a response</xml>";

            File responseFile = tempFolder.newFile("response.xml");
            FileUtils.writeStringToFile(responseFile, expectedResponse);
            
            Mockito.when(configuration.get(ConfigKeys.SSAP_METADATA_RESPONSE.getKey()))
                    .thenReturn(responseFile.getAbsolutePath());
            ssapService.setConfiguration(configuration);
            ssapService.isReady();

            StringWriter writer = new StringWriter();
            Map<String, String[]> paramsMap = new HashMap<>();
            paramsMap.put("request", new String[] {"querydata"});
            paramsMap.put("format", new String[] {"metadata"});
            boolean result = ssapService.processQuery(writer, paramsMap);

            verify(tapService, never()).processQuery(anyObject(), anyObject());
            
            String metadataResult = writer.toString();
            assertThat(metadataResult, is(expectedResponse));

            assertTrue("processQuery should have returned success", result);
        }
    }

    static SsapService prepareSsapService(TapService tapService, Configuration configuration,
            ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        Mockito.when(tapService.isReady()).thenReturn(true);
        Mockito.when(configuration.get(ConfigKeys.SSAP_TABLE.getKey())).thenReturn("casda.specdm");
        Mockito.when(configuration.get(ConfigKeys.SSAP_OUTPUT_LIMIT.getKey())).thenReturn("200");
        Mockito.when(configuration.get(ConfigKeys.SSAP_DEFAULT_MAX_REC.getKey())).thenReturn("10");
        SsapService ssapService = new SsapService(configRegistry, tapService);
        ssapService.setConfiguration(configuration);
        ssapService.isReady();
        return ssapService;
    }
}
