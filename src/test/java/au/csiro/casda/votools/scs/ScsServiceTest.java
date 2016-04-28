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


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.Log4JTestAppender;
import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.TestUtils;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationDAO;
import au.csiro.casda.votools.config.ConfigurationDAOImpl;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.scs.ConeSearchTable.Verbosity;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * Tests the VO TAP main endpoint Controller.
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class ScsServiceTest
{
    // use a non-standard IP addresses because of checkstyle, but doesn't matter for our purposes because
    // we are only using it in a string comparison
    private static final String TEST_IP_ADDRESS_TRUSTED_1 = "127.l.l.ll";
    private static final String TEST_IP_ADDRESS_TRUSTED_2 = "127.o.l.l";
    private static final String TEST_IP_ADDRESS_ANONYMOUS = "127.l.l.l";

    private TapSchema tapSchema;

    @Mock
    private VoTableRepositoryService voTableRepositoryService;

    private TapColumn idColumn;

    private TapColumn raColumn;

    private TapColumn decColumn;

    private Configuration config;

    @Autowired
    private ConfigurationRegistry configRegistry;

    private Log4JTestAppender testAppender;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();

        MockitoAnnotations.initMocks(this);
        when(voTableRepositoryService.isReady()).thenReturn(true);

        tapSchema = new TapSchema();
        tapSchema.setSchemaName("obscore");
        tapSchema.setTables(new ArrayList<>());
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        // This will also create a minimal configuration object
        ConfigurationDAOImpl dao = new ConfigurationDAOImpl(jdbcTemplate);
        config = dao.getConfig();
        EndPoint scsEndPoint = new EndPoint();
        scsEndPoint.setType(EndPoint.Type.SCS);
        scsEndPoint.addTable("casda.observation");
        scsEndPoint.addTable("casda.catalogue");
        scsEndPoint.put("max.radius", String.valueOf(17));
        config.addEndPoint("SCS", scsEndPoint);

        // This will switch the global current configuration to config
        try
        // Will throw exception when trying to save current configuration, but this happens
        // after actions necessary for testing have been finished
        {
            configRegistry.switchConfiguration(config, false);
        }
        catch (Exception e)
        {// Do something in braces
            scsEndPoint.put("exception", e.getMessage());
        }
    }

    @Test
    public void testValidateScsJob() throws InterruptedException, IOException, ConfigurationException
    {
        Map<String, String> params = new HashMap<>();

        List<TapTable> tableList = new ArrayList<>();
        TapTable scsTable =
                TestUtils.createTapTable("casda", "obs_core", tapSchema, tapSchema.getSchemaName() + ".ObsCore", true,
                        false);
        tableList.add(scsTable);
        when(voTableRepositoryService.getTables()).thenReturn(tableList);

        ScsService service = new ScsService(voTableRepositoryService, configRegistry);
        service.isReady();

        assertThat(service.validateScsJob(params), containsString("Missing radius parameter"));

        params.put("sr", "lots");
        assertThat(service.validateScsJob(params), containsString("Invalid radius parameter value: lots"));

        params.put("sr", "17.1");
        assertThat(service.validateScsJob(params), containsString("Invalid radius parameter value: 17.1"));

        params.put("sr", "16.9");
        assertThat(service.validateScsJob(params), containsString("Missing RA parameter"));

        params.put("ra", "-2");
        assertThat(service.validateScsJob(params), containsString("Invalid RA parameter value: -2"));

        params.put("ra", "361");
        assertThat(service.validateScsJob(params), containsString("Invalid RA parameter value: 361"));

        params.put("ra", "150");
        assertThat(service.validateScsJob(params), containsString("Missing DEC parameter"));

        params.put("dec", "-91");
        assertThat(service.validateScsJob(params), containsString("Invalid DEC parameter value: -91"));

        params.put("dec", "91");
        assertThat(service.validateScsJob(params), containsString("Invalid DEC parameter value: 91"));

        params.put("dec", "-3.482014e");
        assertThat(service.validateScsJob(params), containsString("Invalid DEC parameter value: -3.482014e"));

        params.put("dec", "85");
        assertThat(service.validateScsJob(params), containsString("Undefined catalog to search"));

        params.put("catalog", "XXX");
        assertThat(service.validateScsJob(params), containsString("Invalid catalog: XXX"));

        params.put("catalog", "obscore");
        assertNull(service.validateScsJob(params));
        params.put("catalog", "obSCOre");
        assertNull(service.validateScsJob(params));

        params.put("verb", "invalid");
        assertThat(service.validateScsJob(params), containsString("Invalid VERB parameter value: invalid"));

        params.put("verb", "0");
        assertThat(service.validateScsJob(params), containsString("Invalid VERB parameter value: 0"));

        params.put("verb", "4");
        assertThat(service.validateScsJob(params), containsString("Invalid VERB parameter value: 4"));
    }

    @Test
    public void testPrepareScsMetadataWithMain() throws InterruptedException, IOException, ConfigurationException
    {
        populateTapMetadata();

        ScsService service = new ScsService(voTableRepositoryService, configRegistry);
        service.isReady();
        Map<String, ConeSearchTable> scsMetadata = service.prepareScsMetadata();
        assertThat(scsMetadata.keySet().isEmpty(), is(false));
        assertThat(scsMetadata.keySet(), containsInAnyOrder("obscore", "continuum_component"));
        assertThat(scsMetadata.keySet().size(), is(2));

        ConeSearchTable cst = scsMetadata.get("obscore");
        assertThat(cst.getIdColumn(), is(idColumn));
        assertThat(cst.getRaColumn(), is(raColumn));
        assertThat(cst.getDecColumn(), is(decColumn));
        assertThat(cst.getSelectFields(Verbosity.LEVEL_1), is("obs_id,s_ra,s_dec"));
        assertThat(cst.getSelectFields(Verbosity.LEVEL_2), is("obs_id,s_ra,s_dec,instrument_name"));

        // Verify the field map
        Map<String, String> fieldMap = cst.getVotableFieldMap();
        assertThat(fieldMap.get("obs_core|obs_id").trim(),
                is("<FIELD name=\"obs_id\" ID=\"obs_id\" datatype=\"char\" arraysize=\"255\" ucd=\"ID_MAIN\" />"));
        assertThat(fieldMap.get("obs_core|s_ra").trim(),
                is("<FIELD name=\"s_ra\" ID=\"s_ra\" datatype=\"double\" ucd=\"POS_EQ_RA_MAIN\" />"));
        assertThat(fieldMap.get("obs_core|instrument_name").trim(),
                is("<FIELD name=\"instrument_name\" ID=\"instrument_name\" datatype=\"char\" "
                        + "arraysize=\"255\" ucd=\"meta.id;instr\" />"));
    }

    @Test
    public void testPrepareScsMetadataWithoutMain() throws InterruptedException, IOException, ConfigurationException
    {
        populateTapMetadata();
        idColumn.setUcd("meta.id");
        raColumn.setUcd("pos.eq.ra");
        decColumn.setUcd("pos.eq.dec");

        ScsService service = new ScsService(voTableRepositoryService, configRegistry);
        Map<String, ConeSearchTable> scsMetadata = service.prepareScsMetadata();
        assertThat(scsMetadata.keySet().isEmpty(), is(false));
        assertThat(scsMetadata.keySet(), containsInAnyOrder("obscore", "continuum_component"));
        assertThat(scsMetadata.keySet().size(), is(2));

        ConeSearchTable cst = scsMetadata.get("obscore");
        assertThat(cst.getIdColumn(), is(idColumn));
        assertThat(cst.getRaColumn(), is(raColumn));
        assertThat(cst.getDecColumn(), is(decColumn));
        assertThat(cst.getSelectFields(Verbosity.LEVEL_1), is("obs_id,s_ra,s_dec"));
        assertThat(cst.getSelectFields(Verbosity.LEVEL_2), is("obs_id,s_ra,s_dec,instrument_name"));

        // Verify the field map
        Map<String, String> fieldMap = cst.getVotableFieldMap();
        assertThat(fieldMap.get("obs_core|obs_id").trim(),
                is("<FIELD name=\"obs_id\" ID=\"obs_id\" datatype=\"char\" arraysize=\"255\" ucd=\"ID_MAIN\" />"));
        assertThat(fieldMap.get("obs_core|s_ra").trim(),
                is("<FIELD name=\"s_ra\" ID=\"s_ra\" datatype=\"double\" ucd=\"POS_EQ_RA_MAIN\" />"));
        assertThat(fieldMap.get("obs_core|instrument_name").trim(),
                is("<FIELD name=\"instrument_name\" ID=\"instrument_name\" datatype=\"char\" "
                        + "arraysize=\"255\" ucd=\"meta.id;instr\" />"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessQuery() throws Exception
    {
        populateTapMetadata();
        ScsService service = new ScsService(voTableRepositoryService, configRegistry);
        service.isReady();

        String scsParams = "sr=0.5&ra=149.0&dec=-63.0&verb-1";
        String userId = "abc111";

        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.CATALOG, "obscore");
        params.put("sr", "0.5");
        params.put("ra", "149.0");
        params.put("dec", "-63.0");
        params.put("verb", "1");
        params.put(VoKeys.PARAM_QUERY_STRING, scsParams);
        params.put(VoKeys.USER_ID, userId);

        StringWriter writer = new StringWriter();

        service.processQuery(writer, params);

        assertThat(writer.toString(), is(""));
        verify(config.gtDao().getTemplate())
                .query(eq("SELECT obs_id,s_ra,s_dec FROM casda.obs_core WHERE '1' = "
                        + "(spoint(radians(s_ra),radians(s_dec)) @ scircle(spoint(radians(?),radians(?)),radians(?)))"),
                        eq(new Object[] { 149.0, -63.0, 0.5 }), (ResultSetExtractor<Boolean>) any());
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E144]"), containsString("duration:"), containsString("scsTable: obscore"),
                        containsString("scsParams: " + scsParams), containsString("startTime: "
                                + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                        containsString("endTime: " + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                        containsString("resultsSize: 0"), containsString("totalResults: 0"),
                        containsString("cutoffReason: none"), containsString("userId: " + userId)), (Throwable) null);

    }

    @Test
    public void testProcessQueryValidateFailsLogsInfo() throws Exception
    {
        ScsService service = spy(new ScsService(voTableRepositoryService, configRegistry));
        service.isReady();

        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.CATALOG, "obscore");
        params.put(VoKeys.PARAM_QUERY_STRING, "scsParamDetails");
        params.put(VoKeys.USER_ID, "userIdent");

        doReturn("Scs validation error").when(service).validateScsJob(params);

        StringWriter writer = new StringWriter();
        assertFalse(service.processQuery(writer, params));

        testAppender
                .verifyLogMessage(
                        Level.INFO,
                        allOf(containsString("E143]"),

                        containsString("scsTable: obscore"), containsString("scsParams: scsParamDetails"),
                                containsString("startTime: "
                                        + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                                containsString("userMessage: Scs validation error"),
                                containsString("userId: userIdent")), (Throwable) null);
    }

    @Test
    public void testProcessQueryProcessFailsLogsInfo() throws Exception
    {
        ScsService service = spy(new ScsService(voTableRepositoryService, configRegistry));
        service.isReady();

        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.CATALOG, "obscore");
        params.put(VoKeys.PARAM_QUERY_STRING, "scsParamDetails");
        params.put(VoKeys.USER_ID, "userIdent");

        doReturn(null).when(service).validateScsJob(params);
        doThrow(new IOException("some problem")).when(service).runScsQuery(any(), anyString(), any(Writer.class),
                anyInt(), any(ZonedDateTime.class));

        StringWriter writer = new StringWriter();
        assertFalse(service.processQuery(writer, params));

        testAppender
                .verifyLogMessage(
                        Level.INFO,
                        allOf(containsString("E142]"),

                        containsString("scsTable: obscore"), containsString("scsParams: scsParamDetails"),
                                containsString("startTime: "
                                        + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                                containsString("userMessage: Unable to run query: some problem"),
                                containsString("userId: userIdent")), IOException.class, containsString("some problem"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessReleaseRequiredQuery() throws Exception
    {
        populateTapMetadata();
        ScsService service = new ScsService(voTableRepositoryService, configRegistry);
        service.isReady();

        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.CATALOG, "continuum_component");
        params.put("sr", "0.5");
        params.put("ra", "149.0");
        params.put("dec", "-63.0");
        params.put("verb", "1");
        params.put(VoKeys.USER_ID, VoKeys.ANONYMOUS_USER);
        StringWriter writer = new StringWriter();

        service.processQuery(writer, params);

        assertThat(writer.toString(), is(""));
        verify(config.gtDao().getTemplate()).query(
                eq("SELECT obs_id,s_dec,s_ra FROM casda.continuum_component WHERE '1' = "
                        + "(spoint(radians(s_ra),radians(s_dec)) @ scircle(spoint(radians(?),radians(?)),radians(?)))"
                        + " AND released_date is not null"), eq(new Object[] { 149.0, -63.0, 0.5 }),
                (ResultSetExtractor<Boolean>) any());
        testAppender
                .verifyLogMessage(
                        Level.INFO,
                        allOf(containsString("E144]"),
                                containsString("duration:"),
                                containsString("scsTable: continuum_component"),
                                containsString("scsParams: null"),
                                containsString("startTime: "
                                        + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                                containsString("endTime: "
                                        + CasdaFormatter.formatDateTimeForLog(new Date()).substring(0, 8)),
                                containsString("resultsSize: 0"), containsString("totalResults: 0"),
                                containsString("cutoffReason: none"),
                                containsString("userId: " + VoKeys.ANONYMOUS_USER)), (Throwable) null);
    }

    private void populateTapMetadata()
    {
        List<TapTable> tableList = new ArrayList<>();
        TapTable scsTable =
                TestUtils.createTapTable("casda", "obs_core", tapSchema, tapSchema.getSchemaName() + ".ObsCore", true,
                        false);
        tableList.add(scsTable);
        TapTable continuumComponentTable =
                TestUtils.createTapTable("casda", "continuum_component", tapSchema, tapSchema.getSchemaName()
                        + ".continuum_component", true, true);
        tableList.add(continuumComponentTable);
        TapTable nonScsTable =
                TestUtils.createTapTable("casda", "job", tapSchema, tapSchema.getSchemaName() + ".Job", false, false);
        tableList.add(nonScsTable);

        TapTable tableCopy = new TapTable();
        tableCopy.setTableName(scsTable.getTableName());
        tableCopy.setDbTableName(scsTable.getDbTableName());

        List<TapColumn> columnList = new ArrayList<>();
        idColumn = TestUtils.createTapColumn(scsTable, "obs_id", "VARCHAR", 255, "meta.id;meta.main", 1, 1);
        columnList.add(idColumn);
        raColumn = TestUtils.createTapColumn(scsTable, "s_ra", "DOUBLE", 15, "pos.eq.ra;meta.main", 1, 2);
        columnList.add(raColumn);
        decColumn = TestUtils.createTapColumn(scsTable, "s_dec", "DOUBLE", 15, "pos.eq.dec;meta.main", 1, 3);
        columnList.add(decColumn);
        TapColumn instrumentColumn =
                TestUtils.createTapColumn(scsTable, "instrument_name", "VARCHAR", 255, "meta.id;instr", 2, 4);
        columnList.add(instrumentColumn);
        TapColumn facilityColumn =
                TestUtils.createTapColumn(scsTable, "facility_name", "VARCHAR", 255, "meta.id;instr.tel", 3, 5);
        columnList.add(facilityColumn);
        TapColumn accessurlColumn =
                TestUtils.createTapColumn(scsTable, "access_url", "CLOB", 2000, "meta.ref.url", null, 6);
        columnList.add(accessurlColumn);
        TapColumn nonScsColumn =
                TestUtils.createTapColumn(nonScsTable, "job_id", "LONG", 15, "meta.id;meta.main", null, 7);
        columnList.add(nonScsColumn);

        TapColumn ccIdColumn =
                TestUtils.createTapColumn(continuumComponentTable, "obs_id", "VARCHAR", 255, "meta.id;meta.main", 1, 0);
        columnList.add(ccIdColumn);
        TapColumn ccRaColumn =
                TestUtils.createTapColumn(continuumComponentTable, "s_ra", "DOUBLE", 15, "pos.eq.ra;meta.main", 1, 0);
        columnList.add(ccRaColumn);
        TapColumn ccDecColumn =
                TestUtils.createTapColumn(continuumComponentTable, "s_dec", "DOUBLE", 15, "pos.eq.dec;meta.main", 1, 0);
        columnList.add(ccDecColumn);

        when(voTableRepositoryService.getTables()).thenReturn(tableList);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList);
    }

    @Test
    public void testIsAuthorisedRequest() throws Exception
    {
        // initialise the tap service
        ScsService scsService =
                spy(new ScsService(voTableRepositoryService, mock(ConfigurationRegistry.class)));
        Configuration config = spy(new Configuration());
        scsService.setConfiguration(config);
        ConfigurationDAO mockConfigDAO = mock(ConfigurationDAO.class);
        when(config.gtDao()).thenReturn(mockConfigDAO);
        JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
        when(mockConfigDAO.getTemplate()).thenReturn(mockTemplate);
        DataSource mockDataSource = mock(DataSource.class);
        when(mockTemplate.getDataSource()).thenReturn(mockDataSource);
        config.setDao(mock(ConfigurationDAO.class));

        config.put("auth.trusted.ip", "  " + TEST_IP_ADDRESS_TRUSTED_1 + "   ,   " + TEST_IP_ADDRESS_TRUSTED_2 + " ");
        scsService.isReady();

        HttpServletRequest requestFromAuthIp = mock(HttpServletRequest.class);
        when(requestFromAuthIp.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_1);
        assertTrue(scsService.trustAuthHeader(requestFromAuthIp));

        HttpServletRequest requestFromAnonymous = mock(HttpServletRequest.class);
        when(requestFromAnonymous.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_ANONYMOUS);
        assertFalse(scsService.trustAuthHeader(requestFromAnonymous));
    }
}
