package au.csiro.casda.votools.tap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import adql.parser.ParseException;
import au.csiro.casda.Log4JTestAppender;
import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationDAO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.tap.TapService.TapStatementCreator;
import au.csiro.casda.votools.utils.VoKeys;

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

/**
 * Unit tests of the TapService functions.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class TapServiceTest
{
    private static final String STR_MSG_UNKNOWN_QUERY_LANGUAGE = "Unknown query language";
    private static final String STR_MSG_MISSING_QUERY_PARAMETER = "Missing QUERY parameter";
    private static final String STR_BAD_QUERY_SELECTSELECT = "selectselect * from dsfdfdasf";
    private static final String STR_QUERY_SELECT_STAR_FROM_DSFDFDASF = "select * from dsfdfdasf";
    private static final String STR_MSG_IO_PROBLEM = "IO Problem";
    private static final String STR_BOGUS_FORMAT_XXX = "XXX";
    private static final String STR_UNSUPPORTED_FORMAT_REQUESTED_XXX = "Unsupported FORMAT requested: XXX";

    // use a non-standard IP addresses because of checkstyle, but doesn't matter for our purposes because
    // we are only using it in a string comparison
    private static final String TEST_IP_ADDRESS_TRUSTED_1 = "127.l.l.ll";
    private static final String TEST_IP_ADDRESS_TRUSTED_2 = "127.o.l.l";
    private static final String TEST_IP_ADDRESS_ANONYMOUS = "127.l.l.l";

    private static final String LOG_TIMEZONE = "UTC";
    private static final String PROJECT_CODE_SAMPLE = "C002,A013,C007";
    private static final List<Long> PROJECT_IDS_SAMPLE = Arrays.asList(1l, 2l, 3l);
    private static final ZonedDateTime SUBMITTED = ZonedDateTime.now(ZoneId.of(LOG_TIMEZONE));
    private static final ZonedDateTime STARTED = SUBMITTED.plusHours(1);
    private static final String SUBMITTED_FORMATTED = CasdaFormatter.formatDateTimeForLog(Date.from(SUBMITTED
            .toInstant()));
    private static final String STARTED_FORMATTED = CasdaFormatter.formatDateTimeForLog(Date.from(STARTED.toInstant()));
    private static final String KNOWN_USER = "aaa111";
    private static final String STR_FORMAT_TSV = "tsv";

    private static final String TRUSTED_USER_ID = "4310";
    
    @Autowired
    private ConfigurationRegistry configRegistry;

    @Mock
    private VoTableRepositoryService voTableRepositoryService;
    
    @Mock
    private JdbcTemplate syncJdbcTemplate;
    
    @Mock
    private JdbcTemplate asyncJdbcTemplate;

    private TapService tapService;

    private Log4JTestAppender testAppender;
    
    private List<TapTable> tableList;
    private List<TapColumn> columnList;
    private TapTable tableCopy;

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
        TapSchema tapSchema = new TapSchema();
        tapSchema.setSchemaName("ivoa");

        tableList = new ArrayList<>();
        TapTable table = new TapTable();
        table.setDbSchemaName("casda");
        table.setDbTableName("obs_core");
        table.setSchema(tapSchema);
        table.setTableName("ivoa.ObsCore");
        tableList.add(table);

        tableCopy = new TapTable();
        tableCopy.setDbSchemaName(table.getDbSchemaName());
        tableCopy.setTableName(table.getTableName());
        tableCopy.setDbTableName(table.getDbTableName());
        tableCopy.setDescriptionLong("This is very very very long description");
        tableCopy.setParams("Table Name : asdasd | Indexed Fields : blabl, bla and bla");

        columnList = new ArrayList<>();
        TapColumn tapColumn = new TapColumn();
        tapColumn.setTable(tableCopy);
        tapColumn.setId(new TapColumnPK(table.getTableName(), "dataProduct_type"));
        tapColumn.setDatatype("VARCHAR");
        tapColumn.setSize(255);
        tapColumn.setDbColumnName("dataproduct_type_db");
        columnList.add(tapColumn);

        when(voTableRepositoryService.getTables()).thenReturn(tableList);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList);
        when(voTableRepositoryService.isReady()).thenReturn(true);
        when(voTableRepositoryService.getTableByName(any(String.class))).thenReturn(tableCopy);

        Configuration config = ConfigurationTest.getTestConfiguration();
        EndPoint tapEndPoint = config.getEndPoint("TAP");
        tapEndPoint.put("log.timezone", LOG_TIMEZONE);
        configRegistry.switchConfiguration(config, false);
        config.put("auth.trusted.ip", TEST_IP_ADDRESS_TRUSTED_1);

        tapService = spy(new TapService(configRegistry, voTableRepositoryService));
        tapService.setConfiguration(configRegistry.getCurrent());
        doReturn(STARTED).when(tapService).now();
        doReturn(true).when(tapService).isReady();

        tapService.setJdbcTemplateSync(syncJdbcTemplate);
        tapService.setJdbcTemplateAsync(asyncJdbcTemplate);

        List<String> projectCodes = new ArrayList<String>();
        projectCodes.addAll(Arrays.asList(PROJECT_CODE_SAMPLE.replace(" ", "").split(",")));
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(projectCodes), 
                anyString())).thenReturn(PROJECT_IDS_SAMPLE);
    }

    /**
     * Test method for {@link au.csiro.casda.votools.tap.TapService#getFormat(java.lang.String)}.
     */
    @Test
    public void testGetFormat()
    {
        assertThat(tapService.getFormat("text/xml"), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat("text/XML"), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat("application/x-votable+xml"), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat("votable"), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat("csv"), is(OutputFormat.CSV));
        assertThat(tapService.getFormat("text/csv"), is(OutputFormat.CSV));
        assertThat(tapService.getFormat("tsv"), is(OutputFormat.TSV));
        assertThat(tapService.getFormat("text/tab-separated-values"), is(OutputFormat.TSV));
        assertThat(tapService.getFormat("foo"), nullValue());
        assertThat(tapService.getFormat(""), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat(" "), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat(null), is(OutputFormat.VOTABLE));
        // check file extensions set correctly
        assertThat(tapService.getFormat("votable").getFileExtension(), is("xml"));
    }

    /**
     * Test method for {@link au.csiro.casda.votools.tap.TapService#generateSqlForQuery(java.lang.String)}.
     * 
     * @throws Exception
     *             Not expected.
     */

    /**
     * Test method for {@link TapService#createVotableFieldMap()} ()}.
     * @throws ConfigurationException if there were configuration problems
     */
    @Test
    public void testCreateVotableFieldMap() throws ConfigurationException
    {
        Map<String, String> votableFieldMap = tapService.createVotableFieldMap();
        assertThat(votableFieldMap.get(""), nullValue());
        assertThat(votableFieldMap.keySet(), containsInAnyOrder("casda|obs_core|dataproduct_type"));
        assertThat(votableFieldMap.get("casda|obs_core|dataproduct_type"),
                is("<FIELD name=\"dataProduct_type\" ID=\"dataProduct_type\" "
                        + "datatype=\"char\" arraysize=\"255\" />\r\n"));

    }

    private Map<String, String> createValidParamsForUser(String mode, String query, boolean known)
    {
        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.SUBMITTED_TIME, SUBMITTED.toString());
        params.put(VoKeys.SUBMITTED_MODE, mode);
        params.put(VoKeys.STR_KEY_ADQL_QUERY, query);
        params.put(TapService.STR_KEY_LANG, "ADQL");
        if (known)
        {
            params.put(VoKeys.USER_ID, KNOWN_USER);
            params.put(VoKeys.USER_PROJECTS, PROJECT_CODE_SAMPLE);
            params.put(VoKeys.KEY_REQUESTER_IP_ADDRESS, TEST_IP_ADDRESS_TRUSTED_2);
        }
        else
        {
            params.put(VoKeys.USER_ID, VoKeys.ANONYMOUS_USER);
            params.put(VoKeys.USER_PROJECTS, StringUtils.EMPTY);
            params.put(VoKeys.KEY_REQUESTER_IP_ADDRESS, TEST_IP_ADDRESS_ANONYMOUS);
        }
        return params;
    }

    @Test
    public void testProcessQueryMissingLanguage() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, false);
        params.remove(TapService.STR_KEY_LANG);

        assertThat(tapService.processQuery(writer, params), is(false));
        assertThat(writer.toString(), containsString(STR_MSG_UNKNOWN_QUERY_LANGUAGE));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E061]"), containsString("adqlQuery: \"" + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF
                        + "\""), containsString("submittedTime: " + SUBMITTED_FORMATTED), containsString("startTime: "
                        + STARTED_FORMATTED), containsString("mode: " + mode), containsString("userId: anonymous"),
                        containsString(STR_MSG_UNKNOWN_QUERY_LANGUAGE)), (Throwable) null);
    }

    @Test
    public void testProcessQueryUnknownLanguage() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, false);
        params.put(TapService.STR_KEY_LANG, "random");

        assertThat(tapService.processQuery(writer, params), is(false));
        assertThat(writer.toString(), containsString(STR_MSG_UNKNOWN_QUERY_LANGUAGE));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E061]"), containsString("adqlQuery: \"" + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF
                        + "\""), containsString("submittedTime: " + SUBMITTED_FORMATTED), containsString("startTime: "
                        + STARTED_FORMATTED), containsString("mode: " + mode), containsString("userId: anonymous"),
                        containsString(STR_MSG_UNKNOWN_QUERY_LANGUAGE)), (Throwable) null);
    }

    @Test
    public void testProcessQueryInvalidFormat() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, false);
        params.put(TapService.STR_KEY_FORMAT, STR_BOGUS_FORMAT_XXX);

        writer = new StringWriter();
        assertThat(tapService.processQuery(writer, params), is(false));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        assertThat(writer.toString(), containsString(STR_UNSUPPORTED_FORMAT_REQUESTED_XXX));
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E061]"), containsString("adqlQuery: \"" + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF
                        + "\""), containsString("submittedTime: " + SUBMITTED_FORMATTED), containsString("startTime: "
                        + STARTED_FORMATTED), containsString("mode: " + mode), containsString("userId: anonymous"),
                        containsString(STR_UNSUPPORTED_FORMAT_REQUESTED_XXX)), (Throwable) null);
    }

    @Test
    public void testProcessQueryMissingQuery() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_SYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, StringUtils.EMPTY, false);
        params.remove(VoKeys.STR_KEY_ADQL_QUERY);

        assertThat(tapService.processQuery(writer, params), is(false));
        assertThat(writer.toString(), containsString(STR_MSG_MISSING_QUERY_PARAMETER));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E061]"), containsString("adqlQuery: \"null\""), containsString("submittedTime: "
                        + SUBMITTED_FORMATTED), containsString("startTime: " + STARTED_FORMATTED),
                        containsString("mode: " + mode), containsString("userId: anonymous"),
                        containsString(STR_MSG_MISSING_QUERY_PARAMETER)), (Throwable) null);
    }

    @Test
    public void testProcessQueryInvalidAdqlQuery() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_BAD_QUERY_SELECTSELECT, false);
        writer = new StringWriter();
        assertThat(tapService.processQuery(writer, params), is(false));
        // no results due to mock JdbcTemplate
        assertThat(writer.toString(), containsString(StringUtils.EMPTY));

        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E061]"), containsString("adqlQuery: \"" + STR_BAD_QUERY_SELECTSELECT + "\""),
                        containsString("submittedTime: " + SUBMITTED_FORMATTED), containsString("startTime: "
                                + STARTED_FORMATTED), containsString("mode: " + mode),
                        containsString("userId: anonymous")), ParseException.class, containsString("Encountered "));
        verify(tapService, times(1)).generateSqlForQuery(eq(STR_BAD_QUERY_SELECTSELECT), eq(false), eq(null));

    }

    @Test
    public void testProcessQuerySuccessfulAdqlQueryFromAnonymousUser() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, false);
        params.put(TapService.STR_KEY_FORMAT, STR_FORMAT_TSV);

        assertThat(tapService.processQuery(writer, params), is(true));
        // calls generate sql for query once to validate, and once to run
        verify(tapService, times(2)).generateSqlForQuery(eq(STR_QUERY_SELECT_STAR_FROM_DSFDFDASF), eq(false), eq(null));
        // no results due to mock JdbcTemplate
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        assertThat(writer.toString(), containsString(""));
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E062]"), containsString("duration:"), containsString("adqlQuery: \""
                        + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF + "\""), containsString("submittedTime: "
                        + SUBMITTED_FORMATTED), containsString("startTime: " + STARTED_FORMATTED),
                        containsString("resultsSize: 0"), containsString("totalResults: 0"),
                        containsString("cutoffReason: none"), containsString("mode: " + mode),
                        containsString("userId: anonymous")), (Throwable) null);

    }

    @Test
    public void testProcessQuerySuccessfulAdqlQueryFromKnownUser() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, true);

        assertThat(tapService.processQuery(writer, params), is(true));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        verify(tapService, times(2)).generateSqlForQuery(eq(STR_QUERY_SELECT_STAR_FROM_DSFDFDASF), eq(false),
                eq(PROJECT_IDS_SAMPLE));
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E062]"), containsString("duration:"), containsString("adqlQuery: \""
                        + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF + "\""), containsString("submittedTime: "
                        + SUBMITTED_FORMATTED), containsString("startTime: " + STARTED_FORMATTED),
                        containsString("resultsSize: 0"), containsString("totalResults: 0"),
                        containsString("cutoffReason: none"), containsString("mode: " + mode),
                        containsString("userId: " + KNOWN_USER)), (Throwable) null);
    }

    @Test
    public void testProcessQuerySuccessfulAdqlQueryFromKnownAdminUser() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, true);
        params.put(VoKeys.USER_PROJECTS, VoKeys.STR_PROJECT_CODES_ALL);

        assertThat(tapService.processQuery(writer, params), is(true));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        verify(tapService, times(2)).generateSqlForQuery(eq(STR_QUERY_SELECT_STAR_FROM_DSFDFDASF), eq(true), eq(null));
        testAppender.verifyLogMessage(
                Level.INFO,
                allOf(containsString("E062]"), containsString("duration:"), containsString("adqlQuery: \""
                        + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF + "\""), containsString("submittedTime: "
                        + SUBMITTED_FORMATTED), containsString("startTime: " + STARTED_FORMATTED),
                        containsString("resultsSize: 0"), containsString("totalResults: 0"),
                        containsString("cutoffReason: none"), containsString("mode: " + mode),
                        containsString("userId: " + KNOWN_USER)), (Throwable) null);
    }

    @Test
    public void testProcessQueryIOExceptionRunningQuery() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, true);

        doThrow(new IOException(STR_MSG_IO_PROBLEM)).when(tapService).runTapQuery(any(String.class),
                any(OutputFormat.class), any(Writer.class), any(Integer.class), anyMapOf(String.class, String.class),
                any(ZonedDateTime.class), any(String.class), anyMapOf(String.class, String[].class));
        assertThat(tapService.processQuery(writer, params), is(false));
        // no results due to mock JdbcTemplate
        assertThat(writer.toString(), containsString(StringUtils.EMPTY));
        verify(tapService, times(2)).generateSqlForQuery(eq(STR_QUERY_SELECT_STAR_FROM_DSFDFDASF), eq(false),
                eq(PROJECT_IDS_SAMPLE));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender
                .verifyLogMessage(
                        Level.ERROR,
                        allOf(containsString("E060]"), containsString("adqlQuery: \""
                                + STR_QUERY_SELECT_STAR_FROM_DSFDFDASF + "\""), containsString("submittedTime: "
                                + SUBMITTED_FORMATTED), containsString("startTime: " + STARTED_FORMATTED),
                                containsString("mode: " + mode), containsString("userId: " + KNOWN_USER)),
                        IOException.class, containsString("IO Problem"));
    }

    @Test
    public void testProcessQueryTimeout() throws Exception
    {
        String mode = TapService.SUBMITTED_MODE_ASYNC;
        StringWriter writer = new StringWriter();
        Map<String, String> params = createValidParamsForUser(mode, STR_QUERY_SELECT_STAR_FROM_DSFDFDASF, true);

        DataAccessResourceFailureException dataAccessResourceFailureException =
                new DataAccessResourceFailureException("canceling statement due to user request");
        doThrow(dataAccessResourceFailureException).when(tapService).runTapQuery(any(String.class),
                any(OutputFormat.class), any(Writer.class), any(Integer.class), anyMapOf(String.class, String.class),
                any(ZonedDateTime.class), any(String.class), anyMapOf(String.class, String[].class));
        assertThat(tapService.processQuery(writer, params), is(false));
        assertThat(writer.toString(), containsString("Could not finish query due to timeout."));
        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(Level.ERROR, "Could not finish query due to timeout.",
                dataAccessResourceFailureException);

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProcessQueryVoTableHeader() throws Exception
    {
        Writer writer = new StringWriter();
        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.VO_TABLE_HEADING, "Vo heading");
        params.put(VoKeys.SUBMITTED_TIME, ZonedDateTime.now().toString());
        params.put(VoKeys.SUBMITTED_MODE, TapService.SUBMITTED_MODE_SYNC);
        when(syncJdbcTemplate.query(any(TapStatementCreator.class), any(ResultSetExtractor.class)))
                .thenAnswer(new Answer<Boolean>()
                {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable
                    {
                        ResultSetExtractor<Boolean> extractor = invocation.getArgumentAt(1, ResultSetExtractor.class);
                        ResultSet emptyResultSet = mock(ResultSet.class);
                        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
                        when(emptyResultSet.getMetaData()).thenReturn(metadata);
                        when(metadata.getColumnCount()).thenReturn(0);
                        return extractor.extractData(emptyResultSet);
                    }
                });
        tapService.runTapQuery("sqlQuery", OutputFormat.VOTABLE, writer, 10, params, ZonedDateTime.now(), null, null);

        assertThat(writer.toString(), containsString("Vo heading"));
    }
    

    @Test
    public void testGetOutputFormat()
    {
        TapService tapService =
                new TapService(mock(ConfigurationRegistry.class),
                        mock(VoTableRepositoryService.class));
        assertThat(tapService.getFormat(null), is(OutputFormat.VOTABLE));
        assertThat(tapService.getFormat("blah/blah.xml"), is(nullValue()));
        assertThat(tapService.getFormat("CSV"), is(OutputFormat.CSV));
    }

    @Test
    public void testLogFormatWithNulls()
    {
        String resultE060 =
                CasdaVoToolsEvents.E060.messageBuilder().addTimeTaken(10)
                        .addAll(Arrays.asList(null, null, null, null, null, null)).toString();
        String expectedE060 =
                "[duration:10] [E060] [Could not execute VO query] [adqlQuery: \"null\"] "
                        + "[submittedTime: null] [startTime: null] [mode: null] "
                        + "[userMessage: Could not execute VO query] [userId: null] ";
        assertEquals(expectedE060, resultE060);

        String resultE061 =
                CasdaVoToolsEvents.E061.messageBuilder().addTimeTaken(10)
                        .addAll(Arrays.asList(null, null, null, null, null, null)).toString();
        String expectedE061 =
                "[duration:10] [E061] [Invalid VO query] [adqlQuery: \"null\"] [submittedTime: null] "
                        + "[startTime: null] [mode: null] [userMessage: Invalid VO query] " + "[userId: null] ";
        assertEquals(expectedE061, resultE061);

        String resultE062 =
                CasdaVoToolsEvents.E062.messageBuilder().addTimeTaken(10)
                        .addAll(Arrays.asList(null, null, null, null, null, null, null, null, null, null)).toString();
        String expectedE062 =
                "[duration:10] [E062] [Successful VO query] [adqlQuery: \"null\"] [submittedTime: null] "
                        + "[startTime: null] [endTime: null] [resultsSize: null] [totalResults: null] "
                        + "[cutoffReason: null] [mode: null] [userId: null] ";
        assertEquals(expectedE062, resultE062);

        String resultE098 =
                CasdaVoToolsEvents.E098.messageBuilder().addTimeTaken(10)
                        .addAll(Arrays.asList(null, null, null, null, null, null)).toString();
        String expectedE098 =
                "[duration:10] [E098] [Unexpected exception occurred] [adqlQuery: \"null\"] "
                        + "[submittedTime: null] [startTime: null] [mode: null] "
                        + "[userMessage: Unexpected exception occurred] [userId: null] ";
        assertEquals(expectedE098, resultE098);

    }

    // CASDA-4489 - vo tools keeps a cache of known tables/columns.
    // Any changes to the DB structure made using Configuration DAO invalidate this cache and
    // it is completely re-read when isReady() is invoked.
    @Test
    public void testValidateRefreshes() throws ConfigurationException
    {
        String rightQuery = "Select * from unknown.table";
        String wrongQuery = "Select * from wrong.table";

        Map<String, String> params = new HashMap<>();
        params.put(VoKeys.SUBMITTED_MODE, "sync");
        params.put(VoKeys.SUBMITTED_TIME, Instant.now().toString());
        params.put("query", rightQuery);

        // initialise the tap service
        TapService tapService = spy(new TapService(configRegistry, voTableRepositoryService));
        doReturn(true).when(tapService).isReady();
        doReturn("UTC").when(tapService).getLogTimezone();
        tapService.init();

        // set up the new table and column
        TapSchema tapSchema = new TapSchema();
        tapSchema.setSchemaName("unknown");

        List<TapTable> tableList2 = new ArrayList<>();
        TapTable table2 = new TapTable();
        table2.setDbSchemaName("unknown");
        table2.setDbTableName("table");
        table2.setSchema(tapSchema);
        table2.setTableName("unknown.table");
        tableList2.add(table2);

        List<TapColumn> columnList2 = new ArrayList<>();
        TapColumn tapColumn2 = new TapColumn();
        tapColumn2.setTable(table2);
        tapColumn2.setId(new TapColumnPK(table2.getTableName(), "colname"));
        tapColumn2.setDbColumnName("colname");
        tapColumn2.setDatatype("VARCHAR");
        tapColumn2.setSize(255);
        columnList2.add(tapColumn2);

        // make sure the new table and column are returned
        when(voTableRepositoryService.getTables()).thenReturn(tableList2);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList2);

        StringWriter writer = new StringWriter();

        tapService.createDbChecker();
        boolean result =
                tapService.validateQuery(true, rightQuery, params, writer, ZonedDateTime.now(), PROJECT_IDS_SAMPLE);
        assertTrue(result);
        assertThat(writer.toString(), not(containsString("Unknown table &quot;unknown.table&quot;")));
        tapService.refresh();
        when(voTableRepositoryService.getTables()).thenReturn(tableList2);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList2);
        tapService.createDbChecker();
        result = tapService.validateQuery(true, wrongQuery, params, writer, ZonedDateTime.now(), PROJECT_IDS_SAMPLE);
        assertFalse(result);
        assertThat(writer.toString(), containsString("Unknown table &quot;wrong.table&quot;"));

        verify(tapService, times(2)).createDbChecker();
    }

    @Test
    public void testIsAuthorisedRequest() throws Exception
    {
        // initialise the tap service
        TapService tapService =
                spy(new TapService(mock(ConfigurationRegistry.class), voTableRepositoryService));
        Configuration config = spy(new Configuration());
        tapService.setConfiguration(config);
        ConfigurationDAO mockConfigDAO = mock(ConfigurationDAO.class);
        when(config.gtDao()).thenReturn(mockConfigDAO);
        JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
        when(mockConfigDAO.getTemplate()).thenReturn(mockTemplate);
        DataSource mockDataSource = mock(DataSource.class);
        when(mockTemplate.getDataSource()).thenReturn(mockDataSource);
        tapService.getConfig().setDao(mock(ConfigurationDAO.class));

        tapService.getConfig().put("auth.trusted.ip", TEST_IP_ADDRESS_TRUSTED_1 + " , " + TEST_IP_ADDRESS_TRUSTED_2);
        tapService.init();
        tapService.isReady();

        HttpServletRequest requestFromAuthIp = mock(HttpServletRequest.class);
        when(requestFromAuthIp.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_2);
        assertTrue(tapService.trustAuthHeader(requestFromAuthIp));

        HttpServletRequest requestFromAnonymous = mock(HttpServletRequest.class);
        when(requestFromAnonymous.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_ANONYMOUS);
        assertFalse(tapService.trustAuthHeader(requestFromAnonymous));
    }

    /**
     * test trusteduserId from request header VoKeys.VO_AUTH_HEADER_USER_ID
     * 
     * @throws Exception
     */
    @Test
    public void testTrustedUserId() throws Exception
    {
        // initialise the tap service
        TapService tapService =
                spy(new TapService(mock(ConfigurationRegistry.class), voTableRepositoryService));
        Configuration config = spy(new Configuration());
        tapService.setConfiguration(config);
        ConfigurationDAO mockConfigDAO = mock(ConfigurationDAO.class);
        when(config.gtDao()).thenReturn(mockConfigDAO);
        JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
        when(mockConfigDAO.getTemplate()).thenReturn(mockTemplate);
        DataSource mockDataSource = mock(DataSource.class);
        when(mockTemplate.getDataSource()).thenReturn(mockDataSource);
        tapService.getConfig().setDao(mock(ConfigurationDAO.class));

        tapService.getConfig().put("auth.trusted.ip", TEST_IP_ADDRESS_TRUSTED_1 + " , " + TEST_IP_ADDRESS_TRUSTED_2);
        tapService.getConfig().put("auth.trusted.userId", TRUSTED_USER_ID);
        tapService.init();
        tapService.isReady();
        
        //test
        HttpServletRequest requestFromAuth = mock(HttpServletRequest.class);
        when(requestFromAuth.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_2);
        when(requestFromAuth.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)).thenReturn(TRUSTED_USER_ID);
        assertTrue(tapService.isTrustedUserId(requestFromAuth));
        
        HttpServletRequest requestFromNullUser = mock(HttpServletRequest.class);
        when(requestFromNullUser.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_2);
        when(requestFromNullUser.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)).thenReturn(null);
        assertFalse(tapService.isTrustedUserId(requestFromNullUser));
        
        HttpServletRequest requestFromAuthUser = mock(HttpServletRequest.class);
        when(requestFromAuthUser.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_2);
        when(requestFromAuthUser.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)).thenReturn(TRUSTED_USER_ID + "00");
        assertFalse(tapService.isTrustedUserId(requestFromAuthUser));
        
        HttpServletRequest requestFromEmptyUser = mock(HttpServletRequest.class);
        when(requestFromEmptyUser.getRemoteAddr()).thenReturn(TEST_IP_ADDRESS_TRUSTED_2);
        when(requestFromEmptyUser.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)).thenReturn("");
        assertFalse(tapService.isTrustedUserId(requestFromEmptyUser));
    }
    
    @Test
    public void testGetObsCoreVersion()
    {
        // With the default config of a single ivoa.obscore table we expect obscore 1.0 
        assertThat(tapService.getObsCoreVersion(), is("1.0"));
        
        // Add an obscore 1.1 column
        TapColumn tapColumn = new TapColumn();
        tapColumn.setTable(tableCopy);
        tapColumn.setId(new TapColumnPK(tableCopy.getTableName(), "s_xel1"));
        tapColumn.setDatatype("INTEGER");
        tapColumn.setSize(8);
        tapColumn.setDbColumnName("s_xel1");
        columnList.add(tapColumn);
        assertThat(tapService.getObsCoreVersion(), is("1.1"));

        // Hide all the tables 
        when(voTableRepositoryService.getTables()).thenReturn(new ArrayList<>());
        assertThat(tapService.getObsCoreVersion(), is(nullValue()));
    }
}
