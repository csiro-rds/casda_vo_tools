package au.csiro.casda.votools.datalink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.google.common.collect.Iterators;

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
 * Check the DataLinkVoTableBuilder class.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class DataLinkVoTableBuilderTest
{
    private static final String APP_BASE_URL = "http://localhost:8080/casda_vo_tools/";

    /**
     * Test method for {@link au.csiro.casda.votools.datalink.DataLinkVoTableBuilder#getXml()}.
     * 
     * @throws JAXBException
     *             Not expected
     */
    @Test
    public void testGetXmlEmptyTable() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        checkXmlAgainstTestCaseFile("builder.empty", builder.getXml());
    }

    /**
     * Test method for {@link au.csiro.casda.votools.datalink.DataLinkVoTableBuilder#withResultsTable()}.
     * 
     * @throws JAXBException
     *             Not expected
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testWithResultsTable() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable();
        checkXmlAgainstTestCaseFile("builder.results.empty", builder.getXml());
    }

    @Test
    public void testWithServiceDefinition() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable().withServiceDefinition("async_service", "ivo://ivoa.net/std/SODA#async-1.0",
                "http://localhost/data", "DownloadService",
                "Asynchronous download of the file(s) to any server.");
        checkXmlAgainstTestCaseFile("builder.service_def", builder.getXml());
    }

    @Test
    public void testWithServiceDefResult() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable().withServiceDefResult("cube-909", "async_service", "Data Access Portal", null, 1024L,
                "myAuthenticatedIdToken");
        checkXmlAgainstTestCaseFile("builder.result.service_def", builder.getXml());
    }

    @Test
    public void testWithInternalServiceDefResult() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable().withServiceDefResult("cube-909", "pawsey_async_service", "Data Access Portal", null,
                1024L, "myAuthenticatedIdToken");
        checkXmlAgainstTestCaseFile("builder.result.internal.service_def", builder.getXml());
    }

    @Test
    public void testWithAccessUrlResult() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable().withAccessUrlResult("cube-909",
                "http://somewhere/public/casda/casdaResult.zul?dpId=909&dataProductType=IMAGE_CUBE",
                "Data Access Portal", "text/html", 2048L);
        checkXmlAgainstTestCaseFile("builder.result.access_url", builder.getXml());
    }

    @Test
    public void testWithErrorResult() throws Exception
    {
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(APP_BASE_URL);
        builder.withResultsTable().withErrorResult(StringEscapeUtils.escapeXml10("909"),
                "NotFoundFault: " + StringEscapeUtils.escapeXml10("909") + " cannot be found");
        checkXmlAgainstTestCaseFile("builder.result.error", builder.getXml());
    }

    private void checkXmlAgainstTestCaseFile(String testCase, String xml) throws SAXException, IOException
    {
        String expectedXml = FileUtils.readFileToString(new File("src/test/resources/datalink/" + testCase + ".xml"));
        Diff diff = DiffBuilder.compare(expectedXml.toString()).withTest(xml.toString()).ignoreWhitespace().build();

        Iterator<Difference> allDifferences = diff.getDifferences().iterator();
        int numDiffs = Iterators.size(allDifferences);
        if (numDiffs > 0)
        {
            System.out.println("--------");
            System.out.println("Differences found, actual XML is");
            System.out.println("--------");
            System.out.println(xml);
        }

        assertEquals(0, numDiffs, "Differences found: " + diff.toString());
    }
}
