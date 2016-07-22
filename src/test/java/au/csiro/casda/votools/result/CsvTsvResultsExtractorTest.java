package au.csiro.casda.votools.result;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import org.junit.Test;
import org.mockito.Mockito;

import au.csiro.casda.votools.result.CsvTsvResultsExtractor.OutputType;

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
 * Validates the CsvTsvResultsExtractor class.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class CsvTsvResultsExtractorTest
{
    private static final String FIELD_DEFS_LINE = "dataproduct_type,calib_level\r\n";
    private static final String TEST_DATE_STR = "1994-04-15T11:30:00.000Z";
    private static final int TEST_DATE_YEAR = 1994;
    private static final int TEST_DATE_DAY = 15;
    private static final int TEST_DATE_HOUR = 11;
    private static final int TEST_DATE_MIN = 30;
    
    private static final String APP_BASE_URL = "http://localhost/";
    private static final String PROXY_BASE_URL = "http://localhost/proxy/vo";

    /**
     * Test the output of a header when there are no fields/columns in the result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderNoFields() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), is("\r\n"));
    }

    /**
     * Test the output of a header when there are two fields/columns in the result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderSimpleFields() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);

        ResultSetMetaData mockMetaData = create2ColMetadata();

        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), is(FIELD_DEFS_LINE));
    }

    /**
     * Test the output of a footer when there are no errors or overflows.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputFooterNoExtra() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);

        extractor.outputFooter(false, "");
        assertThat(writer.toString(), is(""));

    }

    /**
     * Test the output of data when the result set is empty.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataNoRows() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockResults.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);

        extractor.extractData(mockResults);
        assertThat(writer.toString(), is(FIELD_DEFS_LINE));
    }

    /**
     * Test the output of data when the result set has one row.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataSingleRow() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);

        extractor.extractData(mockResults);
        assertThat(writer.toString(), is(FIELD_DEFS_LINE + "Foo," + TEST_DATE_STR + "\r\n"));
    }

    /**
     * Test the output of data when the result set has more rows than requested.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataOverflow() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 1, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);

        extractor.extractData(mockResults);
        assertThat(writer.toString(), is(FIELD_DEFS_LINE + "Foo," + TEST_DATE_STR + "\r\n"));
    }

    /**
     * Test the output of data in CSV format when escaping is needed .
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataWithCsvEscaping() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 2, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);

        extractor.extractData(mockResults);
        assertThat(writer.toString(), is(FIELD_DEFS_LINE + "Foo," + TEST_DATE_STR + "\r\n\"Bar,A\",\r\n"));
    }

    /**
     * Test the output of data in TSV format when escaping is needed .
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataWithTsvEscaping() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 2, OutputType.TSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("Bar\tA");

        extractor.extractData(mockResults);
        assertThat(writer.toString(), is("dataproduct_type\tcalib_level\r\n" + "Foo\t" + TEST_DATE_STR
                + "\r\n\"Bar\tA\"\t\r\n"));
    }

    /**
     * Test the output of data in CSV format when the url placeholder is substituted. When a proxy url is present
     * it should be used in the output.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataWithUrlSubstitutionWithProxy() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 2, OutputType.CSV, APP_BASE_URL,
                PROXY_BASE_URL);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("#{baseUrl}/bar.html");

        extractor.extractData(mockResults);
        assertThat(writer.toString(),
                is(FIELD_DEFS_LINE + "Foo," + TEST_DATE_STR + "\r\n" + PROXY_BASE_URL + "/bar.html,\r\n"));
    }
    
    /**
     * Test the output of data in CSV format when the url placeholder is substituted. When a proxy url is not present
     * the base url should be used instead.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataWithUrlSubstitutionWithoutProxy() throws Exception
    {
        StringWriter writer = new StringWriter();
        CsvTsvResultsExtractor extractor = new CsvTsvResultsExtractor(writer, 2, OutputType.CSV, APP_BASE_URL,
                null);
        ResultSetMetaData mockMetaData = create2ColMetadata();
        ResultSet mockResults = create2RowResultSet(mockMetaData);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("#{baseUrl}/bar.html");

        extractor.extractData(mockResults);
        assertThat(writer.toString(),
                is(FIELD_DEFS_LINE + "Foo," + TEST_DATE_STR + "\r\n" + APP_BASE_URL + "/bar.html,\r\n"));
    }

    private ResultSetMetaData create2ColMetadata() throws SQLException
    {
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(2);
        Mockito.when(mockMetaData.getColumnName(1)).thenReturn("dataproduct_type");
        Mockito.when(mockMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.when(mockMetaData.getColumnName(2)).thenReturn("calib_level");
        Mockito.when(mockMetaData.getColumnType(2)).thenReturn(Types.TIMESTAMP);
        return mockMetaData;
    }

    // PMD warning not applicable to setting up mocks
    @SuppressWarnings("PMD.CheckResultSet")
    private ResultSet create2RowResultSet(ResultSetMetaData mockMetaData) throws SQLException
    {
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockResults.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockResults.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("Bar,A");
        Timestamp TARGET_DATE = new Timestamp(Date.from(
                LocalDateTime.of(TEST_DATE_YEAR, Month.APRIL, TEST_DATE_DAY, TEST_DATE_HOUR, TEST_DATE_MIN).toInstant(
                        ZoneOffset.UTC)).getTime());
        Mockito.when(mockResults.getTimestamp(2)).thenReturn(TARGET_DATE).thenReturn(null);
        return mockResults;
    }

}
