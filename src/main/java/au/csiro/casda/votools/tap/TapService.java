package au.csiro.casda.votools.tap;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import adql.db.DBChecker;
import adql.db.DBTable;
import adql.db.DefaultDBColumn;
import adql.db.OrderedDbTable;
import adql.db.exception.UnresolvedIdentifiersException;
import adql.parser.ADQLParser;
import adql.parser.ParseException;
import adql.parser.QueryChecker;
import adql.parser.TokenMgrError;
import adql.query.ADQLIterator;
import adql.query.ADQLList;
import adql.query.ADQLObject;
import adql.query.ADQLQuery;
import adql.query.ClauseADQL;
import adql.query.ClauseConstraints;
import adql.query.constraint.ADQLConstraint;
import adql.query.constraint.Comparison;
import adql.query.constraint.ComparisonOperator;
import adql.query.constraint.ConstraintsGroup;
import adql.query.constraint.Exists;
import adql.query.constraint.In;
import adql.query.constraint.IsNull;
import adql.query.from.ADQLTable;
import adql.query.operand.ADQLColumn;
import adql.query.operand.ADQLOperand;
import adql.query.operand.NumericConstant;
import adql.query.operand.StringConstant;
import adql.translator.ADQLTranslator;
import adql.translator.PgSphereTranslator;
import adql.translator.TranslationException;
import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.CsvTsvResultsExtractor;
import au.csiro.casda.votools.result.CsvTsvResultsExtractor.OutputType;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.result.ResultsExtractor;
import au.csiro.casda.votools.result.VoTableResultsExtractor;
import au.csiro.casda.votools.result.VotableError;
import au.csiro.casda.votools.utils.VoKeys;
import au.csiro.casda.votools.utils.SystemTime;

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
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TapService extends Configurable implements SystemTime
{
    private static Logger logger = LoggerFactory.getLogger(TapService.class);

    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");

    /**
     * Asynchronous processing mode
     */
    public static final String SUBMITTED_MODE_ASYNC = "async";

    /**
     * Synchronous processing mode
     */
    public static final String SUBMITTED_MODE_SYNC = "sync";

    /**
     * Name to use in generated VOTable results
     */
    public static final String CASDA_TAP_RESULT_NAME = "CASDA TAP Result";

    private static final String TEXT_XML_FORMAT = "text/xml";
    private static final String MAXIMUM_RECORDS = "tap.max.records";

    /**
     * TAP request parameter map's key for version
     */
    protected static final String STR_KEY_VERSION = "version";
    /**
     * TAP request parameter map's key for language of the query
     */
    public static final String STR_KEY_LANG = "lang";
    /**
     * TAP request parameter map's key for the format of the output requested
     */
    public static final String STR_KEY_FORMAT = "format";

    private static final String STR_VERSION_1_0 = "1.0";

    // valid adql versions (value associated with "lang" key)
    private static final String STR_ADQL = "ADQL";

    /** Query language value for ADQL v2.0 */
    public static final String STR_ADQL_2_0 = "ADQL-2.0";

    private static final String STR_RELEASED_DATE_COLUMN = "released_date";
    private static final String STR_PROJECT_ID_COLUMN = "project_id";

    // message format strings
    private static final String STR_FORMAT_UNSUPPORTED_TAP_VERSION = "Unsupported TAP version: %s";
    private static final String STR_FORMAT_UNKNOWN_QUERY_LANGUAGE = "Unknown query language: %s";
    private static final String STR_FORMAT_INVALID_MAXREC_VALUE = "Invalid MAXREC parameter value: %s";
    private static final String STR_FORMAT_IS_NOT_SUPPORTED = "Format '%s' is not supported by runTapQuery";
    private static final String STR_FORMAT_UNABLE_TO_INTERPRET_QUERY = "Unable to interpret query '%s'";
    private static final String STR_FORMAT_UNABLE_TO_RUN_QUERY = "Unable to run query '%s'";
    private static final String STR_FORMAT_UNEXPECTED_EXCEPTION = "Unexpected exception: %s";
    private static final String STR_MISSING_QUERY_PARAMETER = "Missing QUERY parameter";
    private static final String STR_FORMAT_UNSUPPORTED_FORMAT = "Unsupported FORMAT requested: %s";

    // configurable parameters
    /** log timezone */
    private String logTimezone;
    /** max records in a result set */
    private int maxRecords;
    /** default retention period, seconds */
    private int retentionPeriodDefault;
    /** default execution duration, seconds */
    private int executionDurationDefault;

    private List<String> authTrustedIp;

    private List<String> authTrustedUserId;

    private ConfigurationRegistry configRegistry;

    private VoTableRepositoryService voTableRepositoryService;

    private JdbcTemplate jdbcTemplateSync;

    private JdbcTemplate jdbcTemplateAsync;

    private QueryChecker queryChecker;

    private boolean ready = false;

    private Configuration config;

    /** Map of field definitions for each table and column. */
    private Map<String, String> votableFieldMap;

    private String votableXsl;

    /**
     * Constructor
     * 
     * @param registry
     *            the configuration registry
     * @param voTableRepositoryService
     *            the vo table repository service
     */
    @Autowired
    public TapService(ConfigurationRegistry registry, VoTableRepositoryService voTableRepositoryService)
    {
        this.configRegistry = registry;
        this.voTableRepositoryService = voTableRepositoryService;
    }

    /**
     * This method will set up any objects that depend on values injected from spring.
     * 
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @PostConstruct
    public void init() throws ConfigurationException
    {
        configRegistry.register(this);
        ready = false;
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
        logger.debug("Request from {}, is from trusted ip address {}", request.getRemoteAddr(), trustAuthHeader);
        return trustAuthHeader;
    }

    /**
     * Checks whether the request is from a trusted user id.
     * 
     * @param request
     *            the user's request
     * @return true if the the source of the request is a trusted user id
     */
    public boolean isTrustedUserId(HttpServletRequest request)
    {
        return trustAuthHeader(request)
                && this.authTrustedUserId.contains(request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        this.config = config;
        ready = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        if (config != null && !ready && voTableRepositoryService != null && voTableRepositoryService.isReady()
                && config.gtDao() != null)
        {
            authTrustedIp = config.getList("auth.trusted.ip");
            authTrustedUserId = config.getList("auth.trusted.userId");

            DataSource dataSource = config.gtDao().getTemplate().getDataSource();
            EndPoint tapEndPoint = config.getEndPoint("TAP");
            if (tapEndPoint == null)
            {
            	logger.error("No TAP endpoint is defined.");
                return false;
            }
            jdbcTemplateSync = new JdbcTemplate(dataSource);
            jdbcTemplateSync.setQueryTimeout(tapEndPoint.getInt("tap.sync.timeout"));
            jdbcTemplateAsync = new JdbcTemplate(dataSource);
            jdbcTemplateAsync.setQueryTimeout(tapEndPoint.getInt("tap.async.timeout"));
            maxRecords = tapEndPoint.getInt("tap.max.records");
            logTimezone = tapEndPoint.get("log.timezone");
            executionDurationDefault = tapEndPoint.getInt("tap.execution.duration.default");
            retentionPeriodDefault = tapEndPoint.getInt(ConfigKeys.TAP_RETENTION_PERIOD_DEFAULT.getKey());
            votableXsl = tapEndPoint.get(ConfigKeys.TAP_VOTABLE_XSL.getKey());
            ready = true;
            createDbChecker();
            createVotableFieldMap();
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

    /**
     * Determines the output format to be used based on the user supplied format string. This allows for mime types as
     * well as simple names to be provided by the user.
     * 
     * @param formatStr
     *            The requested format.
     * @return The format to be used.
     */
    public OutputFormat getFormat(String formatStr)
    {
        if (StringUtils.isBlank(formatStr))
        {
            formatStr = TEXT_XML_FORMAT;
        }
        formatStr = formatStr.toLowerCase();

        return OutputFormat.findMatchingFormat(formatStr);
    }

    /**
     * Translates an ADQL query into SQL so that it can be run against a PostgreSQL+pgSphere database.
     * 
     * @param query
     *            The ADQL query to be translated.
     * @param isAdmin
     *            if true, the user has access to all data (released and unreleased); if false, the user has access only
     *            to the given project ids
     * @param projectIds
     *            List of project_ids that the user has access to, that will match the project table id.
     * @return The equivalent sql query.
     * @throws ParseException
     *             If the ADQL cannot be parsed.
     * @throws TranslationException
     *             If the parsed query cannot be translated into SQL.
     */
    protected String generateSqlForQuery(String query, boolean isAdmin, List<Long> projectIds)
            throws ParseException, TranslationException
    {
        String sql = StringUtils.EMPTY;
        ADQLQuery adqlQuery = createAdqlquery(query);

        if (!isAdmin)
        {
            logger.debug("Updating query to include constraints: {}", query);
            addAuthorisationConstraintsToQuery(adqlQuery, projectIds);
        }

        // Translate:
        // creates a translator that will ignore case
        ADQLTranslator translator = new PgSphereTranslator(false);
        sql = translator.translate(adqlQuery);

        return sql;
    }

    private ADQLQuery createAdqlquery(String query) throws ParseException, TranslationException
    {
        ADQLParser parser = new ADQLParser();
        parser.disable_tracing();
        parser.setQueryChecker(queryChecker);

        return parser.parseQuery(query);
    }

    /**
     * Identify single table queries and return table name else return blank
     * 
     * @param query
     *            Query to be processed
     * @return Table name if single table query
     * @throws ParseException
     *             If the ADQL cannot be parsed.
     * @throws TranslationException
     *             If the parsed query cannot be translated into SQL.
     */
    private String getSingleTableName(String query) throws ParseException, TranslationException
    {
        ADQLQuery adqlQuery = createAdqlquery(query);
        int size = adqlQuery.getFrom().getTables().size();
        if (size > 1)
        {
            return "";
        }
        return adqlQuery.getFrom().getTables().get(0).getFullTableName();
    }

    private void addAuthorisationConstraintsToQuery(ADQLQuery query, List<Long> projectIds)
    {
        // update the query to only include released data or data
        List<ADQLTable> mainTables = query.getFrom().getTables();
        List<ADQLConstraint> releasedDateConstraints = generateAuthorisationConstraintsForTables(mainTables,
                projectIds);
        for (ADQLConstraint releasedDateConstraint : releasedDateConstraints)
        {
            query.getWhere().add(ClauseConstraints.AND, releasedDateConstraint);
        }
        ADQLIterator fromIterator = query.getFrom().adqlIterator();
        updateIterator(fromIterator, projectIds);

        ADQLIterator whereIterator = query.getWhere().adqlIterator();
        updateIterator(whereIterator, projectIds);

    }

    private void updateIterator(ADQLIterator iterator, List<Long> projectIds)
    {
        while (iterator.hasNext())
        {
            ADQLObject obj = iterator.next();
            if (obj instanceof In)
            {
                In inClause = (In) obj;
                if (inClause.getSubQuery() != null)
                {
                    addAuthorisationConstraintsToQuery(inClause.getSubQuery(), projectIds);
                }
            }
            else if (obj instanceof Exists)
            {
                Exists existsClause = (Exists) obj;
                if (existsClause.getSubQuery() != null)
                {
                    addAuthorisationConstraintsToQuery(existsClause.getSubQuery(), projectIds);
                }
            }
            else
            {
                ADQLIterator adqlIterator = obj.adqlIterator();
                updateIterator(adqlIterator, projectIds);
            }
        }
    }

    private List<ADQLConstraint> generateAuthorisationConstraintsForTables(List<ADQLTable> tables,
            List<Long> projectIds)
    {
        List<ADQLConstraint> constraints = new ArrayList<>();
        for (ADQLTable table : tables)
        {
            if (table.getSubQuery() != null)
            {
                addAuthorisationConstraintsToQuery(table.getSubQuery(), projectIds);
            }
            else
            {
                for (TapTable tapTable : voTableRepositoryService.getTables())
                {
                    if (table.getDBLink() != null
                            && tapTable.getDbSchemaName().equals(table.getDBLink().getDBSchemaName())
                            && tapTable.getDbTableName().equals(table.getDBLink().getDBName())
                            && Boolean.TRUE.equals(tapTable.getReleaseRequired()))
                    {

                        String tableref = (StringUtils.isNotBlank(table.getAlias())) ? table.getAlias()
                                : table.getFullTableName();

                        ADQLColumn releaseDateColumn = new ADQLColumn(tableref, STR_RELEASED_DATE_COLUMN);
                        if (hasReleasedDateCol(tapTable))
                        {
                            // Table access is controlled at row level by a released_date column in the table.
                            ADQLConstraint includeReleasedData = new IsNull(releaseDateColumn, true);
    
                            // for embargo'd ASKAP Level 7 collections we want records 
                            // that have a released date and are less than or equal to today's date
                            ADQLConstraint embargoReleasedData =
                                    new Comparison(releaseDateColumn, ComparisonOperator.LESS_OR_EQUAL,
                                            new StringConstant(getCurrentUTCDateTime().toString()));
    
                            ConstraintsGroup embargoReleaseDateAndIncludeReleasedDataConstraint = new ConstraintsGroup();
                            embargoReleaseDateAndIncludeReleasedDataConstraint.add(includeReleasedData);
                            embargoReleaseDateAndIncludeReleasedDataConstraint.add(ConstraintsGroup.AND,
                                    embargoReleasedData);
    
                            if (CollectionUtils.isNotEmpty(projectIds))
                            {
                                ADQLList<ADQLOperand> adqlList = new ClauseADQL<>();
                                for (Long projectId : projectIds)
                                {
                                    NumericConstant id = new NumericConstant(projectId);
                                    adqlList.add(id);
                                }
                                ConstraintsGroup projectOrReleasedConstraint = new ConstraintsGroup();
                                ADQLConstraint includeProjectData =
                                        new In(new ADQLColumn(tableref, STR_PROJECT_ID_COLUMN), adqlList);
                                projectOrReleasedConstraint.add(includeProjectData);
                                projectOrReleasedConstraint.add(ConstraintsGroup.OR,
                                        embargoReleaseDateAndIncludeReleasedDataConstraint);
                                constraints.add(projectOrReleasedConstraint);
                            }
                            else
                            {
                                constraints.add(embargoReleaseDateAndIncludeReleasedDataConstraint);
                            }
                        }
                        else
                        {
                            // Table access is controlled by the released_date in the table metadata
                            if (isWholeTableEmbargoedForUser(tapTable, projectIds))
                            {
                                // Add a 'false' constraint (2 < 1) to block any data from being returned
                                ADQLConstraint emptyResultConstraint = new Comparison(new NumericConstant(2),
                                        ComparisonOperator.LESS_THAN, new NumericConstant(1));
                                constraints.add(emptyResultConstraint);
                            }

                        }
                    }
                }
            }
        }
        return constraints;
    }

    private boolean isWholeTableEmbargoedForUser(TapTable tapTable, List<Long> userProjectIds)
    {
        // Check if the embargo has expired
        if (tapTable.getReleaseDate() != null && getCurrentUTCDateTime().isAfter(tapTable.getReleaseDate()))
        {
            return false;
        }
        
        // For level 7 tables we can get project code from schema name - but need to translate to numeric proj id
        List<String> projectCodes = Collections.singletonList(tapTable.getSchema().getSchemaName());
        List<Long> idsFromCodes =
                voTableRepositoryService.fetchProjectIdsFromCodes(projectCodes, config.gtDao().getSchema());
        
        if (!idsFromCodes.isEmpty() && userProjectIds != null && userProjectIds.contains(idsFromCodes.get(0)))
        {
            // The user has access to this project's data
            return false;
        }

        return true;
    }

    private boolean hasReleasedDateCol(TapTable tapTable)
    {
        for (TapColumn column : tapTable.getColumns())
        {
            if (column.getId().getColumnName().equals(STR_RELEASED_DATE_COLUMN))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Runs a query against our database.
     * 
     * @param sqlQuery
     *            The SQL query to be run.
     * @param format
     *            The output format in which the results should be returned.
     * @param writer
     *            The writer to output the results to.
     * @param maxrecs
     *            The maximum number of records to be output. If 0, a header will still be output.
     * @param params
     *            The parameter map.
     * @param started
     *            The time the job processing was started.
     * @param singleTableName
     *            Name of the table if the query includes only one table.
     * @param extraMetaDataMap
     *            The map of metadata to be included in the query result. May be null
     * @param customVotableFieldMap 
     *            The map of field definitions to be appended when using VOTable format. 
     * @throws InterruptedException
     *             If the query was interrupted.
     * @throws IOException
     *             If the result cannot be written to the writer.
     */
    void runTapQuery(String sqlQuery, OutputFormat format, Writer writer, int maxrecs, Map<String, String> params,
            ZonedDateTime started, String singleTableName, Map<String, String[]> extraMetaDataMap, 
            Map<String, String> customVotableFieldMap)
            throws InterruptedException, IOException
    {
        int recsLimit = Math.min(maxrecs, maxRecords);

        // create map of metadata properties
        LinkedHashMap<String, String[]> metaDataMap = new LinkedHashMap<String, String[]>();
        if (extraMetaDataMap != null)
        {
            metaDataMap.putAll(extraMetaDataMap);
        }
        String[] metaDataKeys = new String[] { "instrument", "server", "serviceShortName", "serviceTitle", "identifier",
                "servicePublisher", "furtherInformation", "contactPerson", "copyright" };
        for (String key : metaDataKeys)
        {
            String entry = config.get(Configuration.METADATA_PREFIX + key);
            if (StringUtils.isNotBlank(entry))
            {
                String[] values = entry.split("\\|");
                if (values.length > 1)
                {
                    metaDataMap.put(key, values);
                }
            }
        }

        if (params.get(VoKeys.STR_KEY_SIAP_QUERY) != null)
        {
            metaDataMap.put("SIAP query",
                    new String[] { StringEscapeUtils.escapeXml10(params.get(VoKeys.STR_KEY_SIAP_QUERY)),
                            "SIAP Query submitted by the user" });
        }
        else if (params.get(VoKeys.STR_KEY_SSAP_QUERY) != null)
        {
            metaDataMap.put("SSAPQuery",
                    new String[] { StringEscapeUtils.escapeXml10(params.get(VoKeys.STR_KEY_SSAP_QUERY)),
                            "SSAP Query submitted by the user" });
        }
        else
        {
            metaDataMap.put("query",
                    new String[] { StringEscapeUtils.escapeXml10(params.get(VoKeys.STR_KEY_ADQL_QUERY)),
                            "Query submitted by the user" });
        }
        metaDataMap.put("executionTime", new String[] { started.toString(), "Time taken to execute query" });
        metaDataMap.put("datetimeRequested",
                new String[] { this.format.format(started), "Date and time that the request was received" });

        // singleTableName is not blank only when single table query is requested

        TapTable tapTable = null;
        if (StringUtils.isNotBlank(singleTableName))
        {
            tapTable = voTableRepositoryService.getTableByName(singleTableName);
        }

        if (tapTable != null)
        {
            // @formatter:off
            metaDataMap.put("tableName", new String[] { tapTable.getTableName(), "Name of the table queried" });
            metaDataMap.put("tableDescription",
                    new String[] { tapTable.getDescription(), "Description of the table queried" });
            metaDataMap.put("tableLongDescription",
                    new String[] {
                            StringUtils.isNotBlank(tapTable.getDescriptionLong()) ? tapTable.getDescriptionLong() : " ",
                            "Long description of the table queried" });
            // @formatter:on

            if (StringUtils.isNotBlank(tapTable.getParams()))
            {
                Arrays.asList(tapTable.getParams().split("\\|")).stream().map(p -> p.split(" : ")).forEach(p -> {
                    p[0] = p[0].replace(" ", "");
                    metaDataMap.put(p[0],
                            new String[] { p[1], "Parameter supplied with Level 7 table (Derived Catalogues)" });
                });
            }
        }

        String baseUrl = config.get(ConfigValueKeys.APP_BASE_URL);
        String proxyUrl = config.get(ConfigValueKeys.DATALINK_BASE_URL);

        ResultSetExtractor<Boolean> extractor;
        switch (format)
        {
        case VOTABLE:
            String voTableHeading = params.get(VoKeys.VO_TABLE_HEADING);
            if (StringUtils.isBlank(voTableHeading))
            {
                voTableHeading = CASDA_TAP_RESULT_NAME;
            }
            boolean proxiedOutput = StringUtils.isNotBlank(params.get(VoKeys.USER_ID))
                    && !VoKeys.ANONYMOUS_USER.equalsIgnoreCase(params.get(VoKeys.USER_ID));
            extractor = new VoTableResultsExtractor(writer, maxrecs, customVotableFieldMap, voTableHeading, metaDataMap,
                    baseUrl, proxyUrl, proxiedOutput, votableXsl);
            break;

        case CSV:
            extractor = new CsvTsvResultsExtractor(writer, maxrecs, OutputType.CSV, baseUrl, proxyUrl);
            break;

        case TSV:
            extractor = new CsvTsvResultsExtractor(writer, maxrecs, OutputType.TSV, baseUrl, proxyUrl);
            break;

        default:
            throw new IllegalArgumentException(String.format(STR_FORMAT_IS_NOT_SUPPORTED, format.toString()));
        }
        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }

        String mode = params.get(VoKeys.SUBMITTED_MODE);

        TapStatementCreator tsc = new TapStatementCreator(sqlQuery);
        if (TapService.SUBMITTED_MODE_SYNC.equals(mode))
        {
            jdbcTemplateSync.query(tsc, extractor);
        }
        else
        {
            jdbcTemplateAsync.query(tsc, extractor);
        }

        ZonedDateTime submitted = ZonedDateTime.parse(params.get(VoKeys.SUBMITTED_TIME));
        ZonedDateTime finished = now();

        int resultSizeInt = (int) ((ResultsExtractor) extractor).getResultSize();
        String cutoff = "none";
        if (resultSizeInt > recsLimit)
        {
            cutoff = maxrecs < maxRecords ? "userSize" : "maxRows";
        }
        String resultSize = Integer.toString(resultSizeInt);
        String processedCount = Long.toString(((ResultsExtractor) extractor).getProcessedCount());
        String userId = params.get(VoKeys.USER_ID);

        logger.info(
                CasdaVoToolsEvents.E062.messageBuilder().addTimeTaken(Duration.between(started, finished).toMillis())
                        .addAll(Arrays.asList(params.get(VoKeys.STR_KEY_ADQL_QUERY),
                                CasdaFormatter.formatDateTimeForLog(Date.from(submitted.toInstant())),
                                CasdaFormatter.formatDateTimeForLog(Date.from(started.toInstant())),
                                CasdaFormatter.formatDateTimeForLog(Date.from(finished.toInstant())), processedCount,
                                resultSize, cutoff, mode, userId))
                        .toString());
    }

    /**
     * Reports an error in a TAP request by outputting a VOTABLE to the writer.
     *
     * @param writer
     *            The writer to send the error info to.
     * @param errorMsg
     *            The description of the error.
     * @throws IOException
     *             If the error cannot be written.
     */
    public void reportTapError(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportError(CASDA_TAP_RESULT_NAME, errorMsg));
    }

    /**
     * Trigger refreshing the table access metadata.
     */
    public void refresh()
    {
        voTableRepositoryService.refreshObjectCache();
        setReady(false);
    }

    /**
     * Create a new DBChecker instance populated with the current TAP metadata. The DBChecker will then be able to limit
     * queries to allowed tables and columns.
     * 
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    void createDbChecker() throws ConfigurationException
    {
        // List all available tables:
        List<DBTable> dbTableList = new ArrayList<DBTable>();
        if (isReady())
        {
            List<TapTable> tapTables = voTableRepositoryService.getTables();
            List<TapColumn> tapColumns = voTableRepositoryService.getColumns();
            for (TapTable tapTable : tapTables)
            {
                String tableName = tapTable.getTableName();
                String schemaPrefix = tapTable.getSchema().getSchemaName() + ".";
                if (tableName.startsWith(schemaPrefix))
                {
                    tableName = tableName.substring(schemaPrefix.length());
                }
                OrderedDbTable dbTable = new OrderedDbTable(null, null, tapTable.getDbSchemaName(),
                        tapTable.getSchema().getSchemaName(), tapTable.getDbTableName(), tableName);
                dbTableList.add(dbTable);

                // make sure the columns are added in order
                tapColumns.sort(new Comparator<TapColumn>()
                {
                    @Override
                    public int compare(TapColumn o1, TapColumn o2)
                    {
                        return Integer.compare(o1.getColumnOrder(), o2.getColumnOrder());
                    }

                });
                for (TapColumn tapColumn : tapColumns)
                {
                    if (!tapTable.getTableName().equals(tapColumn.getTable().getTableName()))
                    {
                        continue;
                    }
                    String columnName = tapColumn.getId().getColumnName();
                    if (columnName.matches("^\".+\"$"))
                    {
                        columnName = columnName.substring(1, columnName.length() - 1);
                    }
                    dbTable.addColumn(new DefaultDBColumn(tapColumn.getDbColumnName(), columnName, dbTable));
                }

            }
        }
        queryChecker = new DBChecker(dbTableList);
    }

    /**
     * Build up a map of votable compliant field entries for each column that can be retrieved. This map is cached for
     * the runtime of the application.
     *
     * Issue with the postgres driver:
     * http://stackoverflow.com/questions/9250517/is-it-correct-that-resultset-getmetadata
     * -gettablenamecol-of-postgresqls-jdb We get back an empty table name and thus the map lookup won't work. I left
     * the map in place in the hope that they come up with a workable solution in the future.
     *
     * @return The map of fields for each column.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    protected Map<String, String> createVotableFieldMap() throws ConfigurationException
    {
        Map<String, String> fieldMap = new HashMap<>();

        if (isReady())
        {
            // List all available columns
            List<TapColumn> tapColumns = voTableRepositoryService.getColumns();
            for (TapColumn tapColumn : tapColumns)
            {
                fieldMap.put(
                        tapColumn.getTable().getDbSchemaName() + "|" + tapColumn.getTable().getDbTableName() + "|"
                                + tapColumn.getId().getColumnName().toLowerCase(),
                        VoTableResultsExtractor.buildVoTableFieldHeader(tapColumn));
            }
        }
        votableFieldMap = fieldMap;
        return votableFieldMap;
    }

    /**
     * Checks an ADQL query to ensure it is valid. If any errors are found they will be reported as a VOTABLE to the
     * supplied writer.
     * 
     * @param isAdmin
     *            if true, the user has access to all data (released and unreleased); if false, the user has access only
     *            to the given project ids
     * @param query
     *            The ADQL query to be checked.
     * @param params
     *            The parameters map.
     * @param writer
     *            Client output writer.
     * @param started
     *            The time query processing was started.
     * @param projectIds
     *            List of allowed project to fetch data from .
     * @return true if the query is valid, false if errors were reported.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    boolean validateQuery(boolean isAdmin, String query, Map<String, String> params, Writer writer,
            ZonedDateTime started, List<Long> projectIds) throws ConfigurationException
    {
        boolean result = false;
        StringBuilder errMsg = null;

        try
        {
            try
            {
                generateSqlForQuery(query, isAdmin, projectIds);
                result = true;
            }
            catch (UnresolvedIdentifiersException uie)
            {
                // CASDA-4489 if the table is new, the dbChecker could need a refresh
                if (uie.getErrors() != null && uie.getErrors().hasNext())
                {
                    String message = uie.getErrors().next().getMessage();
                    for (TapTable table : voTableRepositoryService.getTables())
                    {
                        if (message.contains(table.getTableName()))
                        {
                            // updates the cache of database tables for adql query parsing
                            createDbChecker();
                            try
                            {
                                generateSqlForQuery(query, isAdmin, projectIds);
                                result = true;
                                uie = null;
                            }
                            catch (UnresolvedIdentifiersException uie2)
                            {
                                uie = uie2;
                            }
                            break;
                        }
                    }
                }

                if (uie != null)
                {
                    errMsg = new StringBuilder();
                    for (Iterator<ParseException> iterator = uie.getErrors(); iterator.hasNext();)
                    {
                        ParseException ex = iterator.next();
                        errMsg.append(ex.getMessage() + StringUtils.SPACE + ex.getPosition());
                        if (iterator.hasNext())
                        {
                            errMsg.append(StringUtils.LF);
                        }
                    }
                    logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg.toString()), uie);
                }
            }
        }
        catch (ParseException | TranslationException | TokenMgrError e1)
        {
            errMsg = new StringBuilder().append(e1.getMessage());
            logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg.toString()), e1);
        }

        if (!result && errMsg != null)
        {
            try
            {
                this.reportTapError(writer, String.format(STR_FORMAT_UNABLE_TO_INTERPRET_QUERY, errMsg.toString()));
            }
            catch (IOException e)
            {
                logger.error(formFailedMsg(CasdaVoToolsEvents.E098, params, started,
                        String.format(STR_FORMAT_UNEXPECTED_EXCEPTION, e.getMessage())), e);
            }
        }

        return result;
    }

    /**
     * Validate the parameters passed to a query request.
     * 
     * @param isAdmin
     *            if true, the user has access to all data (released and unreleased); if false, the user has access only
     *            to the given project ids
     * @param params
     *            The job parameters to validate
     * @param outputFormat
     *            The selected output format to use for this job
     * @param writer
     *            The output stream to send parameters to.
     * @param started
     *            The time query processing was started.
     * @param projectIds
     *            List of allowed project to fetch data from .
     * @return true if the job is valid, false if not.
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    protected boolean validateTapJob(boolean isAdmin, Map<String, String> params, OutputFormat outputFormat,
            Writer writer, ZonedDateTime started, List<Long> projectIds) throws ConfigurationException
    {
        String version = params.get(STR_KEY_VERSION);
        String lang = params.get(STR_KEY_LANG);
        String query = params.get(VoKeys.STR_KEY_ADQL_QUERY);
        String maxRecValue = params.get(VoKeys.STR_KEY_MAXREC);
        String format = params.get(STR_KEY_FORMAT);
        String errMsg = null;

        if (version != null && !STR_VERSION_1_0.equals(version))
        {
            errMsg = String.format(STR_FORMAT_UNSUPPORTED_TAP_VERSION, version);
            logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg));
        }
        else if (lang == null || !(STR_ADQL.equals(lang) || STR_ADQL_2_0.equals(lang)))
        {
            errMsg = String.format(STR_FORMAT_UNKNOWN_QUERY_LANGUAGE, lang);
            logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg));
        }
        else if (StringUtils.isBlank(query))
        {
            errMsg = STR_MISSING_QUERY_PARAMETER;
            logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg));
        }
        else if (outputFormat == null)
        {
            // outputFormat defaults to votable so this will only happen with unrecognised format
            errMsg = String.format(STR_FORMAT_UNSUPPORTED_FORMAT, format);
            logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg));
        }

        if (StringUtils.isNotBlank(maxRecValue))
        {
            try
            {
                Integer.parseInt(maxRecValue);
            }
            catch (NumberFormatException e)
            {
                errMsg = String.format(STR_FORMAT_INVALID_MAXREC_VALUE, maxRecValue);
                logger.info(formFailedMsg(CasdaVoToolsEvents.E061, params, started, errMsg));
            }
        }

        if (errMsg != null)
        {
            try
            {
                this.reportTapError(writer, errMsg);
            }
            catch (IOException e)
            {
                logger.error(formFailedMsg(CasdaVoToolsEvents.E098, params, started,
                        String.format(STR_FORMAT_UNEXPECTED_EXCEPTION, e.getMessage())), e);
            }
            return false;
        }

        return validateQuery(isAdmin, query, params, writer, started, projectIds);
    }

    /**
     * Validate and process TAP query and write the result to the supplied writer. If an error is encountered the error
     * will be written in VOTABLE format to the writer.
     *
     * @param writer
     *            The destination for the query output.
     * @param paramsMap
     *            The parameters for this job.
     * @return true if the query was successful, false if an error occurred
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public boolean processQuery(Writer writer, Map<String, String> paramsMap) throws ConfigurationException
    {
        return processQuery(writer, paramsMap, null, new ArrayList<>());
    }

    /**
     * Validate and process TAP query and write the result to the supplied writer. If an error is encountered the error
     * will be written in VOTABLE format to the writer.
     *
     * @param writer
     *            The destination for the query output.
     * @param paramsMap
     *            The parameters for this job.
     * @param metaDataMap
     *            The map of metadata to be included in the query result. May be null
     * @param uploadedTables
     *            The list of tables the user has supplied for querying.
     * @return true if the query was successful, false if an error occurred
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public boolean processQuery(Writer writer, Map<String, String> paramsMap, Map<String, String[]> metaDataMap,
            List<UploadedTable> uploadedTables) throws ConfigurationException
    {
        return processQuery(writer, paramsMap, metaDataMap, uploadedTables, votableFieldMap);
    }
    
    /**
     * Validate and process TAP query and write the result to the supplied writer. If an error is encountered the error
     * will be written in VOTABLE format to the writer.
     * 
     * @param writer
     *            The destination for the query output.
     * @param paramsMap
     *            The parameters for this job.
     * @param metaDataMap
     *            The map of metadata to be included in the query result. May be null
     * @param uploadedTables
     *            The list of tables the user has supplied for querying.
     * @param customVotableFieldMap
     *            The map of field definitions to be appended when using VOTable format. 
     * @return true if the query was successful, false if an error occurred
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public boolean processQuery(Writer writer, Map<String, String> paramsMap, Map<String, String[]> metaDataMap,
            List<UploadedTable> uploadedTables, Map<String, String> customVotableFieldMap) throws ConfigurationException
    {
        ZonedDateTime started = now();
        String query = paramsMap.get(VoKeys.STR_KEY_ADQL_QUERY);
        String maxRecValue = paramsMap.get(VoKeys.STR_KEY_MAXREC);
        String format = paramsMap.get(STR_KEY_FORMAT);
        OutputFormat outputFormat = this.getFormat(format);
        String errMsg = null;
        boolean result = false;

        List<String> projectCodes = new ArrayList<String>();

        // if the user has access to projects, these will be in the the params map. The user details are only added to
        // the params by the controller if the request is from a trusted IP address.
        String projectCodesStr = paramsMap.get(VoKeys.USER_PROJECTS);
        boolean isCasdaAdmin = isCasdaAdministrator(projectCodesStr);

        // compose projects id for codes if any (and if the user isn't an admin)
        List<Long> projectIds = null;
        if (!isCasdaAdmin && StringUtils.isNotBlank(projectCodesStr))
        {
            projectCodes.addAll(Arrays.asList(projectCodesStr.replace(" ", "").split(",")));
            projectIds = voTableRepositoryService.fetchProjectIdsFromCodes(projectCodes, config.gtDao().getSchema());
        }

        String sqlForQuery = null;
        try
        {
            // Parameters to be validated
            if (this.validateTapJob(isCasdaAdmin, paramsMap, outputFormat, writer, started, projectIds))
            {
                int maxRec = config.getEndPoint("TAP").getInt(MAXIMUM_RECORDS);
                if (StringUtils.isNotBlank(maxRecValue))
                {
                    maxRec = Integer.parseInt(maxRecValue);
                }
                sqlForQuery = this.generateSqlForQuery(query, isCasdaAdmin, projectIds);
                String singleTableName = getSingleTableName(query);

                logger.debug("Updated query for isCasdaAdmin={}: {}", isCasdaAdmin, sqlForQuery);
                runTapQuery(sqlForQuery, outputFormat, writer, maxRec, paramsMap, started, singleTableName,
                        metaDataMap, customVotableFieldMap);
                result = true;
            }
        }
        catch (ParseException | TranslationException | TokenMgrError e1)
        {
            errMsg = String.format(STR_FORMAT_UNABLE_TO_INTERPRET_QUERY, e1.getMessage());
            logger.error(formFailedMsg(CasdaVoToolsEvents.E060, paramsMap, started, errMsg), e1);
        }
        catch (InterruptedException | IOException | DataAccessException e2)
        {
            errMsg = String.format(STR_FORMAT_UNABLE_TO_RUN_QUERY, e2.getMessage());
            if (e2 instanceof DataAccessResourceFailureException
                    && e2.getMessage().contains("canceling statement due to user request"))
            {
                errMsg = "Could not finish query due to timeout.";
            }
            logger.error(formFailedMsg(CasdaVoToolsEvents.E060, paramsMap, started, errMsg), e2);
        }

        if (errMsg != null)
        {
            try
            {
                this.reportTapError(writer, errMsg);
            }
            catch (IOException ee)
            {
                logger.error(formFailedMsg(CasdaVoToolsEvents.E098, paramsMap, started,
                        String.format(STR_FORMAT_UNEXPECTED_EXCEPTION, ee.getMessage())), ee);
            }
        }

        return result;
    }

    /**
     * Identify which, if any, version of the IVOA ObsCore spec that this database exports. 
     * @return The version string (1.1 or 1.0) if obscore is implemented, or null if it isn't implemented.
     */
    public String getObsCoreVersion()
    {
        List<TapTable> tapTables = voTableRepositoryService.getTables();
        List<TapColumn> tapColumns = voTableRepositoryService.getColumns();
        
        TapTable obscore = getObsCoreTable(tapTables);
        if (obscore == null)
        {
            return null;
        }
                
        for (TapColumn tapColumn : tapColumns)
        {
            if (tapColumn.getTable().getTableName().equals(obscore.getTableName()))
            {
                TapColumnPK colId = tapColumn.getId();
                if ("s_xel1".equalsIgnoreCase(colId.getColumnName()))
                {
                    return "1.1";
                }
            }
        }
        return "1.0";
    }
    
    private TapTable getObsCoreTable(List<TapTable> tapTables)
    {
        for (TapTable table : tapTables)
        {
            if ("ivoa.obscore".equalsIgnoreCase(table.getTableName()))
            {
                return table;
            }
        }
        return null;
    }
    
    /**
     * Find the metadata key for the ADQL table. This is composed from the database schema and table names.
     * @param adqlSchema The ADQL schema of the target table.
     * @param adqlTableName The ADQL table name of the target table.
     * @return The metadata key e.g. schema|table
     */
    public String getTableMetdataKey(String adqlSchema, String adqlTableName)
    {
        String adqlKey = String.format("%s.%s", adqlSchema, adqlTableName);
        List<TapTable> tapTables = voTableRepositoryService.getTables();
        for (TapTable tapTable : tapTables)
        {
            if (adqlKey.equalsIgnoreCase(tapTable.getTableName()))
            {
                return String.format("%s|%s", tapTable.getDbSchemaName(), tapTable.getDbTableName());
            }
        }
        
        return "";
    }
    
    /**
     * Forms a log message reporting a failure
     * 
     * @param eventType
     *            Type of the event being logged
     * @param paramsMap
     *            Parameters passed to the service
     * @param started
     *            Time when job processing was started
     * @param customMsg
     *            Custom message to add to the log record
     * @return Log line to pass to the logger
     */
    private String formFailedMsg(CasdaVoToolsEvents eventType, Map<String, String> paramsMap, ZonedDateTime started,
            String customMsg)
    {
        String mode = paramsMap.get(VoKeys.SUBMITTED_MODE);
        String submittedStr = paramsMap.get(VoKeys.SUBMITTED_TIME);
        submittedStr = CasdaFormatter.formatDateTimeForLog(Date.from(ZonedDateTime.parse(submittedStr).toInstant()));
        String startedStr = CasdaFormatter.formatDateTimeForLog(Date.from(started.toInstant()));
        String query = paramsMap.containsKey(VoKeys.STR_KEY_ADQL_QUERY) ? paramsMap.get(VoKeys.STR_KEY_ADQL_QUERY)
                : "null";
        String userId = paramsMap.get(VoKeys.USER_ID);
        return eventType.messageBuilder().addCustomMessage(customMsg)
                .addAll(Arrays.asList(query, submittedStr, startedStr, mode, userId)).toString().trim();
    }

    /**
     * Check whether the user is a casda admin based on the project codes
     * 
     * @param projectCodes
     *            Project codes that the user is allowed to access.
     * @return true if projects codes string suggest its a casda admin
     */
    public boolean isCasdaAdministrator(String projectCodes)
    {
        return (VoKeys.STR_PROJECT_CODES_ALL.equals(projectCodes));
    }

    /**
     * Returns zoned date time using time zone configured in application properties file
     * 
     * @return zoned time in desired time zone
     */
    public ZonedDateTime now()
    {
        String timeZone = getLogTimezone();
        ZoneId id = ZoneId.of(timeZone);
        return ZonedDateTime.now(id);
    }

    public String getLogTimezone()
    {
        return logTimezone;
    }

    public int getRetentionPeriodDefault()
    {
        return retentionPeriodDefault;
    }

    public int getExecutionDurationDefault()
    {
        return executionDurationDefault;
    }

    public Configuration getConfig()
    {
        return config;
    }

    public void setReady(boolean b)
    {
        ready = b;
    }

    public JdbcTemplate getJdbcTemplateSync()
    {
        return jdbcTemplateSync;
    }

    public void setJdbcTemplateSync(JdbcTemplate jdbcTemplateSync)
    {
        this.jdbcTemplateSync = jdbcTemplateSync;
    }

    public JdbcTemplate getJdbcTemplateAsync()
    {
        return jdbcTemplateAsync;
    }

    public void setJdbcTemplateAsync(JdbcTemplate jdbcTemplateAsync)
    {
        this.jdbcTemplateAsync = jdbcTemplateAsync;
    }

    /**
     * A prepared statement creator configured to allow streaming of results. Each instance is responsible for creating
     * the statement for a specific query. They should not be reused.
     */
    static class TapStatementCreator implements PreparedStatementCreator
    {
        private String query;

        /**
         * Create a new TapStatementCreator instance for a specific query.
         * 
         * @param query
         *            The query to be run.
         */
        public TapStatementCreator(String query)
        {
            this.query = query;
        }

        /** {@inheritDoc} */
        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException
        {
            con.setAutoCommit(false);
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
            final int resultFetchSizeRows = 1000;
            preparedStatement.setFetchSize(resultFetchSizeRows);
            return preparedStatement;
        }
    }

    @Override
    public DateTime getCurrentUTCDateTime()
    {
        return new DateTime(DateTimeZone.UTC);
    }

    /**
     * @return A copy of the votable field map.
     */
    public Map<String, String> getVotableFieldMap()
    {
        return new HashMap<>(votableFieldMap);
    }
}
