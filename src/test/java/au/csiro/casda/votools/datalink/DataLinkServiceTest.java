package au.csiro.casda.votools.datalink;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import au.csiro.casda.Log4JTestAppender;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;

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
 * 
 * Tests for Data Link Service
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class DataLinkServiceTest
{
    @Autowired
    private ConfigurationRegistry configRegistry;

    private DataLinkService dataLinkService;

    @Mock
    private VoTableRepositoryService voTableRepositoryService;

    private static final List<String> PROJECT_CODE_SAMPLE_LIST = Arrays.asList("C002,A013,C007");

    private Log4JTestAppender testAppender;

    private JdbcTemplate jdbcTemplate;

    // keep the date same through out the testing 12 Nov 2015 10:16
    private static final Date ACCESS_DATE = new Date(1447283779875l);
    
    private static final String SYNC_DESCRIPTION = "Download data at Pawsey Centre. Note: Only users from within "
                                                + "PSC network can access the data through this link. Only use if "
                                                + "you currently have access to Pawsey facilities.";

    private static final String ASYNC_DESCRIPTION = "Scripted file access via Pawsey Centre. Note: Only users from "
                                                + "within PSC network can access the data through this link. Only "
                                                + "use if you currently have access to Pawsey facilities.";
    
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
        Configuration config = ConfigurationTest.getTestConfiguration();
        configRegistry.switchConfiguration(config, false);
        dataLinkService = new DataLinkService(configRegistry, voTableRepositoryService);
        dataLinkService.setConfiguration(configRegistry.getCurrent());
        dataLinkService.isReady();
        jdbcTemplate = config.gtDao().getTemplate();
    }

    @Test
    public void processQueryInvalidIds() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", DateTime.now());
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer,
                new String[] { "visibility-1", "cube-1", "", null, "invalid-123456",
                        "cube-1;drop table casda.tablename" },
                "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST, true, ACCESS_DATE);

        System.out.println(writer.getBuffer().toString());
        checkXmlAgainstTestCaseFile("service.invalid.ids", writer.getBuffer().toString());
    }

    @Test
    public void processQueryCasdaAdminTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                true, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.authenticated.released", writer.getBuffer().toString());
    }
    
    @Test
    public void processQueryWithFileSizeGreaterThanLimitTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 209717200L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                true, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.authenticated.released.size.limit", writer.getBuffer().toString());
    }

    @Test
    public void processQueryUnauthenticatedReleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList());
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 2L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "anonymous", "", PROJECT_CODE_SAMPLE_LIST,
                false, ACCESS_DATE);
        checkXmlAgainstTestCaseFile("service.unauthenticated.released", writer.getBuffer().toString());
    }

    @Test
    public void processQueryUnauthenticatedUneleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList());
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 2L);
        result.put("released_date", null);
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "visibility-123456" }, "anonymous", "",
                PROJECT_CODE_SAMPLE_LIST, false, ACCESS_DATE);
        checkXmlAgainstTestCaseFile("service.unauthenticated.unreleased", writer.getBuffer().toString());
    }

    @Test
    public void processQueryAuthenticatedAndNonCasdaAdminTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", null);
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);
        when(jdbcTemplate.queryForObject(any(), eq(new Object[] { 123456L }), eq(String.class))).thenReturn("12345");

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                false, ACCESS_DATE);
        checkXmlAgainstTestCaseFile("service.authenticated.unreleased", writer.getBuffer().toString());
    }

    @Test
    public void processQueryExceptionTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", null);
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = spy(new StringWriter());
        RuntimeException runtimeException = new RuntimeException("Invalid ids");
        doThrow(runtimeException).when(writer).append(Mockito.contains("<VOT:TD>http://"));

        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                false, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.error", writer.getBuffer().toString());

        testAppender.verifyLogMessage(Level.ERROR,
                allOf(containsString("E150]"), containsString("requestIds: \"[cube-123456]\""),
                        containsString("userMessage: failed to build AccessData URI's"),
                        containsString("[DataLink failed to build AccessData links]"),
                        containsString("userId: pul052")),
                (Throwable) runtimeException);
    }

    private void checkXmlAgainstTestCaseFile(String testCase, String xml) throws SAXException, IOException
    {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(
                FileUtils.readFileToString(new File("src/test/resources/datalink/" + testCase + ".xml")), xml));

        List<?> allDifferences = diff.getAllDifferences();
        Assert.assertEquals("Differences found: " + diff.toString(), 0, allDifferences.size());
    }
}
