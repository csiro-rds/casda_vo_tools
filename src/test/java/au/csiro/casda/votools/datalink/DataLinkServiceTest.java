package au.csiro.casda.votools.datalink;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.google.common.collect.Iterators;

import au.csiro.BaseTest;
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
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
public class DataLinkServiceTest extends BaseTest
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
    
    private static String VALID_REDIRECT = "http://localhost:8080/casda_data_access/data/sync?"
    											+ "id=8pPzgfTsuvHqCb-D0ULq71Paz3rC-7tvqp2KY9liUTr92SpP0kSfaDliFMcNq8bV";
    
    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
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
                new String[] { "visibility-1", "cube-1", "spectrum-1", "moment_map-1", "cubelet-1", "", null, 
                		"invalid-123456", "cube-1;drop table casda.tablename" },
                "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST, true, false, ACCESS_DATE);

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
                true, false, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.authenticated.released", writer.getBuffer().toString());
    }

    /**
     * Support for downloading catalogues via datalink by id added in 
     * https://jira.csiro.au/browse/CASDA-6482
     * @throws Exception err
     */
    @Test
    public void processQueryCatalogueTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "catalogue-123456" }, "pul052", "OPAL",
                PROJECT_CODE_SAMPLE_LIST, true, false, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.authenticated.released.catalogue", writer.getBuffer().toString());
    }

    /**
     * Check the handling of measurement set requests using scan ids 
     * @throws Exception err
     */
    @Test
    public void processQueryScanTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 1L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(10152L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "scan-10152-99817" }, "dem040", "OPAL",
                PROJECT_CODE_SAMPLE_LIST, true, false, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.authenticated.released.scan", writer.getBuffer().toString());
    }
    
    @Test
    public void processQueryWithFileSizeGreaterThanLimitTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 534288000L);
        result.put("released_date", "13-01-2016:16:29:39:00");
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                true, false, ACCESS_DATE);

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
                false, false, ACCESS_DATE);
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
                PROJECT_CODE_SAMPLE_LIST, false, false, ACCESS_DATE);
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
                false, false, ACCESS_DATE);
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
        doThrow(runtimeException).when(writer).append(Mockito.contains("<TD>http://"));

        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                false, false, ACCESS_DATE);

        checkXmlAgainstTestCaseFile("service.error", writer.getBuffer().toString());

        testAppender.verifyLogMessage(Level.INFO, "Initialised connection");
        testAppender.verifyLogMessage(Level.ERROR,
                allOf(containsString("E150]"), containsString("requestIds: \"[cube-123456]\""),
                        containsString("userMessage: failed to build AccessData URI's"),
                        containsString("[DataLink failed to build AccessData links]"),
                        containsString("userId: pul052")),
                (Throwable) runtimeException);
    }
    
    @Test
    public void processDownloadInvalidIdTest() throws Exception
    {
    	when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList(123l, 456l, 789l));
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 1L);
		result.put("released_date", DateTime.now());
		when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);

		DataLinkVoTableBuilder builder = (DataLinkVoTableBuilder) dataLinkService.processDownload
				("spectrum-x", "gre497", "OPAL", PROJECT_CODE_SAMPLE_LIST, true, ACCESS_DATE);	

        checkXmlAgainstTestCaseFile("ssap.download.invalid.id", builder.getXml());
    }
    
    @Test
    public void processDownloadValidNonExistentIdTest() throws Exception
    {
    	when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList(123l, 456l, 789l));
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 0);
		result.put("released_date", DateTime.now());
		when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);
		
		DataLinkVoTableBuilder builder = (DataLinkVoTableBuilder) dataLinkService.processDownload
				("spectrum-3", "gre497", "OPAL", PROJECT_CODE_SAMPLE_LIST, true, ACCESS_DATE);	
		
        checkXmlAgainstTestCaseFile("ssap.download.non.exist.id", builder.getXml());
    }
    
    @Test
    public void processDownloadUnauthenticatedUneleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList());
		Map<String, Object> result = new HashMap<>();
		when(jdbcTemplate.queryForMap(any(), eq(3L))).thenReturn(result);
		
		DataLinkVoTableBuilder builder = (DataLinkVoTableBuilder) dataLinkService.processDownload
				("spectrum-3", "anonymous", "OPAL", PROJECT_CODE_SAMPLE_LIST, false, ACCESS_DATE);	
		checkXmlAgainstTestCaseFile("ssap.download.non.exist.id", builder.getXml());
    }
    
    @Test
    public void processDownloadAuthenticatedUnReleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList());
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 2L);
		result.put("released_date", "");
		when(jdbcTemplate.queryForMap(any(), eq(3L))).thenReturn(result);
		
		String url = (String) dataLinkService.processDownload
				("spectrum-3", "gre497", "OPAL", PROJECT_CODE_SAMPLE_LIST, false, ACCESS_DATE);	
		
		assertEquals(VALID_REDIRECT, url);
    }
    
    @Test
    public void processDownloadAuthenticatedReleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList());
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 2L);
		result.put("released_date", "13-01-2016:16:29:39:00");
		when(jdbcTemplate.queryForMap(any(), eq(3L))).thenReturn(result);
		
		String url = (String) dataLinkService.processDownload
				("spectrum-3", "gre497", "OPAL", PROJECT_CODE_SAMPLE_LIST, false, ACCESS_DATE);	
		
		assertEquals(VALID_REDIRECT, url);
    }
    
    @Test
    public void processDownloadUnauthenticatedReleasedTest() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList());
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 2L);
		result.put("released_date", "13-01-2016:16:29:39:00");
		when(jdbcTemplate.queryForMap(any(), eq(3L))).thenReturn(result);
		
		DataLinkVoTableBuilder builder = (DataLinkVoTableBuilder) dataLinkService.processDownload
				("spectrum-3", "anonymous", "OPAL", PROJECT_CODE_SAMPLE_LIST, false, ACCESS_DATE);	
		
		checkXmlAgainstTestCaseFile("ssap.download.non.exist.id", builder.getXml());
    }
    
    @Test
    public void processDownloadUnreleasedCasdaAdminTest()
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
        .thenReturn(Arrays.asList());
		Map<String, Object> result = new HashMap<>();
		result.put("filesize", 2L);
		result.put("released_date", "");
		when(jdbcTemplate.queryForMap(any(), eq(3L))).thenReturn(result);
		
		String url = (String) dataLinkService.processDownload
				("spectrum-3", "gre497", "OPAL", PROJECT_CODE_SAMPLE_LIST, true, ACCESS_DATE);	
		
		assertEquals(VALID_REDIRECT, url);
    }

    private void checkXmlAgainstTestCaseFile(String testCase, String xml) throws SAXException, IOException
    {
        String testXml = FileUtils.readFileToString(new File("src/test/resources/datalink/" + testCase + ".xml"));
        Diff diff = DiffBuilder.compare(removeProcessingInstruction(testXml)).withTest(removeProcessingInstruction(xml))
                .ignoreWhitespace()//
                .build();
        
        Iterator<Difference> allDifferences = diff.getDifferences().iterator();
        int numDiff = Iterators.size(allDifferences);
        
        if (numDiff > 0)
        {
            System.out.println("--------");
            System.out.println("Differences found, actual XML is");
            System.out.println("--------");
            System.out.println(xml);
        }

        assertEquals(0, numDiff, "Differences found: " + diff.toString());
    }
    
    private String removeProcessingInstruction(String xml)
    {
        return xml.replaceAll("<\\?xml-stylesheet.*\\?>", "");
    }
    
    @Test
    public void processQueryAsCASDALargeDownloadRoleUserLessThanLimit() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 209717200L);
        result.put("released_date", null);
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);
        when(jdbcTemplate.queryForObject(any(), eq(new Object[] { 123456L }), eq(String.class))).thenReturn("12345");

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                false, true, ACCESS_DATE);
        checkXmlAgainstTestCaseFile("service.authenticated.large.download.role", writer.getBuffer().toString());
    }
    
    @Test
    public void processQueryAsCASDALargeDownloadRoleUserGreaterThanLimit() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(eq(PROJECT_CODE_SAMPLE_LIST), anyString()))
                .thenReturn(Arrays.asList(123l, 456l, 789l));
        Map<String, Object> result = new HashMap<>();
        result.put("filesize", 21474836485L);
        result.put("released_date", null);
        when(jdbcTemplate.queryForMap(any(), eq(123456L))).thenReturn(result);
        when(jdbcTemplate.queryForObject(any(), eq(new Object[] { 123456L }), eq(String.class))).thenReturn("12345");

        StringWriter writer = new StringWriter();
        dataLinkService.processQuery(writer, new String[] { "cube-123456" }, "pul052", "OPAL", PROJECT_CODE_SAMPLE_LIST,
                false, true, ACCESS_DATE);
        checkXmlAgainstTestCaseFile("service.authenticated.large.download.role.exceed.limit",
                writer.getBuffer().toString());
    }
}
