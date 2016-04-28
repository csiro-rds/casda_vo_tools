package au.csiro.casda.votools.result;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

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
 * Outputs the results of a query to a CSV or TSV format in accordance with the Table Access Protocol Version 1.0 IVOA
 * Recommendation 2010-03-27.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class CsvTsvResultsExtractor extends ResultsExtractor implements ResultSetExtractor<Boolean>
{
    /**
     * Output types available for the CSV / TSV Results extractor
     * <p>
     * Copyright 2015, CSIRO Australia. All rights reserved.
     */
    public enum OutputType 
    {
        /** Comma separated output type */
        CSV(','),
        /** Tab separated output type */
        TSV('\t');
        
        private final char separator;
        private OutputType(char separator)
        {
            this.separator = separator;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(CsvTsvResultsExtractor.class);

    private final Writer writer;
    private final OutputType outputType;
    private final int maxRec;

    /**
     * Creates a new instance of OutputTapQueryToVoTable for use outputting a single query only. Instances are not
     * reusable.
     * 
     * @param writer
     *            The writer to output the query results to.
     * @param maxRec
     *            The maximum number of records the user has requested.
     * @param outputType
     *            CSV or TSV
     * @param baseUrl
     *            The web address at which this VO Tools instance can be found.
     * @param proxyUrl
     *            The web address at which this VO Proxy instance can be found.
     */
    public CsvTsvResultsExtractor(Writer writer, int maxRec, OutputType outputType, String baseUrl, String proxyUrl)
    {
        super(baseUrl, proxyUrl);
        this.writer = writer;
        this.maxRec = maxRec;
        this.outputType = outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException
    {
        setCutoff(false);
        int columnCount = rs.getMetaData().getColumnCount();
        setProcessedCount(0);
        String error = "";

        try
        {
            outputHeader(rs.getMetaData());
            int[] dataTypes = getDataTypes(rs.getMetaData());
            try
            {
                while (getProcessedCount() < maxRec && rs.next())
                {
                    writer.append(buildRowOutput(rs, columnCount, dataTypes));
                    setProcessedCount(getProcessedCount() + 1);
                }
                setCutoff(!(rs.isLast() || rs.isAfterLast()));
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
     * Build up the response for a single row of data.
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
        StringBuilder rowOutput = new StringBuilder();
        for (int i = 1; i <= columnCount; i++)
        {
            String value = getFieldValue(rs, dataTypes[i], i);
            value = escapeValue(value);
            rowOutput.append(value);
            if (i < columnCount)
            {
                rowOutput.append(outputType.separator);
            }
        }
        rowOutput.append("\r\n");
        return rowOutput.toString();
    }

    /**
     * Escape the string by putting enclosing in double quotes if the string contains the separator character, or double
     * quotes or new line characters.
     * 
     * @param value
     *            the string to escape
     * @return escaped string, if required (eg escaped string: "some,problem\n"string"", non-escaped string: this string
     *         isn't a problem)
     */
    String escapeValue(String value)
    {
        boolean escape = value.indexOf(outputType.separator) >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0;
        if (escape)
        {
            return '"' + value + '"';
        }
        return value;
    }

    /**
     * Appends a header section to the output. This is a row with names for each column.
     * 
     * @param metaData
     *            The result set metadata.
     * @throws SQLException
     *             If the metadata header cannot be retrieved.
     * @throws IOException
     *             If the header cannot be written.
     */
    protected void outputHeader(ResultSetMetaData metaData) throws SQLException, IOException
    {
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++)
        {
            String name = metaData.getColumnName(i).toLowerCase();
            writer.append(escapeValue(name));
            if (i < columnCount)
            {
                writer.append(outputType.separator);
            }
        }
        writer.append("\r\n");
    }

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
    protected void outputFooter(boolean overflow, String error) throws IOException
    {
    }
}
