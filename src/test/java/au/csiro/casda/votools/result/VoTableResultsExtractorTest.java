package au.csiro.casda.votools.result;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.tap.TapService;

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
 * Validates the VoTableResultsExtractor class.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class VoTableResultsExtractorTest
{

    private static final String BASE_HEADER_PART1 = "^<\\?xml version=\"1.0\" encoding=\"utf-8\"\\?>\r\n"
            + "<\\?xml-stylesheet href='http://localhost/votable.xsl' type='text/xsl'\\?>\r\n"
            + "<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\" xmlns:stc=\"http://www.ivoa.net/xml/STC/v1.30\" >\r\n"
            + "<RESOURCE name=\"CASDA TAP Result\" type='results'>\r\n"
            + "<INFO name=\"QUERY_STATUS\" value=\"OK\">Successful query</INFO>\r\n"
            + "<INFO name=\"Instrument\" value=\"ASKAP\">Instrument from which data originated </INFO>\r\n"
            + "<INFO name=\"Server\" value=\"https://casda-t-app.pawsey.ivec.org:8080/casda_vo_tools/tap\">URL of the "
            + "CASDA VO endpoint used </INFO>\r\n"
            + "<INFO name=\"Service Short Name\" value=\"CASDA TAP\">Short name of the CASDA service</INFO>\r\n"
            + "<INFO name=\"Service Title\" value=\"CSIRO ASKAP Science Data Archive TAP service\">TAP service title "
            + "</INFO>\r\n"
            + "<INFO name=\"Identifier\" value=\"ivo://casda.csiro.au/casda/TAP\">Unique resource registry identifier"
            + "</INFO>\r\n"
            + "<INFO name=\"Service Publisher\" value=\"The CASDA team\">Publisher for the TAP service</INFO>\r\n"
            + "<INFO name=\"Further Information\" value=\"http://www.atnf.csiro.au/projects/askap/index.html\">Link to"
            + " further information on the data and usage of the service</INFO>\r\n"
            + "<INFO name=\"Contact Person\" value=\"CASDA Support <atnf-datasup@csiro.au>\">Who to contact about this "
            + "service</INFO>\r\n"
            + "<INFO name=\"Copyright\" value=\"Creative Commons Attribution Licence\">CASDA's Data license</INFO>\r\n"
            + "<INFO name=\"Query\" value=\"SELECT TOP 1000 \\* FROM casda.spectral_line_absorption\">Query submitted "
            + "by the user</INFO>\r\n"
            + "<INFO name=\"Execution Time\" value=\"[0-9]*ms\">Time taken to execute query</INFO>\r\n"
            + "<INFO name=\"Datetime Requested\" value=\"[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} [A-Z]"
            + "{2,4}\">Date and time that the request " + "was received</INFO>\r\n"
            + "<INFO name=\"Description Long\" value=\"This table has very long description\">"
            + "Long des of the table</INFO>\r\n" + "<INFO name=\"Table Name\" value=\"One potato\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<INFO name=\"Index Fields\" value=\"Two potatos\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<INFO name=\"Principal\" value=\"Three potatos\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<TABLE name=\"results\">\r\n";

    private static final String CSS_HEADER_PART1 = "^<\\?xml version=\"1.0\" encoding=\"utf-8\"\\?>\r\n"
            + "<\\?xml-stylesheet href='%svotable.xsl' type='text/xsl'\\?>\r\n"
            + "<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\" xmlns:stc=\"http://www.ivoa.net/xml/STC/v1.30\" >\r\n"
            + "<RESOURCE name=\"CASDA TAP Result\" type='results'>\r\n"
            + "<INFO name=\"QUERY_STATUS\" value=\"OK\">Successful query</INFO>\r\n"
            + "<INFO name=\"Instrument\" value=\"ASKAP\">Instrument from which data originated </INFO>\r\n"
            + "<INFO name=\"Server\" value=\"https://casda-t-app.pawsey.ivec.org:8080/casda_vo_tools/tap\">URL of the "
            + "CASDA VO endpoint used </INFO>\r\n"
            + "<INFO name=\"Service Short Name\" value=\"CASDA TAP\">Short name of the CASDA service</INFO>\r\n"
            + "<INFO name=\"Service Title\" value=\"CSIRO ASKAP Science Data Archive TAP service\">TAP service title "
            + "</INFO>\r\n"
            + "<INFO name=\"Identifier\" value=\"ivo://casda.csiro.au/casda/TAP\">Unique resource registry identifier"
            + "</INFO>\r\n"
            + "<INFO name=\"Service Publisher\" value=\"The CASDA team\">Publisher for the TAP service</INFO>\r\n"
            + "<INFO name=\"Further Information\" value=\"http://www.atnf.csiro.au/projects/askap/index.html\">Link to"
            + " further information on the data and usage of the service</INFO>\r\n"
            + "<INFO name=\"Contact Person\" value=\"CASDA Support <atnf-datasup@csiro.au>\">Who to contact about this "
            + "service</INFO>\r\n"
            + "<INFO name=\"Copyright\" value=\"Creative Commons Attribution Licence\">CASDA's Data license</INFO>\r\n"
            + "<INFO name=\"Query\" value=\"SELECT TOP 1000 \\* FROM casda.spectral_line_absorption\">Query submitted "
            + "by the user</INFO>\r\n"
            + "<INFO name=\"Execution Time\" value=\"[0-9]*ms\">Time taken to execute query</INFO>\r\n"
            + "<INFO name=\"Datetime Requested\" value=\"[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} [A-Z]"
            + "{2,4}\">Date and time that the request " + "was received</INFO>\r\n"
            + "<INFO name=\"Description Long\" value=\"This table has very long description\">"
            + "Long des of the table</INFO>\r\n" + "<INFO name=\"Table Name\" value=\"One potato\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<INFO name=\"Index Fields\" value=\"Two potatos\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<INFO name=\"Principal\" value=\"Three potatos\">"
            + "Parameter supplied with Level 7 table \\(Derived Catalogues\\)</INFO>\r\n"
            + "<TABLE name=\"results\">\r\n";

    private static final String BASE_HEADER_PART2 = "<DATA>\r\n" + "<TABLEDATA>\r\n";

    private static final String FIELD_DEF_DATA_PRODUCT_TYPE =
            "<FIELD name=\"dataproduct_type\" " + "ID=\"dataproduct_type\" ucd=\"\" ref=\"\" datatype=\"char\" />\r\n";
    private static final String FIELD_DEF_CALIB_LEVEL =
            "<FIELD name=\"calib_level\" ID=\"calib_level\" " + "ucd=\"\" ref=\"\" datatype=\"int\" />\r\n";
    private static final String FIELD_DEF_FLAGS = "  <FIELD ID=\"flags\" name=\"flags\" datatype=\"short\"> </FIELD>\n";
    private static final String FIELD_DEF_Y_AVE = "  <FIELD ID=\"y_ave\" name=\"y_ave\" datatype=\"float\"> </FIELD>\n";
    private static final String EMPTY_HEADER = BASE_HEADER_PART1 + BASE_HEADER_PART2;

    private static final String FIELD_DEFS =
            FIELD_DEF_DATA_PRODUCT_TYPE + FIELD_DEF_CALIB_LEVEL + FIELD_DEF_FLAGS + FIELD_DEF_Y_AVE;

    private static final String APP_BASE_URL = "http://localhost/";
    private static final String PROXY_BASE_URL = "http://localhost/proxy/vo";

    private static final String EMPTY_FOOTER =
            "</TABLEDATA>\r\n" + "</DATA>\r\n" + "</TABLE>\r\n" + "</RESOURCE>\r\n" + "</VOTABLE>\r\n";
    private static final String OVERFLOW_FOOTER = "</TABLEDATA>\r\n" + "</DATA>\r\n" + "</TABLE>\r\n"
            + "<INFO name=\"QUERY_STATUS\" value=\"OVERFLOW\" />\r\n" + "</RESOURCE>\r\n" + "</VOTABLE>\r\n";

    private Map<String, String> votableFieldMap;

    private Map<String, String[]> metadataMap;
    private DateTimeFormatter format;

    @Before
    public void setup()
    {
        votableFieldMap = new HashMap<String, String>();
        votableFieldMap.put("obscore|dataproduct_type", FIELD_DEF_DATA_PRODUCT_TYPE);
        votableFieldMap.put("obscore|calib_level", FIELD_DEF_CALIB_LEVEL);

        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");
        metadataMap = new LinkedHashMap<String, String[]>();
        ZonedDateTime started = ZonedDateTime.now();
        metadataMap.put("instrument", new String[] { "ASKAP", "Instrument from which data originated " });
        metadataMap.put("server", new String[] { "https://casda-t-app.pawsey.ivec.org:8080/casda_vo_tools/tap",
                "URL of the CASDA VO endpoint used " });
        metadataMap.put("serviceShortName", new String[] { "CASDA TAP", "Short name of the CASDA service" });
        metadataMap.put("serviceTitle",
                new String[] { "CSIRO ASKAP Science Data Archive TAP service", "TAP service title " });
        metadataMap.put("identifier",
                new String[] { "ivo://casda.csiro.au/casda/TAP", "Unique resource registry identifier" });
        metadataMap.put("servicePublisher", new String[] { "The CASDA team", "Publisher for the TAP service" });
        metadataMap.put("furtherInformation", new String[] { "http://www.atnf.csiro.au/projects/askap/index.html",
                "Link to further information on the data and usage of the service" });
        metadataMap.put("contactPerson",
                new String[] { "CASDA Support <atnf-datasup@csiro.au>", "Who to contact about this service" });
        metadataMap.put("copyright", new String[] { "Creative Commons Attribution Licence", "CASDA's Data license" });
        metadataMap.put("query", new String[] { "SELECT TOP 1000 * FROM casda.spectral_line_absorption",
                "Query submitted by the user" });
        metadataMap.put("executionTime", new String[] { started.toString(), "Time taken to execute query" });
        metadataMap.put("datetimeRequested",
                new String[] { format.format(started), "Date and time that the request was received" });
        metadataMap.put("descriptionLong",
                new String[] { "This table has very long description", "Long des of the table" });
        metadataMap.put("TableName",
                new String[] { "One potato", "Parameter supplied with Level 7 table (Derived Catalogues)" });
        metadataMap.put("IndexFields",
                new String[] { "Two potatos", "Parameter supplied with Level 7 table (Derived Catalogues)" });
        metadataMap.put("Principal",
                new String[] { "Three potatos", "Parameter supplied with Level 7 table (Derived Catalogues)" });
    }

    /**
     * Tests outputting a header when no columns/fields are defined in the result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderNoFields() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, new HashMap<String, String>(),
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), matchesPattern(EMPTY_HEADER));
    }

    /**
     * Tests outputting a header with a different VO Table heading.
     * 
     * @throws Exception
     *             not expected.
     */
    @Test
    public void testOutputHeaderAlternativeVoTableHeading() throws Exception
    {
        String ALTERNATIVE_VO_TABLE_HEADING = "Alternative VO Table Heading";
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, new HashMap<String, String>(),
                ALTERNATIVE_VO_TABLE_HEADING, metadataMap, APP_BASE_URL);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(),
                matchesPattern(EMPTY_HEADER.replace(TapService.CASDA_TAP_RESULT_NAME, ALTERNATIVE_VO_TABLE_HEADING)));
    }

    /**
     * Tests outputting a header when two columns/fields are defined in the result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderTwoFields() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);

        ResultSetMetaData mockMetaData = create4ColMetadata();

        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), matchesPattern(BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2 + "$"));
    }

    /**
     * Tests outputting a header for proxied request.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderProxiedRequest() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, new HashMap<String, String>(),
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL, PROXY_BASE_URL, true);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), matchesPattern(String.format(CSS_HEADER_PART1, PROXY_BASE_URL) + BASE_HEADER_PART2));
    }
    
    /**
     * Tests outputting a header for direct request.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderDirectRequest() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, new HashMap<String, String>(),
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL, PROXY_BASE_URL, false);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), matchesPattern(String.format(CSS_HEADER_PART1, APP_BASE_URL) + BASE_HEADER_PART2));
    }

    /**
     * Tests outputting a header when no cssUrl are defined in the result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputHeaderNoCssUrl() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, new HashMap<String, String>(),
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);

        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(),
                matchesPattern(String.format(CSS_HEADER_PART1, APP_BASE_URL) + BASE_HEADER_PART2));
    }

    /**
     * Tests outputting a footer with no overflow or errors.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputFooterNoExtra() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor =
                new VoTableResultsExtractor(writer, 1, votableFieldMap, TapService.CASDA_TAP_RESULT_NAME, APP_BASE_URL);

        extractor.outputFooter(false, "");
        assertThat(writer.toString(), is(EMPTY_FOOTER));

    }

    /**
     * Tests outputting a footer with an errors.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testOutputFooterWithError() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor =
                new VoTableResultsExtractor(writer, 1, votableFieldMap, TapService.CASDA_TAP_RESULT_NAME, APP_BASE_URL);

        extractor.outputFooter(false, "This is an error");
        assertThat(writer.toString(),
                is("</TABLEDATA>\r\n" + "</DATA>\r\n" + "</TABLE>\r\n"
                        + "<INFO name=\"QUERY_STATUS\" value=\"ERROR\">This is an error</INFO>\r\n" + "</RESOURCE>\r\n"
                        + "</VOTABLE>\r\n"));

    }

    /**
     * Tests outputting an empty result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataNoRows() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);
        ResultSetMetaData mockMetaData = create4ColMetadata();
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockResults.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);

        extractor.extractData(mockResults);
        assertThat(writer.toString(),
                matchesPattern(BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2 + EMPTY_FOOTER + "$"));
    }

    /**
     * Tests outputting a single record result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataSingleRow() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);
        ResultSetMetaData mockMetaData = create4ColMetadata();
        ResultSet mockResults = create4ColResultSet(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);

        extractor.extractData(mockResults);
        assertThat(writer.toString(), matchesPattern(BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2
                + "           <TR><TD>Foo</TD><TD>1</TD><TD>2</TD><TD>211.1</TD></TR>\n" + EMPTY_FOOTER + "$"));
    }

    /**
     * Tests outputting a single record result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataTwoRowWithNull() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 2, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);
        ResultSetMetaData mockMetaData = create4ColMetadata();
        ResultSet mockResults = create4ColResultSet(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);

        extractor.extractData(mockResults);
        String pattern = BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2
                + "           <TR><TD>Foo</TD><TD>1</TD><TD>2</TD><TD>211.1</TD></TR>\n"
                + "           <TR><TD>Bar</TD><TD></TD><TD>42</TD><TD>190.05</TD></TR>\n" + EMPTY_FOOTER + "$";

        assertThat(writer.toString(), matchesPattern(pattern));
    }

    /**
     * Tests outputting a single record result set.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataTwoRowWithUrlSubstitution() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 2, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);
        ResultSetMetaData mockMetaData = create4ColMetadata();
        ResultSet mockResults = create4ColResultSet(mockMetaData);
        Mockito.when(mockResults.isAfterLast()).thenReturn(true);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("#{baseUrl}tap/sync");

        extractor.extractData(mockResults);
        String pattern = BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2
                + "           <TR><TD>Foo</TD><TD>1</TD><TD>2</TD><TD>211.1</TD></TR>\n" //
                + "           <TR><TD>" + APP_BASE_URL + "tap/sync</TD><TD></TD><TD>42</TD><TD>190.05</TD></TR>\n"
                + EMPTY_FOOTER + "$";

        assertThat(writer.toString(), matchesPattern(pattern));
    }

    @Test
    public void testBuildVoTableFieldHeader()
    {
        // Use a TapColumn to build the votable columnb info
        TapColumn charCol = this.buildTapColumn("bob", "VARCHAR", "", "", "", 5, "some desc");
        String charColStr = VoTableResultsExtractor.buildVoTableFieldHeader(charCol);
        assertThat(charColStr, is("<FIELD name=\"bob\" ID=\"bob\" datatype=\"char\" arraysize=\"5\" >"
                + "\r\n <DESCRIPTION>some desc</DESCRIPTION>\r\n</FIELD>\r\n"));

        TapColumn regionCol = this.buildTapColumn("region", "REGION", "", "", "", 5, "another desc");
        String regionColStr = VoTableResultsExtractor.buildVoTableFieldHeader(regionCol);
        assertThat(regionColStr, is("<FIELD name=\"region\" ID=\"region\" datatype=\"char\" arraysize=\"5\" >"
                + "\r\n <DESCRIPTION>another desc</DESCRIPTION>\r\n</FIELD>\r\n"));

        TapColumn doubleCol = this.buildTapColumn("dub", "DOUBLE", "", "", "", 5, null);
        String doubleColStr = VoTableResultsExtractor.buildVoTableFieldHeader(doubleCol);
        assertThat(doubleColStr, is("<FIELD name=\"dub\" ID=\"dub\" datatype=\"double\" />\r\n"));

        TapColumn bigIntCol = this.buildTapColumn("dub", "BIGINT", "", "", "", 5, "");
        String bigIntColStr = VoTableResultsExtractor.buildVoTableFieldHeader(bigIntCol);
        assertThat(bigIntColStr, is("<FIELD name=\"dub\" ID=\"dub\" datatype=\"long\" />\r\n"));

        TapColumn intCol = this.buildTapColumn("dub", "INTEGER", "", "", "", 5, null);
        String intColStr = VoTableResultsExtractor.buildVoTableFieldHeader(intCol);
        assertThat(intColStr, is("<FIELD name=\"dub\" ID=\"dub\" datatype=\"int\" />\r\n"));

        TapColumn realCol = this.buildTapColumn("dub", "REAL", "", "", "", 5, null);
        String realColStr = VoTableResultsExtractor.buildVoTableFieldHeader(realCol);
        assertThat(realColStr, is("<FIELD name=\"dub\" ID=\"dub\" datatype=\"float\" />\r\n"));

        TapColumn allCol = this.buildTapColumn("all", "INTEGER", "unitA", "UCD1", "Utype2", 5, "The Desc");
        String allColStr = VoTableResultsExtractor.buildVoTableFieldHeader(allCol);
        assertThat(allColStr,
                is("<FIELD name=\"all\" ID=\"all\" datatype=\"int\" "
                        + "unit=\"unitA\" ucd=\"UCD1\" utype=\"Utype2\" >\r\n "
                        + "<DESCRIPTION>The Desc</DESCRIPTION>\r\n</FIELD>\r\n"));

    }

    /**
     * Tests outputting a result set with more records than requested.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataOverflow() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);
        ResultSetMetaData mockMetaData = create4ColMetadata();
        ResultSet mockResults = create4ColResultSet(mockMetaData);

        extractor.extractData(mockResults);
        assertThat(writer.toString(),
                matchesPattern(BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2
                        + "           <TR><TD>Foo</TD><TD>1</TD><TD>2</TD><TD>211.1</TD></TR>\n" + OVERFLOW_FOOTER
                        + "$"));
    }

    /**
     * Tests outputting a result set with more records than requested.
     * 
     * @throws Exception
     *             Not expected.
     */
    @Test
    public void testExtractDataTableHeaders() throws Exception
    {
        StringWriter writer = new StringWriter();
        VoTableResultsExtractor extractor = new VoTableResultsExtractor(writer, 1, votableFieldMap,
                TapService.CASDA_TAP_RESULT_NAME, metadataMap, APP_BASE_URL);

        ResultSetMetaData mockMetaData = create4ColMetadata();

        extractor.outputHeader(mockMetaData);
        assertThat(writer.toString(), matchesPattern(BASE_HEADER_PART1 + FIELD_DEFS + BASE_HEADER_PART2 + "$"));
    }

    private ResultSetMetaData create4ColMetadata() throws SQLException
    {
        ResultSetMetaData mockMetaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(mockMetaData.getColumnCount()).thenReturn(4);
        Mockito.when(mockMetaData.getTableName(anyInt())).thenReturn("obscore");
        Mockito.when(mockMetaData.getColumnName(1)).thenReturn("dataproduct_type");
        Mockito.when(mockMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.when(mockMetaData.getColumnName(2)).thenReturn("calib_level");
        Mockito.when(mockMetaData.getColumnType(2)).thenReturn(Types.INTEGER);
        Mockito.when(mockMetaData.getColumnName(3)).thenReturn("flags");
        Mockito.when(mockMetaData.getColumnType(3)).thenReturn(Types.OTHER);
        Mockito.when(mockMetaData.getColumnName(4)).thenReturn("y_ave");
        Mockito.when(mockMetaData.getColumnType(4)).thenReturn(Types.REAL);
        return mockMetaData;
    }

    // PMD warning not applicable to setting up mocks
    @SuppressWarnings("PMD.CheckResultSet")
    private ResultSet create4ColResultSet(ResultSetMetaData mockMetaData) throws SQLException
    {
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        Mockito.when(mockResults.getMetaData()).thenReturn(mockMetaData);
        Mockito.when(mockResults.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.when(mockResults.getString(1)).thenReturn("Foo").thenReturn("Bar");
        Mockito.when(mockResults.getString(2)).thenReturn("1").thenReturn(null);
        Mockito.when(mockResults.getString(3)).thenReturn("10").thenReturn("101010");
        Mockito.when(mockResults.getString(4)).thenReturn("BAD").thenReturn("BAD");
        Mockito.when(mockResults.getFloat(4)).thenReturn(211.1f).thenReturn(190.05f);
        return mockResults;
    }

    private TapColumn buildTapColumn(String columnName, String datatype, String unit, String ucd, String utype,
            int size, String description)
    {
        TapColumn col = new TapColumn();
        col.setDatatype(datatype);
        col.setSize(size);
        col.setUcd(ucd);
        col.setUtype(utype);
        col.setUnit(unit);
        TapColumnPK id = new TapColumnPK();
        id.setColumnName(columnName);
        col.setId(id);
        col.setDescription(description);
        return col;
    }
}
