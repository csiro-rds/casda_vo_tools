package au.csiro.casda.votools.result;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.utils.Utils;

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
 * Outputs the results of a query to a VOTABLE format in accordance with the Table Access Protocol Version 1.0 IVOA
 * Recommendation 2010-03-27 and the VOTable Format Definition Version 1.3 IVOA Recommendation 2013-09-20.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class VoTableResultsExtractor extends ResultsExtractor implements ResultSetExtractor<Boolean>
{
    private static Logger logger = LoggerFactory.getLogger(VoTableResultsExtractor.class);

    private final Writer writer;
    private final int maxRec;
    private final String resourceName;
    private Map<String, String[]> serviceMetaDataMap;

    /** Map of field definitions for each table / column. */
    private Map<String, String> votableFieldMap;   
    
    private boolean proxiedOutput;

    /**
     * Creates a new instance of OutputTapQueryToVoTable for use outputting a single query only. Instances are not
     * reusable.
     * 
     * @param writer
     *            The writer to output the query results to.
     * @param maxRec
     *            The maximum number of records the user has requested.
     * @param votableFieldMap
     *            The map of field types for this query
     * @param resourceName
     *            The name to use in the VOtable resource element
     * @param baseUrl
     *            The web address at which this VO Tools instance can be found.
     */
    public VoTableResultsExtractor(Writer writer, int maxRec, Map<String, String> votableFieldMap, String resourceName,
            String baseUrl)
    {
        this(writer, maxRec, votableFieldMap, resourceName, null, baseUrl, null, false);
    }

    /**
     * Creates a new instance of OutputTapQueryToVoTable for use outputting a single query only. Instances are not
     * reusable.
     * 
     * @param writer
     *            The writer to output the query results to.
     * @param maxRec
     *            The maximum number of records the user has requested.
     * @param votableFieldMap
     *            The map of field types for this query
     * @param resourceName
     *            The name to use in the VOtable resource element\
     * @param serviceMetaDataMap
     *            a map of the meta data properties from the configuration file
     * @param baseUrl
     *            The web address at which this VO Tools instance can be found.
     */
    public VoTableResultsExtractor(Writer writer, int maxRec, Map<String, String> votableFieldMap, String resourceName,
            Map<String, String[]> serviceMetaDataMap, String baseUrl)
    {
        this(writer, maxRec, votableFieldMap, resourceName, serviceMetaDataMap, baseUrl, null, false);
    }

    /**
     * Creates a new instance of OutputTapQueryToVoTable for use outputting a single query only. Instances are not
     * reusable.
     * 
     * @param writer
     *            The writer to output the query results to.
     * @param maxRec
     *            The maximum number of records the user has requested.
     * @param votableFieldMap
     *            The map of field types for this query
     * @param resourceName
     *            The name to use in the VOtable resource element\
     * @param serviceMetaDataMap
     *            a map of the meta data properties from the configuration file
     * @param baseUrl
     *            The web address at which this VO Tools instance can be found.
     * @param proxyUrl
     *            The web address at which this VO Proxy instance can be found.
     * @param proxiedOutput
     *            Whether the out should being catered for vo_proxy or to the user.
     * 
     */
    public VoTableResultsExtractor(Writer writer, int maxRec, Map<String, String> votableFieldMap, String resourceName,
            Map<String, String[]> serviceMetaDataMap, String baseUrl, String proxyUrl, boolean proxiedOutput)
    {
        super(baseUrl, StringUtils.isBlank(proxyUrl) ? baseUrl : proxyUrl);
        this.writer = writer;
        this.maxRec = maxRec;
        this.votableFieldMap = votableFieldMap;
        this.resourceName = resourceName;
        this.serviceMetaDataMap = serviceMetaDataMap;
        this.proxiedOutput = proxiedOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException
    {
        logger.info("Started extracting data for result set " + rs);
        Utils.reportMemory(logger);

        setCutoff(false);
        int columnCount = rs.getMetaData().getColumnCount();
        final int memoryReportInterval = 50000;
        setProcessedCount(0);
        String error = "";
        try
        {
            int[] dataTypes = getDataTypes(rs.getMetaData());
            outputHeader(rs.getMetaData());
            try
            {
                while (getProcessedCount() < maxRec && rs.next())
                {
                    writer.append(buildRowOutput(rs, columnCount, dataTypes));
                    setProcessedCount(getProcessedCount() + 1);
                    if (getProcessedCount() % memoryReportInterval == 0)
                    {
                        logger.info("Reached " + getProcessedCount() + " records. ");
                        Utils.reportMemory(logger);
                    }
                }
                setCutoff(getProcessedCount() >= maxRec && !(rs.isLast() || rs.isAfterLast()));
                setResultSize(getProcessedCount());
                if (isCutoff())
                {
                    setResultSize(rs);
                }
            }
            catch (SQLException e)
            {
                logger.error("Error running query ", e);
                error = e.getMessage();
            }
            outputFooter(isCutoff(), error);
        }
        catch (IOException e)
        {
            logger.error("Error outputting query results.", e);
            throw new ProcessingException("Unable to write out results", e);
        }
        return isCutoff();
    }

    /**
     * Build up the response for a single row of data. This is done in a builder to avoid invalid XML output in the
     * event of an error.
     * 
     * @param rs
     *            The ResultSet to extract data from
     * @param columnCount
     *            The number of columns in the result.
     * @param dateColList
     *            A list of the indexes of columns identified to contain date information
     * @return The text of the row to be output.
     * @throws SQLException
     *             If the data cannot be read
     */
    private String buildRowOutput(ResultSet rs, int columnCount, int[] dataTypes) throws SQLException
    {
        StringBuilder rowOutput = new StringBuilder("           <TR>");
        for (int i = 1; i <= columnCount; i++)
        {
            String value = getFieldValue(rs, dataTypes[i], i);
            rowOutput.append("<TD>" + value + "</TD>");
        }
        rowOutput.append("</TR>\n");
        return rowOutput.toString();
    }

    /**
     * Appends a header section to the output. This includes setting up the VOTABLE xml, adding field descriptions and
     * starting the tabledata section.
     *
     * {@inheritDoc}
     */
    @Override
    protected void outputHeader(ResultSetMetaData metaData) throws SQLException, IOException
    {
        writer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
        writer.append("<?xml-stylesheet href='" + fetchXslBaseUrl() + "votable.xsl' type='text/xsl'?>\r\n");
        writer.append("<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\" "
                + "xmlns:stc=\"http://www.ivoa.net/xml/STC/v1.30\" >\r\n");
        writer.append("<RESOURCE name=\"");
        writer.append(this.resourceName);
        writer.append("\" type='results'>\r\n");
        writer.append("<INFO name=\"QUERY_STATUS\" value=\"OK\">Successful query</INFO>\r\n");
        // add params to header
        if (serviceMetaDataMap != null)
        {
            generateHeaderParams(writer);
        }

        // Note: we could add further entries here such as a license entry.
        writer.append("<TABLE name=\"results\">\r\n");
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++)
        {
            String name = metaData.getColumnName(i).toLowerCase();
            int type = metaData.getColumnType(i);
            String table = metaData.getTableName(i).toLowerCase();
            String fieldDef = votableFieldMap.get("scs|" + name);
            // using scs| because we are hard coding UCD1 values for scs (prefer UCD1.1 in the tables)
            // If we get a table from postgres, lookup the field info.
            if (StringUtils.isNotBlank(table))
            {
                fieldDef = votableFieldMap.get(table + "|" + name);
            }
            // Provided in case no field definition can be found.
            if (fieldDef == null)
            {
                fieldDef = getFieldDef(name, type, null);
            }
            writer.append(fieldDef);
        }
        writer.append("<DATA>\r\n");
        writer.append("<TABLEDATA>\r\n");
    }

    /**
     * Returns the VOTABLE compliant field definition for an otherwise unmatched database column.
     * 
     * @param name
     *            - name of the column described by this object
     * @param type
     *            - type of the column described by this object
     * @param ucd
     *            - ucd value for the column
     * @return An XML FIELD element describing the column.
     */
    public static String getFieldDef(String name, int type, String ucd)
    {
        String datatype = null;
        String nullvalue = "";
        switch (type)
        {
        case Types.BIT:
        case Types.BOOLEAN:
            datatype = "boolean";
            break;

        case Types.OTHER: // Bit varying
        case Types.SMALLINT:
            datatype = "short";
            break;

        case Types.DECIMAL:
        case Types.INTEGER:
        case Types.NUMERIC:
        case Types.TINYINT:
            datatype = "int";
            break;

        case Types.BIGINT:
            datatype = "long";
            break;

        case Types.ROWID:
            datatype = "int";
            // nullvalue = "-9223372036854775808";
            break;

        case Types.DOUBLE:
            datatype = "double";
            break;

        case Types.FLOAT:
        case Types.REAL:
            datatype = "float";
            break;

        default: // The rest is varchar
            datatype = "char";
            break;
        }

        StringBuffer s = new StringBuffer();
        s.append("  <FIELD ID=\"");
        s.append(name);
        s.append("\" name=\"");
        s.append(name);
        s.append("\" datatype=\"");
        s.append(datatype);
        s.append("\"");
        if (datatype.equals("char"))
        {
            s.append(" arraysize=\"*\"");
        }
        if (StringUtils.isNotBlank(ucd))
        {
            s.append(" ucd=\"");
            s.append(ucd);
            s.append("\"");
        }
        s.append(">");

        if (nullvalue.length() > 0)
        {
            s.append("         <VALUES null=\"");
            s.append(nullvalue);
            s.append("\"/>");
        }
        s.append(" </FIELD>\n");
        return s.toString();
    }

    /**
     * Appends a footer section to the output. This will close off the table, add any error or overflow info rows and
     * close off the votable.
     * 
     * @param overflow
     *            Are there more results than available than were output.
     * @param error
     *            An optional error message to be output
     * @throws IOException
     *             If the footer cannot be written.
     */
    @Override
    protected void outputFooter(boolean overflow, String error) throws IOException
    {
        writer.append("</TABLEDATA>\r\n");
        writer.append("</DATA>\r\n");
        writer.append("</TABLE>\r\n");
        if (StringUtils.isNotBlank(error))
        {
            writer.append("<INFO name=\"QUERY_STATUS\" value=\"ERROR\">" + error + "</INFO>\r\n");
        }
        else if (overflow)
        {
            writer.append("<INFO name=\"QUERY_STATUS\" value=\"OVERFLOW\" />\r\n");
        }
        writer.append("</RESOURCE>\r\n");
        writer.append("</VOTABLE>\r\n");
    }

    /**
     * Builds an xml field element representing the header information for a column in a Votable response.
     * 
     * @param tapColumn
     *            the column definition to use
     * @return the generated xml
     */
    public static String buildVoTableFieldHeader(TapColumn tapColumn)
    {
        return buildVoTableFieldHeader(tapColumn, tapColumn.getUcd());
    }

    /**
     * Builds an xml field element representing the header information for a column in a Votable response. The supplied
     * UCD is used instead of the TAP UCD, allowing overriding of the default (needed for compliance with UCD1 services
     * such as cone search..
     * 
     * @param tapColumn
     *            the column definition to use
     * @param ucd
     *            the iniversal content descriptior (UCD) value to use
     * @return the generated xml
     */
    public static String buildVoTableFieldHeader(TapColumn tapColumn, String ucd)
    {

        StringBuilder fieldDef = new StringBuilder("<FIELD name=\"");
        fieldDef.append(tapColumn.getId().getColumnName());
        fieldDef.append("\"");
        fieldDef.append(" ID=\"").append(tapColumn.getId().getColumnName());
        fieldDef.append("\"");
        String voTableColType = translateTapColumnTypeToVoTableType(tapColumn.getDatatype());
        if (StringUtils.isNotBlank(tapColumn.getDatatype()))
        {

            fieldDef.append(" datatype=\"");
            fieldDef.append(voTableColType);
            fieldDef.append("\"");
        }
        if ("char".equals(voTableColType))
        {
            Integer size = tapColumn.getSize();
            fieldDef.append(" arraysize=\"");
            if (size == null || size <= 0)
            {
                fieldDef.append("*");
            }
            else
            {
                fieldDef.append(size);
            }
            fieldDef.append("\"");
        }
        if (StringUtils.isNotBlank(tapColumn.getUnit()))
        {
            fieldDef.append(" unit=\"");
            fieldDef.append(tapColumn.getUnit());
            fieldDef.append("\"");
        }
        if (StringUtils.isNotBlank(ucd))
        {
            fieldDef.append(" ucd=\"");
            fieldDef.append(ucd);
            fieldDef.append("\"");
        }
        if (StringUtils.isNotBlank(tapColumn.getUtype()))
        {
            fieldDef.append(" utype=\"");
            fieldDef.append(tapColumn.getUtype());
            fieldDef.append("\"");
        }

        if (StringUtils.isNotBlank(tapColumn.getDescription()))
        {
            fieldDef.append(" >\r\n");
            fieldDef.append(" <DESCRIPTION>");
            fieldDef.append(tapColumn.getDescription());
            fieldDef.append("</DESCRIPTION>\r\n");
            fieldDef.append("</FIELD>\r\n");
        }
        else
        {
            fieldDef.append(" />\r\n");
        }
        return fieldDef.toString();
    }

    /**
     * Convert a SQL or TAP column type into a VOTable type.
     * @param tapColumnType The SQL or TAP c0olumn type to be converted.
     * @return The VOTable type
     */
    static String translateTapColumnTypeToVoTableType(String tapColumnType)
    {
        String datatype;
        switch (tapColumnType.toUpperCase())
        {
        case "INTEGER":
            datatype = "int";
            break;

        case "BIGINT":
            datatype = "long";
            break;

        case "VARCHAR":
        case "REGION":
        case "CLOB":
        case "TIMESTAMP":
        case "TEXT":
        case "SPOLY":
            datatype = "char";
            break;

        case "REAL":
            datatype = "float";
            break;

        case "DOUBLE PRECISION":
            datatype = "double";
            break;

        case "BOOLEAN":
            datatype = "boolean";
            break;

        case "VARBINARY":
        case "BIT":
        case "SMALLINT":
            datatype = "short";
            break;

        default:
            if (tapColumnType.toUpperCase().startsWith("CHARACTER"))
            {
                datatype = "char";
            }
            else
            {
                // The rest can be switched to lowercase - ie DOUBLE -> double
                datatype = tapColumnType.toLowerCase();
            }
            break;
        }
        return datatype;
    }

    /**
     * 
     * @param writer
     *            the writer for generating the header
     * @throws IOException
     *             an exception
     */
    private void generateHeaderParams(Writer writer) throws IOException
    {
        // time taken to run query
        if (serviceMetaDataMap.get("executionTime") != null)
        {
            long duration = Duration
                    .between(ZonedDateTime.parse(serviceMetaDataMap.get("executionTime")[0]), ZonedDateTime.now())
                    .toMillis();
            serviceMetaDataMap.get("executionTime")[0] = String.valueOf(duration) + "ms";
        }

        for (String key : serviceMetaDataMap.keySet())
        {
            String[] info = serviceMetaDataMap.get(key);
            if (info.length > 1)
            {
                writer.append("<INFO name=\"" + Utils.convertCamelCase(key) + "\" value=\"" + info[0] + "\">" + info[1]
                        + "</INFO>\r\n");
            }
        }
    }
    
    private String fetchXslBaseUrl()
    {
        return proxiedOutput ? getProxyUrl(): getBaseUrl();
    }
}
