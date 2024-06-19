package au.csiro.casda.votools.siap1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import au.csiro.BaseTest;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.surveys.SiapSurvey;
import au.csiro.casda.votools.surveys.SiapSurveysService;
import au.csiro.casda.votools.tap.TapService;
import au.csiro.casda.votools.utils.VoKeys;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Check the Siap1Service class.
 * <p>
 * Copyright 2022, CSIRO Australia All rights reserved.
 */
public class Siap1ServiceTest
{

    /**
     * Base class for helpers for testing the Siap1Service class.
     * <p>
     * Copyright 2022, CSIRO Australia. All rights reserved.
     */
    public abstract static class BaseSiap1ServiceTest extends BaseTest
    {
        @Mock
        protected ConfigurationRegistry configRegistry;

        @Mock
        protected TapService tapService;

        @Mock
        protected SiapSurveysService siapSurveysService;

        protected Siap1Service siap1Service;

        private Configuration config;

        @BeforeEach
        public void baseSiap1Setup() throws Exception
        {
            siap1Service = new Siap1Service(configRegistry, tapService, siapSurveysService);
            config = new Configuration();
            siap1Service.setConfiguration(config);
            when(tapService.isReady()).thenReturn(true);
            when(siapSurveysService.isReady()).thenReturn(true);
        }

        protected void configureSurveys()
        {
            SiapSurvey survey = new SiapSurvey();
            survey.setCode("ABC");
            survey.setName("A First Survey");
            survey.setWhereClause("(obs_program = 'survey1')");
            when(siapSurveysService.getSurvey("ABC")).thenReturn(survey);
            when(siapSurveysService.getSurveyCodeList()).thenReturn(Arrays.asList("ABC"));

            Map<String, String> settings = new HashMap<>();
            settings.put(ConfigKeys.SIA1_ACCESS_URL.getKey(),
                    "https://someplace/casda_data_access/sia1download/{obs_publisher_did}?"
                            + "format={access_format}&pos={pos}&size={size}");
            settings.put(ConfigKeys.SIA1_FORMAT_TABLE.getKey(), "internal.sia1_format");
            settings.put(ConfigKeys.SIA1_OUTPUT_LIMIT.getKey(), "200");
            settings.put(ConfigKeys.SIA1_DEFAULT_MAX_REC.getKey(), "10");
            config.setOptions(settings);
        }

        protected Map<String, String[]> buildParamsMap(String survey, String pos, String size, String format)
        {
            Map<String, String[]> paramsMap = new HashMap<>();
            if (survey != null)
            {
                paramsMap.put("survey", new String[] { survey });
            }
            if (pos != null)
            {
                paramsMap.put("pos", new String[] { pos });
            }
            if (size != null)
            {
                paramsMap.put("size", new String[] { size });
            }
            if (format != null)
            {
                paramsMap.put("format", new String[] { format });
            }
            return paramsMap;
        }

    }

    /**
     * Check the buildQuery method
     */
    public static class CheckBuildQuery extends BaseSiap1ServiceTest
    {

        @BeforeEach
        public void setup() throws Exception
        {
            configureSurveys();
            assertTrue(siap1Service.isReady());
        }

        @Test
        public void testBuildQuery()
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", "120,-31", "0.2", null);
            String query = siap1Service.buildQuery(paramsMap);
            assertEquals("SELECT DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',120.000000, -31.000000)) as distance_deg, "
                    + "to_char(DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',120.000000, -31.000000)), 'FM990.9999') as \"distance\", "
                    + "internal.sia1_format.content_type as access_format, "
                    + "internal.sia1_format.pix_flags, 'A First Survey' as survey, "
                    + "'Cutout from '||ivoa.obscore.filename as image_title, "
                    + "120.000000 as s_ra, -31.000000 as s_dec, ivoa.obscore.instrument_name, ivoa.obscore.t_min, "
                    + "2 as n_axes, "
                    + "ceil(0.200000/sqrt(s_fov/(s_xel1*s_xel2)))||' '||ceil(0.200000/sqrt(s_fov/(s_xel1*s_xel2))) "
                    + "as n_axis, "
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999')||' '||"
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999') as image_scale, "
                    + "to_char(ceil(ivoa.obscore.access_estsize*1024*(0.040000/s_fov)), 'FM999999999999') "
                    + "as est_size_bytes, "
                    + "'https://someplace/casda_data_access/sia1download/'||"
                    + "ivoa.obscore.obs_publisher_did||'?format='||internal.sia1_format.content_type||'" //
                    + "&pos=120.000000,-31.000000&size=0.200000,0.200000' as access_url " //
                    + "FROM ivoa.obscore, internal.sia1_format "
                    + "WHERE (sia1_format.content_type IN ('image/fits', 'image/png')) AND "
                    + "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 120.0, -31.0, 0.1),s_region)=1) AND "
                    + "((obs_program = 'survey1')) " + "ORDER BY 1 ASC, 2 ASC", query);
        }

        @Test
        public void testBuildQueryGraphicFormat()
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", "201.2,+15.75", "0.2", "image/png");
            String query = siap1Service.buildQuery(paramsMap);
            assertEquals("SELECT DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',201.200000, 15.750000)) as distance_deg, "
                    + "to_char(DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',201.200000, 15.750000)), 'FM990.9999') as \"distance\", "
                    + "internal.sia1_format.content_type as access_format, "
                    + "internal.sia1_format.pix_flags, 'A First Survey' as survey, "
                    + "'Cutout from '||ivoa.obscore.filename as image_title, "
                    + "201.200000 as s_ra, 15.750000 as s_dec, ivoa.obscore.instrument_name, ivoa.obscore.t_min, "
                    + "2 as n_axes, "
                    + "ceil(0.200000/sqrt(s_fov/(s_xel1*s_xel2)))||' '||ceil(0.200000/sqrt(s_fov/(s_xel1*s_xel2))) "
                    + "as n_axis, "
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999')||' '||"
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999') as image_scale, "
                    + "to_char(ceil(ivoa.obscore.access_estsize*1024*(0.040000/s_fov)), 'FM999999999999') "
                    + "as est_size_bytes, "
                    + "'https://someplace/casda_data_access/sia1download/'||"
                    + "ivoa.obscore.obs_publisher_did||'?format='||internal.sia1_format.content_type||'" //
                    + "&pos=201.200000,15.750000&size=0.200000,0.200000' as access_url " //
                    + "FROM ivoa.obscore, internal.sia1_format "
                    + "WHERE (sia1_format.content_type IN ('image/png')) AND "
                    + "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 201.2, 15.75, 0.1),s_region)=1) AND "
                    + "((obs_program = 'survey1')) " + "ORDER BY 1 ASC, 2 ASC", query);
        }

        @Test
        public void testBuildQueryUnsupportedFormat()
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", "120,-31", "0.3", "image/BMP");
            String query = siap1Service.buildQuery(paramsMap);
            assertEquals("SELECT DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',120.000000, -31.000000)) as distance_deg, "
                    + "to_char(DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),"
                    + "POINT('ICRS GEOCENTER',120.000000, -31.000000)), 'FM990.9999') as \"distance\", "
                    + "internal.sia1_format.content_type as access_format, "
                    + "internal.sia1_format.pix_flags, 'A First Survey' as survey, "
                    + "'Cutout from '||ivoa.obscore.filename as image_title, "
                    + "120.000000 as s_ra, -31.000000 as s_dec, ivoa.obscore.instrument_name, ivoa.obscore.t_min, "
                    + "2 as n_axes, "
                    + "ceil(0.300000/sqrt(s_fov/(s_xel1*s_xel2)))||' '||ceil(0.300000/sqrt(s_fov/(s_xel1*s_xel2))) "
                    + "as n_axis, "
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999')||' '||"
                    + "to_char(sqrt(s_fov/(s_xel1*s_xel2)), 'FM990.99999') as image_scale, "
                    + "to_char(ceil(ivoa.obscore.access_estsize*1024*(0.090000/s_fov)), 'FM999999999999') "
                    + "as est_size_bytes, "
                    + "'https://someplace/casda_data_access/sia1download/'||"
                    + "ivoa.obscore.obs_publisher_did||'?format='||internal.sia1_format.content_type||'" //
                    + "&pos=120.000000,-31.000000&size=0.300000,0.300000' as access_url " //
                    + "FROM ivoa.obscore, internal.sia1_format " + "WHERE (sia1_format.content_type IN ('')) AND "
                    + "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 120.0, -31.0, 0.15),s_region)=1) AND "
                    + "((obs_program = 'survey1')) " + "ORDER BY 1 ASC, 2 ASC", query);
        }

    }

    /**
     * Check the validateSiap1Job method
     */
    public static class ValidateValidateSiap1Job extends BaseSiap1ServiceTest
    {

        @BeforeEach
        public void setup() throws Exception
        {
            configureSurveys();
            assertTrue(siap1Service.isReady());
        }

        @Test
        public void testValidateSiap1JobInvalid() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("pos", new String[] { "foo" });
            List<String> result = siap1Service.validateSiap1Job(params);
            assertEquals("UsageFault: Invalid POS value foo", result.get(0));
        }

        @Test
        public void testValidateSiap1JobMissingValue() throws IOException
        {
            Map<String, String[]> params = buildParamsMap("ABC", "110,-12", null, null);
            List<String> result = siap1Service.validateSiap1Job(params);
            assertEquals("UsageFault: Parameter SIZE is required", result.get(0));
            assertEquals(1, result.size());
        }

        @Test
        public void testValidateSiap1JobNoParams() throws IOException
        {
            Map<String, String[]> params = new HashMap<>();
            List<String> result = siap1Service.validateSiap1Job(params);
            assertEquals("UsageFault: Parameter POS is required", result.get(0));
            assertEquals("UsageFault: Parameter SIZE is required", result.get(1));
            assertEquals("UsageFault: Parameter SURVEY is required", result.get(2));
            assertEquals(3, result.size());
        }

        @Test
        public void testValidateSiap1JobMultipleValues() throws IOException
        {
            Map<String, String[]> params = buildParamsMap("ABC", null, "0.2", null);
            params.put("pos", new String[] { "10,-5", "5, -30" });

            List<String> result = siap1Service.validateSiap1Job(params);
            assertEquals("UsageFault: Only a single POS value may be specified", result.get(0));
            assertEquals(1, result.size());
        }

        @Test
        public void testValidateSiap1JobValid() throws IOException
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", "120,-31", "0.2", "image/fits,image/PNG");
            List<String> result = siap1Service.validateSiap1Job(paramsMap);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSiap1JobIgnoreUnknown() throws IOException
        {
            Map<String, String[]> params = buildParamsMap("ABC", "120,-31", "0.2", null);
            params.put("foo", new String[] { "bar" });
            List<String> result = siap1Service.validateSiap1Job(params);
            assertThat(result, is(empty()));
        }

        @Test
        public void testValidateSiap1JobNotConfigured() throws IOException
        {
            siap1Service.setConfiguration(null);
            Map<String, String[]> params = buildParamsMap("ABC", "120,-31", "0.2", null);
            List<String> result = siap1Service.validateSiap1Job(params);
            assertEquals("FatalFault: SIAP1 is not supported by this service.", result.get(0));
            assertEquals(1, result.size());
        }

        @Test
        public void testValidateSiap1JobMetadataQuery() throws IOException
        {
            Map<String, String[]> params = buildParamsMap(null, null, null, "METADATA");
            List<String> result = siap1Service.validateSiap1Job(params);
            assertThat(result, is(empty()));
        }
    }

    /**
     * Check the processQuery method.
     */
    public static class CheckProcessQuery extends BaseSiap1ServiceTest
    {

        private StringWriter writer = new StringWriter();

        @BeforeEach
        public void setup() throws Exception
        {
            configureSurveys();
            assertTrue(siap1Service.isReady());
        }

        @Test
        public void testProcessQueryInvalid() throws Exception
        {
            Map<String, String[]> params = new HashMap<>();
            params.put("pos", new String[] { "foo" });
            assertFalse("processQuery should have returned failure", siap1Service.processQuery(writer, params));

            verify(tapService, never()).processQuery(any(), any());

            String errorResult = writer.toString();
            // System.out.println(errorResult);
            assertThat(errorResult, containsString("UsageFault: Invalid POS value foo"));
            assertThat(errorResult, containsString("<INFO name=\"QUERY_STATUS\" value=\"ERROR\">"));
            assertThat(errorResult, containsString("<VOTABLE"));
            assertThat(errorResult, containsString("</VOTABLE>"));
        }

        @Test
        public void testProcessQueryMetadataQuery() throws Exception
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", null, null, "METADATA");

            assertTrue("processQuery should have returned success", siap1Service.processQuery(writer, paramsMap));
            String metadataResult = writer.toString();
            assertThat(metadataResult, containsString("<RESOURCE name=\"CASDA SIAP Result\" type=\"meta\">"));
            assertThat(metadataResult,
                    containsString("<DESCRIPTION>A First Survey Simple Image Access Service</DESCRIPTION>"));
            assertThat(metadataResult, containsString("<INFO name=\"QUERY_STATUS\" value=\"OK\">"));
            assertThat(metadataResult,
                    containsString("<PARAM name=\"INPUT:POS\" value=\"\" datatype=\"char\" arraysize=\"*\">"));
            assertThat(metadataResult,
                    containsString("<PARAM name=\"INPUT:SIZE\" value=\"0.2\" datatype=\"double\" unit=\"deg\">"));
            assertThat(metadataResult, containsString("<MAX value=\"2\" />"));
            assertThat(metadataResult, containsString("<PARAM name=\"INPUT:FORMAT\""));
            assertThat(metadataResult, containsString("<OPTION value=\"image/fits\" />"));
            assertThat(metadataResult, containsString("<OPTION value=\"all\" />"));
            assertThat(metadataResult, containsString("<FIELD name=\"OUTPUT:image_title\" "));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        public void testProcessQueryValid() throws Exception
        {
            Map<String, String[]> paramsMap = buildParamsMap("ABC", "120,-31", "0.2", "image/fits,image/PNG");
            paramsMap.put("maxrec", new String[] { "6" });

            assertTrue("processQuery should have returned success", siap1Service.processQuery(writer, paramsMap));

            ArgumentCaptor<Map> tapJobParamsCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> tapMetadataMapCaptor = ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Map> fieldMapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(tapService).processQuery(eq(writer), tapJobParamsCaptor.capture(), tapMetadataMapCaptor.capture(),
                    eq(new ArrayList<>()), fieldMapCaptor.capture());

            Map<String, String> tapJobParams = (Map<String, String>) tapJobParamsCaptor.getValue();
            assertEquals(Siap1Service.CASDA_SIAP1_RESULT_NAME, tapJobParams.get(VoKeys.VO_TABLE_HEADING));
            assertThat(tapJobParams.get(VoKeys.STR_KEY_ADQL_QUERY), startsWith("SELECT "));
            assertThat(tapJobParams.get(VoKeys.STR_KEY_ADQL_QUERY),
                    containsString("FROM ivoa.obscore, internal.sia1_format "));
            assertEquals("POS=120,-31&SIZE=0.2&SURVEY=ABC&FORMAT=image/fits,image/PNG&MAXREC=6",
                    tapJobParams.get(VoKeys.STR_KEY_SIAP_QUERY));
            assertEquals("6", tapJobParams.get(VoKeys.STR_KEY_MAXREC));
            assertEquals(10, tapJobParams.size());

        }
    }

    /**
     * Check the processQuery method.
     */
    public static class CheckReadFields extends BaseSiap1ServiceTest
    {
        
        @Test
        public void testReadFields()
        {
            Map<String, Siap1Field> fields = siap1Service.readFields();

            assertThat(fields.keySet(),
                    containsInAnyOrder("image_title", "instrument_name", "t_min", "s_ra", "s_dec", "n_axes", "n_axis",
                            "image_scale", "distance", "pix_flags", "access_format", "access_url", "survey",
                            "est_size_bytes"));

            Siap1Field imageScale = fields.get("image_scale");
            assertEquals("scs|image_scale", imageScale.getKey());
            assertEquals("double", imageScale.getDatatype());
            assertEquals("deg", imageScale.getUnit());
            assertEquals("VOX:Image_Scale", imageScale.getUcd());
            assertEquals("Spatial resolution of data", imageScale.getDescription());
            assertEquals(
                    "<FIELD ID=\"image_scale\" name=\"image_scale\" datatype=\"double\" arraysize=\"*\" unit=\"deg\" "
                            + "ucd=\"VOX:Image_Scale\"><DESCRIPTION>Spatial resolution of data</DESCRIPTION> </FIELD>",
                    imageScale.getFieldDef());

            Siap1Field access_url = fields.get("access_url");
            assertEquals("scs|access_url", access_url.getKey());
            assertEquals("*", access_url.getArraysize());
            assertNull("Unit should not be defined", access_url.getUnit());
            assertEquals(
                    "<FIELD ID=\"access_url\" name=\"access_url\" datatype=\"char\" arraysize=\"*\" "
                            + "ucd=\"VOX:Image_AccessReference\">"
                            + "<DESCRIPTION>URL used to access dataset</DESCRIPTION> </FIELD>",
                    access_url.getFieldDef());
        }

        @Test
        public void testBuildVotableFieldMap()
        {
            Map<String, Siap1Field> fields = siap1Service.readFields();
            Map<String, String> votableFieldMap = siap1Service.buildVotableFieldMap(fields);

            assertThat(votableFieldMap.keySet(),
                    containsInAnyOrder("scs|image_title", "ivoa|obscore|instrument_name", "ivoa|obscore|t_min",
                            "scs|s_ra", "scs|s_dec", "scs|n_axes", "scs|n_axis", "scs|image_scale", "scs|distance",
                            "scs|pix_flags", "scs|access_format", "scs|access_url", "scs|survey",
                            "scs|est_size_bytes"));

            assertEquals(
                    "<FIELD ID=\"image_scale\" name=\"image_scale\" datatype=\"double\" arraysize=\"*\" unit=\"deg\" "
                            + "ucd=\"VOX:Image_Scale\"><DESCRIPTION>Spatial resolution of data</DESCRIPTION> </FIELD>",
                    votableFieldMap.get("scs|image_scale"));
            assertEquals(
                    "<FIELD ID=\"access_url\" name=\"access_url\" datatype=\"char\" arraysize=\"*\" "
                            + "ucd=\"VOX:Image_AccessReference\">"
                            + "<DESCRIPTION>URL used to access dataset</DESCRIPTION> </FIELD>",
                    votableFieldMap.get("scs|access_url"));

        }
    }

}
