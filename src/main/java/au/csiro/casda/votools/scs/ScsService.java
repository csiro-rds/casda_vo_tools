package au.csiro.casda.votools.scs;

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.result.ResultsExtractor;
import au.csiro.casda.votools.result.VoTableResultsExtractor;
import au.csiro.casda.votools.result.VotableError;
import au.csiro.casda.votools.scs.ConeSearchTable.Verbosity;
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
 * A service to run TAP queries against the database.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.NO)
public class ScsService extends Configurable
{
    private ConfigurationRegistry configRegistry;

    /**
     * Name to use in generated VOTable results
     */
    private static final String CASDA_SCS_RESULT_NAME = "CASDA SCS Result";

    /**
     * Template cone search query for a geospatial database - based on the query produced by the ADQL parser. Should be
     * formatted with the following parameters in order: field list, table name, ra field, dec field to produce the
     * query and then have the parameter values supplied of right ascension, declination and radius. All values and
     * fields must be in decimal degrees.
     */
    private static final String QUERY_FORMAT = "SELECT %s FROM %s WHERE '1' = "
            + "(spoint(radians(%s),radians(%s)) @ scircle(spoint(radians(?),radians(?)),radians(?)))";

    private static final String RELEASED_CLAUSE = " AND released_date is not null";

    private static final String ID_UCD = "meta.id;meta.main";
    private static final String ID_UCD_NON_MAIN = "meta.id";
    private static final String RA_UCD = "pos.eq.ra;meta.main";
    private static final String RA_UCD_NON_MAIN = "pos.eq.ra";
    private static final String DEC_UCD = "pos.eq.dec;meta.main";
    private static final String DEC_UCD_NON_MAIN = "pos.eq.dec";

    private static final int DEGREES_IN_ROTATION = 360;
    private static final int MAX_DECLINATION = 90;
    private static final int MIN_DECLINATION = -90;
    private static final int MAX_SCS_VERBOSITY = 3;
    private static final int MIN_SCS_VERBOSITY = 1;
    private static final int INVALID_VERBOSITY = -1;
    private static final int INVALID_RADIUS = -1;
    private static final int INVALID_RA = -1;
    private static final int INVALID_DECLINATION = -91;

    private static final float DEFAULT_MAX_RADIUS = 5f;
    private static final int DEFAULT_MAX_RECORDS = 1000;

    private static Logger logger = LoggerFactory.getLogger(ScsService.class);

    private JdbcTemplate jdbcTemplate;

    private float maxRadius;

    private int maxRecords;

    private final VoTableRepositoryService voTableRepositoryService;

    private Map<String, ConeSearchTable> coneSearchTables;


    private List<String> authTrustedIp;

    private Configuration config;

    private boolean ready;

    /**
     * Constructor
     * 
     * @param voTableRepositoryService
     *            The JPA repositpry providing access to TAP metadata
     * @param configRegistry
     *            The configuration registry
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @Autowired
    public ScsService(VoTableRepositoryService voTableRepositoryService, 
            ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        this.voTableRepositoryService = voTableRepositoryService;
        this.setConfigRegistry(configRegistry);
        // Register for callbacks when configuration changes.
        this.setConfigRegistry(configRegistry);
        configRegistry.register(this);
    }

    /**
     * Checks whether the request is from a trusted ip address, and so we can trust the authorisation information in the
     * header
     * 
     * @param request
     *            the user's request
     * @return true if the the source of the request is a trusted ip address
     */
    public boolean trustAuthHeader(HttpServletRequest request)
    {
        boolean trustAuthHeader = this.authTrustedIp.contains(request.getRemoteAddr());
        logger.info("Request from {}, is from trusted ip address {}", request.getRemoteAddr(), trustAuthHeader);
        return trustAuthHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        ready = false;
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        if (!ready && config != null && config.gtDao() != null && voTableRepositoryService != null
                && voTableRepositoryService.isReady())
        {
            authTrustedIp = config.getList("auth.trusted.ip");

            setJdbcTemplate(config.gtDao().getTemplate());
            EndPoint scsEndPoint = config.getEndPoint("SCS");
            if (scsEndPoint == null)
            {
                return false;
            }
            maxRadius = config.getEndPoint("SCS").getFloat("max.radius", DEFAULT_MAX_RADIUS);
            maxRecords = config.getEndPoint("SCS").getInt("max.records", DEFAULT_MAX_RECORDS);
            ready = true;
            try
            {
                prepareScsMetadata();
            }
            catch(Exception e)
            {
            	logger.error("SCS service is unavailable due to: " + e.getMessage());
                ready = false ;
            }
        }
        return ready;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    protected void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create the cone search metadata based on the TAP metadata in the database.
     * 
     * @return A copy of the produced metadata map. This is a duplicate provided for testing.
     * @throws ConfigurationException if there are configuration problems
     */
    protected Map<String, ConeSearchTable> prepareScsMetadata() throws ConfigurationException
    {
        coneSearchTables = new HashMap<>();
        if (!isReady())
        {
            return coneSearchTables;
        }
        List<TapTable> tapTables = voTableRepositoryService.getTables();
        List<TapColumn> tapColumns = voTableRepositoryService.getColumns();
        for (TapTable table : tapTables)
        {
            if (Boolean.TRUE == table.getScsEnabled())
            {
                ConeSearchTable scsTable = new ConeSearchTable(table);
                String tableName = table.getTableName();
                if (tableName.startsWith(table.getSchema().getSchemaName()))
                {
                    tableName = tableName.substring(table.getSchema().getSchemaName().length() + 1);
                }
                coneSearchTables.put(tableName.toLowerCase(), scsTable);

                for (TapColumn tapColumn : tapColumns)
                {
                    if (tapColumn.getTable().getTableName().equals(table.getTableName()))
                    {
                        addColumnToTable(scsTable, tapColumn);
                    }
                }
            }
        }
        return coneSearchTables;
    }

    /**
     * Add the column to the list of columns to be output at the verbosity level recorded for the column (if any).
     * 
     * @param scsTable
     *            The table the column is part of.
     * @param tapColumn
     *            The column definition.
     */
    protected void addColumnToTable(ConeSearchTable scsTable, TapColumn tapColumn)
    {
        final int verbosityLevel3 = 3;
        String columnName = tapColumn.getDbColumnName();
        int columnOrder = tapColumn.getColumnOrder();
        boolean level1Field = false;
        if (tapColumn.getScsVerbosity() != null)
        {
            switch (tapColumn.getScsVerbosity())
            {
            case 1:
                scsTable.addColumn(Verbosity.LEVEL_1, columnName, columnOrder);
                level1Field = true;
                break;
            case 2:
                scsTable.addColumn(Verbosity.LEVEL_2, columnName, columnOrder);
                break;
            case verbosityLevel3:
                scsTable.addColumn(Verbosity.LEVEL_3, columnName, columnOrder);
                break;
            default:
                logger.error("Unexpected verbosity '{}' for field {}", tapColumn.getScsVerbosity(), tapColumn);
                break;
            }
        }

        String fieldKey = tapColumn.getTable().getDbSchemaName() + "|" + tapColumn.getTable().getDbTableName() + "|"
                + tapColumn.getDbColumnName();

        // The SCS standard requires UCD1 values be used for the id, ra and dec fields. Others will use UCD1+ as normal.
        if (ID_UCD.equalsIgnoreCase(tapColumn.getUcd())
                || (level1Field && ID_UCD_NON_MAIN.equalsIgnoreCase(tapColumn.getUcd()) && scsTable.getIdColumn() == null))
        {
            scsTable.setIdColumn(tapColumn);
            scsTable.putVoTableColumnDef(fieldKey,
                    VoTableResultsExtractor.buildVoTableFieldHeader(tapColumn, "ID_MAIN"));
        }
        else if (RA_UCD.equalsIgnoreCase(tapColumn.getUcd())
                || (level1Field && RA_UCD_NON_MAIN.equalsIgnoreCase(tapColumn.getUcd()) && scsTable.getRaColumn() == null))
        {
            scsTable.setRaColumn(tapColumn);
            scsTable.putVoTableColumnDef(fieldKey,
                    VoTableResultsExtractor.buildVoTableFieldHeader(tapColumn, "POS_EQ_RA_MAIN"));
        }
        else if (DEC_UCD.equalsIgnoreCase(tapColumn.getUcd())
                || (level1Field && DEC_UCD_NON_MAIN.equalsIgnoreCase(tapColumn.getUcd()) && scsTable.getDecColumn() == null))
        {
            scsTable.setDecColumn(tapColumn);
            scsTable.putVoTableColumnDef(fieldKey,
                    VoTableResultsExtractor.buildVoTableFieldHeader(tapColumn, "POS_EQ_DEC_MAIN"));
        }
        else
        {
            scsTable.putVoTableColumnDef(fieldKey, VoTableResultsExtractor.buildVoTableFieldHeader(tapColumn));
        }
    }

    /**
     * Runs a query against our database.
     * 
     * @param params
     *            Request parameters.
     * @param formatStr
     *            The format in which the results should be output.
     * @param writer
     *            The writer to output the results to.
     * @param maxrecs
     *            The maximum number of records to be output. If 0, a header will still be output.
     * @param startTime
     *            the time we started processing this query
     * @throws InterruptedException
     *             If the query was interrupted.
     * @throws IOException
     *             If the result cannot be written to the writer.
     */
    protected void runScsQuery(Map<String, String> params, String formatStr, Writer writer, int maxrecs,
            ZonedDateTime startTime) throws InterruptedException, IOException
    {
        String catalog = params.get(VoKeys.CATALOG).toLowerCase();
        ConeSearchTable coneSearchTable = coneSearchTables.get(catalog);
        if (coneSearchTable == null)
        {
            // This should have already been picked up by validation, but just in case.
            throw new IllegalArgumentException("Unsupported catalog param of " + catalog);
        }
        ResultSetExtractor<Boolean> extractor =
                getExtractor(formatStr, writer, maxrecs, coneSearchTable.getVotableFieldMap());

        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }

        double ra = Double.parseDouble(params.get("ra"));
        double dec = Double.parseDouble(params.get("dec"));
        double sr = Double.parseDouble(params.get("sr"));
        String verbStr = params.get("verb");
        Verbosity verbosity = Verbosity.findLevelForKey(verbStr);
        String fields = coneSearchTable.getSelectFields(verbosity);

        String tableName =
                coneSearchTable.getTable().getDbSchemaName() + "." + coneSearchTable.getTable().getDbTableName();
        if (coneSearchTable.getRaColumn() == null || coneSearchTable.getDecColumn() == null)
        {
            throw new IllegalArgumentException("Catalog " + catalog + " does not have ra and dec fields defined."); 
        }
        String raName = coneSearchTable.getRaColumn().getDbColumnName();
        String decName = coneSearchTable.getDecColumn().getDbColumnName();

        // restrict access to only released data if required
        String queryFormat = QUERY_FORMAT;
        if (Boolean.TRUE == coneSearchTable.getTable().getReleaseRequired())
        {
            queryFormat += RELEASED_CLAUSE;
        }

        String statement = String.format(queryFormat, fields, tableName, raName, decName);

        jdbcTemplate.query(statement, new Object[] { ra, dec, sr }, extractor);

        ZonedDateTime finished = ZonedDateTime.now(ZoneId.of("UTC"));

        long resultSize = ((ResultsExtractor) extractor).getResultSize();
        String cutoff = "none";
        if (resultSize > maxrecs)
        {
            cutoff = maxrecs < maxRecords ? "userSize" : "maxRows";
        }
        String processedCount = Long.toString(((ResultsExtractor) extractor).getProcessedCount());

        logger.info(CasdaVoToolsEvents.E144
                .messageBuilder()
                .addTimeTaken(Duration.between(startTime, finished).toMillis())
                .addAll(Arrays.asList(catalog, params.get(VoKeys.PARAM_QUERY_STRING),
                        CasdaFormatter.formatDateTimeForLog(Date.from(startTime.toInstant())),
                        CasdaFormatter.formatDateTimeForLog(Date.from(finished.toInstant())), processedCount,
                        resultSize, cutoff, params.get(VoKeys.USER_ID))).toString());
    }

    /**
     * Gets the results extractor for a cone search - this will write the results from the cone search as a given output
     * format.
     * 
     * @param formatStr
     *            the requested format
     * @param writer
     *            the stream to write to
     * @param maxrecs
     *            the maximum number of records to include
     * @param map
     * @return the result set extractor
     */
    private ResultSetExtractor<Boolean> getExtractor(String formatStr, Writer writer, int maxrecs,
            Map<String, String> votableFieldMap)
    {
        ResultSetExtractor<Boolean> extractor = new VoTableResultsExtractor(writer, maxrecs, votableFieldMap,
                CASDA_SCS_RESULT_NAME, config.get(ConfigValueKeys.APP_BASE_URL));
        return extractor;
    }

    /**
     * Reports an error in an SCS request by outputting a VOTABLE to the writer.
     * 
     * @param writer
     *            The writer to send the error info to.
     * @param errorMsg
     *            The description of the error.
     * @throws IOException
     *             If the error cannot be written.
     */
    public void reportScsError(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportScsError(CASDA_SCS_RESULT_NAME, errorMsg));
    }

    /**
     * Validate the parameters passed to a query request.
     * 
     * The job to validate
     * 
     * @param paramsMap
     *            The parameters for this job.
     * 
     * @return the validation error message if the job is not valid, null if the job is valid.
     * @throws IOException
     *             If an error cannot be written to the writer.
     */
    protected String validateScsJob(Map<String, String> paramsMap) throws IOException
    {
        String radiusStr = paramsMap.get("sr");
        String rastr = paramsMap.get("ra");
        String decstr = paramsMap.get("dec");
        String verbStr = paramsMap.get("verb");
        String catalog = paramsMap.get(VoKeys.CATALOG);

        if (!StringUtils.isBlank(verbStr))
        {
            int verb = NumberUtils.toInt(verbStr, INVALID_VERBOSITY);
            if (verb < MIN_SCS_VERBOSITY || verb > MAX_SCS_VERBOSITY)
            {
                return "Invalid VERB parameter value: " + verbStr;
            }
        }

        if (StringUtils.isBlank(radiusStr))
        {
            return "Missing radius (sr) parameter";
        }
        else
        {
            float radius = NumberUtils.toFloat(radiusStr, INVALID_RADIUS);
            if (radius > maxRadius || radius <= 0)
            {
                return "Invalid radius parameter value: " + radiusStr + ". Radius must be > 0 and < " + maxRadius;
            }
        }

        if (StringUtils.isBlank(rastr))
        {
            return "Missing RA parameter";
        }
        else
        {
            float ra = NumberUtils.toFloat(rastr, INVALID_RA);
            if (ra > DEGREES_IN_ROTATION || ra < 0)
            {
                return "Invalid RA parameter value: " + rastr;
            }
        }

        if (StringUtils.isBlank(decstr))
        {
            return "Missing DEC parameter";
        }
        else
        {
            float dec = NumberUtils.toFloat(decstr, INVALID_DECLINATION);
            if (dec > MAX_DECLINATION || dec < MIN_DECLINATION)
            {
                return "Invalid DEC parameter value: " + decstr;
            }
        }

        if (StringUtils.isBlank(catalog))
        {
            return "Undefined catalog to search";
        }
        else
        {
            if (!coneSearchTables.containsKey(catalog.toLowerCase()))
            {
                return "Invalid catalog: " + catalog;
            }
            // Check that the table is ready for a cone search query
            ConeSearchTable coneSearchTable = coneSearchTables.get(catalog.toLowerCase());
            if (coneSearchTable.getRaColumn() == null || coneSearchTable.getDecColumn() == null)
            {
                return "Catalog " + catalog + " is not completely configured and connot be queried at this time. "
                        + "It does not have right ascension and declination fields specified.";
            }

        }

        return null;
    }

    /**
     * Process an already validated TAP query and write the result to the supplied writer. If an error is encountered
     * the error will be written in VOTABLE format to the writer.
     *
     * @param writer
     *            The destination for the query output.
     * @param paramsMap
     *            The parameters for this job.
     * @return true if the query was successful, false if an error occurred
     * @throws InterruptedException
     *             if the job is interrupted
     * @throws IOException
     *             if an error occurs using writer
     */
    public boolean processQuery(Writer writer, Map<String, String> paramsMap) throws InterruptedException, IOException
    {
        // only one format for SCS
        String format = OutputFormat.VOTABLE.getDefaultContentType();

        ZonedDateTime start = ZonedDateTime.now();

        // Parameters to be validated
        String scsValidationError = this.validateScsJob(paramsMap);
        if (StringUtils.isNotBlank(scsValidationError))
        {
            this.reportScsError(writer, scsValidationError);
            logger.info(CasdaVoToolsEvents.E143
                    .messageBuilder()
                    .addAll(Arrays.asList(paramsMap.get(VoKeys.CATALOG), paramsMap.get(VoKeys.PARAM_QUERY_STRING),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())), scsValidationError,
                            paramsMap.get(VoKeys.USER_ID))).toString());
            return false;
        }

        try
        {
            this.runScsQuery(paramsMap, format, writer, maxRecords, start);
        }
        catch (Exception e)
        {
            logger.info(
                    CasdaVoToolsEvents.E142
                            .messageBuilder()
                            .addAll(Arrays.asList(paramsMap.get(VoKeys.CATALOG),
                                    paramsMap.get(VoKeys.PARAM_QUERY_STRING),
                                    CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                                    "Unable to run query: " + e.getMessage(), paramsMap.get(VoKeys.USER_ID)))
                            .toString(), e);
            this.reportScsError(writer, "Unable to run query: " + e.getMessage());
            return false;
        }

        return true;
    }

    public Map<String, ConeSearchTable> getCatalogueMap()
    {
        return coneSearchTables;
    }

    /**
     * Refresh the cone search metadata.
     * @throws ConfigurationException if there are configuration problems
     */
    public void refresh() throws ConfigurationException
    {
        voTableRepositoryService.refreshObjectCache();
        prepareScsMetadata();
    }

    public float getMaxRadius()
    {
        return maxRadius;
    }

    public void setMaxRadius(float maxRadius)
    {
        this.maxRadius = maxRadius;
    }

    public int getMaxRecords()
    {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords)
    {
        this.maxRecords = maxRecords;
    }

    public ConfigurationRegistry getConfigRegistry()
    {
        return configRegistry;
    }

    public void setConfigRegistry(ConfigurationRegistry configRegistry)
    {
        this.configRegistry = configRegistry;
    }

}
