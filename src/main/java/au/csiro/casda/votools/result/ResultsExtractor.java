package au.csiro.casda.votools.result;

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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.NumericEntityEscaper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;


/**
 * Contains common functionality for different format ResultsExtractors.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public abstract class ResultsExtractor
{

    private static Logger logger = LoggerFactory.getLogger(ResultsExtractor.class);

    /** Common format for date/time conversion */
    private DateTimeFormatter format;

    /** True if results were truncated */
    private boolean cutoff;

    /** Total number of matching rows */
    private long resultSize;

    /** Processed number of matching rows */
    private long processedCount;

    private String baseUrl;
    
    private String proxyUrl;

    /**
     * Creates a new instance of the TapResultsExtractor.
     * 
     * @param baseUrl
     *            The web address at which this VO Tools instance can be found.
     * @param proxyUrl
     *            The web address at which this VO Proxy instance can be found.
     */
    public ResultsExtractor(String baseUrl, String proxyUrl)
    {
        super();
        this.baseUrl = baseUrl;
        this.proxyUrl = proxyUrl;
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    /**
     * Appends a header section to the output. This might describe the fields and/or include query information.
     * 
     * @param metaData
     *            The result set metadata.
     * @throws SQLException
     *             If the metadata header cannot be retrieved.
     * @throws IOException
     *             If the header cannot be written.
     */
    protected abstract void outputHeader(ResultSetMetaData metaData) throws SQLException, IOException;

    /**
     * Appends a footer section to the output.
     * 
     * @param overflow
     *            Are there more results than available than were output.
     * @param error
     *            An optional error message to be output
     * @throws IOException
     *             If the footer cannot be written.
     */
    protected abstract void outputFooter(boolean overflow, String error) throws IOException;

    /**
     * Scans the result columns and identifies those that are date/time columns.
     * 
     * @param metaData
     *            The result set metadata.
     * @return A list of dates column indexes.
     * @throws SQLException
     *             If the metadata cannot be retrieved.
     */
    protected int[] getDataTypes(ResultSetMetaData metaData) throws SQLException
    {
        int columnCount = metaData.getColumnCount();
        int dataTypes[] = new int[columnCount+1];
        for (int i = 1; i <= columnCount; i++)
        {
            dataTypes[i] = metaData.getColumnType(i);
        }
        return dataTypes;
    }

    /**
     * Retrieves the value of a specific field from a result set row, formatting dates and handling null values.
     * 
     * @param rs
     *            The result set we are extracting fields values from.
     * @param dataType
     *            The SQL data type of this field. @see Types
     * @param columnIndex
     *            The index of the column to get a value for.
     * @return The value of the field in the current row.
     * @throws SQLException
     *             If the value cannot be retrieved form the result set.
     */
    protected String getFieldValue(ResultSet rs, int dataType, int columnIndex) throws SQLException
    {   
        final int binaryRadix = 2;
        String value = null;
        switch (dataType)
        {
        case Types.TIMESTAMP:
            value = getTimestampValue(rs, columnIndex, value);
            break;

        case Types.OTHER: // BIT VARYING and GEOMETRY
            value = getOtherTypeValue(rs, columnIndex, binaryRadix, value);
            break;

        case Types.DOUBLE:
            if (rs.getString(columnIndex) != null)
            {
                value = String.valueOf(rs.getDouble(columnIndex));
            }
            break;

        case Types.FLOAT:
        case Types.REAL:
            if (rs.getString(columnIndex) != null)
            {
                value = String.valueOf(rs.getFloat(columnIndex));
            }
            break;

        default:
            value = rs.getString(columnIndex);
            String baseUrlPlaceholder = "#{baseUrl}";
            if (value != null && value.startsWith(baseUrlPlaceholder))
            {
                value = value.replace(baseUrlPlaceholder, StringUtils.isBlank(proxyUrl) ? baseUrl : proxyUrl);
            }
            break;
        }

        if (value == null)
        {
            value = "";
        }
        final int lowestNonAsciiChar = 0x7f;
        return StringEscapeUtils.ESCAPE_XML11.with(NumericEntityEscaper.between(lowestNonAsciiChar, Integer.MAX_VALUE))
                .translate(value);
    }

    private String getOtherTypeValue(ResultSet rs, int columnIndex, final int binaryRadix, String value) throws SQLException
    {
        Object pgObject = rs.getObject(columnIndex);
        //conversion only performed on postgres var bit type 
        if(pgObject instanceof PGobject && ((PGobject) pgObject).getType().equals("varbit"))
        {
            value = String.valueOf(Integer.parseInt(((PGobject) pgObject).getValue(), binaryRadix));
        }
        else
        {
            String rawValue = rs.getString(columnIndex);

            if (rawValue != null)
            {
                String columnTypeName = rs.getMetaData().getColumnTypeName(columnIndex);
                if ("geometry".equals(columnTypeName))
                {
                    // Convert the geometry field to readable text
                    WKBReader reader = new WKBReader();
                    try
                    {
                        Geometry geometry = reader.read(WKBReader.hexToBytes(rawValue));
                        value = geometry.toText();
                    }
                    catch (ParseException e)
                    {
                        logger.error("Unable to convert geometry {} to string, reporting raw string.", rawValue, e);
                        value = rawValue;
                    }
                }
                else if ("spoly".equals(columnTypeName))
                {
                    value = rawValue;
                }
            } 
        }

        return value;
    }

    private String getTimestampValue(ResultSet rs, int columnIndex, String value) throws SQLException
    {
        Date date = rs.getTimestamp(columnIndex);
        if (date != null)
        {
            Instant instant = date.toInstant();
            ZonedDateTime ldt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
            value = format.format(ldt);
        }
        return value;
    }

    /**
     * @return the cutoff
     */
    public boolean isCutoff()
    {
        return cutoff;
    }

    /**
     * @param cutoff
     *            the cutoff to set
     */
    public void setCutoff(boolean cutoff)
    {
        this.cutoff = cutoff;
    }

    /**
     * @return the format
     */
    public DateTimeFormatter getFormat()
    {
        return format;
    }

    /**
     * @return the resultSize
     */
    public long getResultSize()
    {
        return resultSize;
    }

    /**
     * @param size
     *            result size 
     */
    public void setResultSize(long size)
    {
        resultSize = size ;
    }

    /**
     * @param rs
     *            ResultSet to take result size from
     */
    public void setResultSize(ResultSet rs)
    {
        try
        {
            rs.last();
            resultSize = rs.getRow();
        }
        catch (SQLException e)
        {
            resultSize = -1; // should not happen if called correctly
        }

    }

    /**
     * @return the processedCount
     */
    public long getProcessedCount()
    {
        return processedCount;
    }

    /**
     * @param processedCount
     *            the processedCount to set
     */
    public void setProcessedCount(long processedCount)
    {
        this.processedCount = processedCount;
    }

    protected String getBaseUrl()
    {
        return baseUrl;
    }

    public String getProxyUrl()
    {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl)
    {
        this.proxyUrl = proxyUrl;
    }

}
