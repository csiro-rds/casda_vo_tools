package au.csiro.casda.votools.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

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
 * Root of VO Tools configuration Contains DB connection information and array of configurations of end points.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class Configuration extends Options
{
    /** Actions to use configuration object for */
    public enum Action
    {
        /** do nothing */
        NONE,
        /** read current configuration */
        CURRENT,
        /** export database structure and environment parameters */
        EXPLORE,
        /** change the current configuration and database structure to make it exactly as configured */
        APPLY;
    };

    /** Level of changes allowed to perform when implementing the configuration */
    public enum Change
    {
        /** no changes allowed */
        NONE,
        /** minor changes not involving time consuming data operations or data loss */
        UPDATE,
        /** changes potentially involving time consuming data operations, but no data loss */
        REINDEX,
        /** changes involving dropping columns, indices or constraints */
        DROP;

        /**
         * Checks if the change is allowed by the current change level
         * 
         * @param change
         *            requested level of change
         * @return true if allowed
         */
        public boolean allows(Change change)
        {
            return this.ordinal() >= change.ordinal();
        }
    };

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    /** Database access object */
    private ConfigurationDAO dao;

    private Map<String, EndPoint> endPoints;

    private YamlParser parser;

    private Map<String, TableConfig> tables;

    private Map<String, SchemaConfig> schemas;

    private Action action;

    private Change changeLevel;

    private Set<String> tapTables;

    private Set<String> scsTables;

    /** default configuration file name */
    public static final String DEFAULT_CONFIG = "configuration.yaml";

    /** default TAP Examples config xml file */
    public static final String DEFAULT_TAP_EXAMPLES_CONFIG = "config/tap_examples.xml";

    /** default SIA Surveys config xml file */
    public static final String DEFAULT_SIA_SURVEYS_CONFIG = "config/sia1_surveys.xml";
    
    /** default TAP configuration file name */
    public static final String DEFAULT_TAP_CONFIG = "tap_configuration.yaml";

    /** Constant for the prefix of the metadata params used to build the header */
    public static final String METADATA_PREFIX = "metadata.";

    /** Constant for the default db schema parameter */
    public static final String DEFAULT_DB_SCHEMA = "default.db.schema";

    private static ConfigurationRegistry registry;
    
    private static final int DATALINK_RESOURCE_CONFIG_LENGTH = 3;

    /**
     * Parameterless constructor
     */
    public Configuration()
    {
        super();
        endPoints = new HashMap<String, EndPoint>();
        tables = new HashMap<String, TableConfig>();
        schemas = new HashMap<String, SchemaConfig>();
        tapTables = new HashSet<String>();
        scsTables = new HashSet<String>();
        parser = new YamlBeansParser();
    }

    /**
     * A minimal constructor necessary for creating a DAO object
     * 
     * @param connectionUrl
     *            JDBC connection URL
     * @param connectionUsername
     *            JDBC connection user name
     * @param connectionPassword
     *            JDBC connection password
     */
    public Configuration(String connectionUrl, String connectionUsername, String connectionPassword)
    {
        this();
        put(ConfigValueKeys.CONNECTION_URL, connectionUrl);
        put("connection.username", connectionUsername);
        put("connection.password", connectionPassword);
    }

    /**
     * Constructor
     * 
     * @param parser
     *            YAML parser interface
     * @param configurationText
     *            configuration text
     * @throws ConfigurationException
     *             if could not parse configuration text
     */
    public Configuration(YamlParser parser, String configurationText) throws ConfigurationException
    {
        this();
        this.parser = parser;
        Configuration fromText = (Configuration) parser.parse(configurationText);
        if (fromText == null)
        {
            throw new ConfigurationException("Please supply the configuration text");
        }
        endPoints = fromText.endPoints == null ? endPoints : fromText.endPoints;
        schemas = fromText.schemas == null ? schemas : fromText.schemas;
        tables = fromText.tables == null ? tables : fromText.tables;
        action = fromText.action == null ? action : fromText.action;
        changeLevel = fromText.changeLevel == null ? changeLevel : fromText.changeLevel;
        setOptions(escapeMetadata(fromText.getOptions()));
        this.stripPlaceholders();
        wire();
    }

    /**
     * loops through the options and escapes any user entered changes to the metadata.
     * 
     * @param options
     *            the option from the user entered yaml
     * @return the map with the escaped values
     */
    private Map<String, String> escapeMetadata(Map<String, String> options)
    {
        for (Entry<String, String> entry : options.entrySet())
        {
            // only checking for metadata values at the moment.
            if (entry.getKey().startsWith(METADATA_PREFIX))
            {
                options.put(entry.getKey(), StringEscapeUtils.escapeXml10(entry.getValue()));
            }
        }
        return options;
    }

    /**
     * Configuration factory for reading from file
     * 
     * @param file
     *            configuration file
     * @param configurationRegistry
     *            configuration registry with default parameters
     * @return a Configuration object based on the file content
     * @throws ConfigurationException
     *             if could not read or parse
     */
    public static Configuration newConfiguration(File file, ConfigurationRegistry configurationRegistry)
            throws ConfigurationException
    {
        if (configurationRegistry != null)
        {
            Configuration.setRegistry(configurationRegistry);
        }
        try
        {
            if (!file.exists())
            {
                throw new ConfigurationException("Configuration file does not exist " + file.getAbsolutePath());
            }
            YamlParser parser = new YamlBeansParser();
            String content = FileUtils.readFileToString(file);
            Configuration config = new Configuration(parser, content);
            
            // validate the newly generated configuration
            validateConfig(config);
            
            // If configuration is empty add settings from properties files
            if (configurationRegistry != null
                    && (config.endPoints.size() == 0 || config.get(ConfigValueKeys.CONNECTION_URL) == null))
            {
                config.addDefaults();
            }
            return config;
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Validation method for a configuration object
     * @param config
     *          The configuration being validated
     * @throws ConfigurationException
     *          If a validation error occurs an exception is thrown
     */
    private static void validateConfig(Configuration config) throws ConfigurationException
    {
        // Check that if the datalink resources are valued, that they have 3 corresponding values
        String[] arrayConfigValues = {
                ConfigValueKeys.DATALINK_RESOURCE_IMAGE_CUBE,
                ConfigValueKeys.DATALINK_RESOURCE_CATALOGUE,
                ConfigValueKeys.DATALINK_RESOURCE_SPECTRUM,
                ConfigValueKeys.DATALINK_RESOURCE_MOMENT_MAP,
                ConfigValueKeys.DATALINK_RESOURCE_CUBELET,
                ConfigValueKeys.DATALINK_RESOURCE_EVALUATION,
                ConfigValueKeys.DATALINK_RESOURCE_VISIBILITY,
                ConfigValueKeys.DATALINK_RESOURCE_SCAN
        };
        
        for (String configArrayValue: arrayConfigValues)
        {
            String resource = config.getOptions().get(configArrayValue);
            validateArrayConfigLength(configArrayValue, resource, DATALINK_RESOURCE_CONFIG_LENGTH);
        }
    }

    /**
     * Validation method for config array values to ensure they meet the correct length
     * 
     * @param arrayConfigKey
     *          The key of the property in the config map
     * @param arrayConfigValue
     *          The value of the array config, typically csv list
     * @param expectedLength
     *          The expected length of the array config value
     * @throws ConfigurationException
     *          Thrown is validation fails
     */
    private static void validateArrayConfigLength(String arrayConfigKey, String arrayConfigValue, int expectedLength) 
            throws ConfigurationException
    {
        if (arrayConfigValue != null)
        {
            if (arrayConfigValue.split(",").length != expectedLength)
            {
                throw new ConfigurationException("Expected " + expectedLength + " values for config value: " 
                        + arrayConfigKey + " but received: " 
                        + arrayConfigValue.split(",").length);
            }
        }
    }

    /**
     * Adds missing settings using values injected by Spring from properties files
     */
    public void addDefaults()
    {
        EndPoint.addDefaults(this, registry);
        putDefault(ConfigValueKeys.APP_BASE_URL, registry.getApplicationBaseUrl());
        putDefault("application.message", registry.getApplicationMessage());
        putDefault("log.timezone", registry.getLogTimezone());
        putDefault(ConfigValueKeys.CONNECTION_URL, registry.getConnectionUrl());
        putDefault("connection.username", registry.getConnectionUsername());
        putDefault("connection.password", registry.getConnectionPassword());
        putDefault("connection.driverClassName", registry.getConnectionDriverClassName());
        putDefault("auth.trusted.ip", registry.getAuthTrustedIp());
        putDefault("auth.trusted.userId", registry.getAuthTrustedUserId());
        putDefault(ConfigValueKeys.DATALINK_BASE_URL, registry.getDatalinkBaseUrl());
        putDefault(ConfigValueKeys.DATALINK_LINKS_URL, registry.getDatalinkLinksUrl());
        putDefault(ConfigValueKeys.DATALINK_SYNC_SERVICE_NAME_WEB, registry.getDatalinkSyncServiceNameWeb());
        putDefault(ConfigValueKeys.DATALINK_SYNC_SERVICE_NAME_INTERNAL, registry.getDatalinkSyncServiceNameInternal());
        putDefault(ConfigValueKeys.DATALINK_SYNC_SERVICE_URL, registry.getDatalinkSyncServiceUrl());
        putDefault(ConfigValueKeys.DATALINK_SYNC_SERVICE_URL_INTERNAL, registry.getDatalinkSyncServiceUrlInternal());
        putDefault(ConfigValueKeys.DATALINK_ASYNC_SERVICE_NAME_WEB, registry.getDatalinkAsyncServiceName());
        putDefault(ConfigValueKeys.DATALINK_ASYNC_SERVICE_NAME_INTERNAL, registry.getDatalinkAsyncServiceNameInternal());
        putDefault(ConfigValueKeys.DATALINK_ASYNC_SERVICE_URL, registry.getDatalinkAsyncServiceUrl());
        putDefault(ConfigValueKeys.DATALINK_WEB_SERVICE_NAME, registry.getDatalinkWebServiceName());
        putDefault(ConfigValueKeys.DATALINK_WEB_SERVICE_URL, registry.getDatalinkWebUrl());
        putDefault(ConfigValueKeys.DATALINK_CUTOUT_URL, registry.getDatalinkCutoutUrl());
        putDefault(ConfigValueKeys.DATALINK_CUTOUT_SERVICE_NAME, registry.getDatalinkCutoutServiceName());
        putDefault(ConfigValueKeys.DATALINK_GENERATE_SPECTRUM_URL, registry.getDatalinkGenerateSpectrumUrl());
        putDefault(ConfigValueKeys.DATALINK_GENERATE_SPECTRUM_SERVICE_NAME, 
                registry.getDatalinkGenerateSpectrumServiceName());
        putDefault(ConfigValueKeys.DATA_LINK_ACCESS_SECRET_KEY, registry.getSiapSharedSecretKey());
        putDefault(ConfigValueKeys.DATALINK_DOWNLOAD_LIMIT_HTTP, registry.getDatalinkDownloadLimitHttp());
        putDefault(ConfigValueKeys.DATALINK_LARGE_WEB_DOWNLOAD_LIMIT_HTTP,
                registry.getDatalinkLargeWebDownloadLimitHttp());
        
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_IMAGE_CUBE, StringUtils.join(registry.getImageCubeResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_CATALOGUE, StringUtils.join(registry.getCatalogueResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_SPECTRUM, StringUtils.join(registry.getSpectrumResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_MOMENT_MAP, StringUtils.join(registry.getMomentMapResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_CUBELET, StringUtils.join(registry.getCubeletResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_EVALUATION, StringUtils.join(registry.getEvaluationResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_VISIBILITY, StringUtils.join(registry.getVisibilityResource(), ","));
        putDefault(ConfigValueKeys.DATALINK_RESOURCE_SCAN, StringUtils.join(registry.getScanResource(), ","));
        
        putDefault(ConfigValueKeys.ENVIRONMENT, registry.getEnvironment());
        putDefault(ConfigValueKeys.CSS, registry.getCss());
        putDefault(ConfigValueKeys.LOGO_URL, registry.getLogoUrl());
        putDefault(DEFAULT_DB_SCHEMA, registry.getDefaultDbSchema());

        // TAP metadata
        putDefault("metadata.instrument", registry.getMetadataInstrument());
        putDefault("metadata.server", registry.getMetadataServer());
        putDefault("metadata.serviceShortName", registry.getMetadataServiceShortName());
        putDefault("metadata.serviceTitle", registry.getMetadataServiceTitle());
        putDefault("metadata.identifier", registry.getMetadataIdentifier());
        putDefault("metadata.servicePublisher", registry.getMetadataServicePublisher());
        putDefault("metadata.furtherInformation", registry.getMetadataFurtherInformation());
        putDefault("metadata.contactPerson", registry.getMetadataContactPerson());
        putDefault("metadata.copyright", registry.getMetadataCopyright());
        
        // Add all ConfigKeys other than those in endpoints
        for (ConfigKeys configKey : ConfigKeys.values())
        {
            String key = configKey.getKey();
            if (!key.startsWith("tap") && !key.startsWith("scs"))
            {
                putDefault(key, registry.getConfigValue(key));
            }
        }

        // Try to create a DAO if it is not created yet
        if (dao == null)
        {
            try
            {
                dao = new ConfigurationDAOImpl(this);
            }
            catch (Exception e) // not ready to access DB yet
            {
                dao = null;
            }
        }
        // If there are no tables defined for TAP and/or SCS, use configured SCS test table
        EndPoint scs = endPoints.get("SCS");
        EndPoint tap = endPoints.get("TAP");
        String tableSchema = scs == null ? null : scs.get("scs.test.schema");
        String table = scs == null ? null : scs.get("scs.test.catalog");
        Set<String> tableNames = new HashSet<String>();
        if (table != null)
        {
            if (table.indexOf(".") < 0)
            {
                table = tableSchema + "." + table;
                endPoints.get("SCS").put("scs.test.catalog", table);
            }
            if (scs.getTables().size() == 0)
            {
                scs.getTables().add(table);
            }
            tableNames.addAll(scs.getTables());
        }
        if (tap != null)
        {
            tableNames.addAll(tap.getTables());
            tap.getTables().addAll(scs.getTables()); // TAP access to SCS tables is ALWAYS enabled
        }

      String schemaName = get(DEFAULT_DB_SCHEMA);
      if (schemaName != null)
      {
          SchemaConfig schema = new SchemaConfig(schemaName);
          schemas.put(schemaName, schema);
      }
    }

    /**
     * Saves this Configuration object to the configured or default yaml file location
     * 
     * @throws ConfigurationException
     *             if could not save due to IO problem
     */
    public void saveConfigurationYaml() throws ConfigurationException
    {
        File configurationYaml = Configuration.registry.getConfigurationYamlFile();

        try
        {
            String text = this.toString(false);
            FileUtils.writeStringToFile(configurationYaml, text, "UTF-8");
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#stripPlaceholders()
     */
    @Override
    public void stripPlaceholders()
    {
        for (EndPoint point : endPoints.values())
        {
            if (point != null)
            {
                point.stripPlaceholders();
            }
        }
        for (SchemaConfig schema : schemas.values())
        {
            if (schema != null)
            {
                schema.stripPlaceholders();
            }
        }
        for (TableConfig table : tables.values())
        {
            if (table != null)
            {
                table.stripPlaceholders();
            }
        }
        super.stripPlaceholders();
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    /**
     * Converts this Configuration to String, adding helping comments if required
     * 
     * @param comment
     *            add comments if true
     * @return textual representation of this Configuration object
     */
    public String toString(boolean comment)
    {
        try
        {
            if (endPoints.isEmpty() && comment)
            {
                endPoints.put("TAP and/or SCS", EndPoint.helpEndPoint());
            }
            addPlaceholder(ConfigValueKeys.CONNECTION_URL);
            addPlaceholder("connection.username");
            addPlaceholder("connection.password");
            addPlaceholder(DEFAULT_DB_SCHEMA);
            if (!comment)
            {
                stripPlaceholders();
            }
            return parser.serialise(this);
        }
        catch (ConfigurationException e)
        {
            // TODO add logging here?
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Perform configured action
     * 
     * @throws ConfigurationException
     *             if failed, most likely, because could not implement requested database changes
     */
    public void act() throws ConfigurationException
    {
        dao = initDao();
        if (dao == null && action != Action.CURRENT)
        {
            throw new ConfigurationException("Can't obtain a valid database connection.");
        }
        switch (action)
        {
        case CURRENT:
            break;
        case EXPLORE:
            dao.checkTapDbVersion();
            export(true);
            break;
        case APPLY:
            dao.checkTapDbVersion();
            apply();
            break;
        default:
            break;
        }
    }

    /**
     * Perform an action
     * 
     * @param action
     *            action to perform
     * @param level
     *            authorised level of changes
     * @return if making changes - text of changed current configuration, if exploring - configuration of listed objects
     * @throws ConfigurationException
     *             if failed, most likely, because could not implement requested database changes
     */
    public String act(Action action, Change level) throws ConfigurationException
    {
        try
        {
            this.action = action;
            registry.getCurrent().changeLevel = level;
            this.changeLevel = level;
            act();
            registry.getCurrent().changeLevel = null;
            this.changeLevel = null;
            this.action = null;
            Configuration result = action == Action.EXPLORE ? export(true) : registry.getCurrent();
            // If the action did not change configuration, return textual representation of exported objects
            if (action == Action.EXPLORE || action == Action.NONE || action == Action.CURRENT)
            {
                return result.toString(true);
            }

            // else save this configuration and switch to it
            stripPlaceholders();
            saveConfigurationYaml();
            registry.switchConfiguration(this, true);
            return this.toString(true);
        }
        finally
        {
            registry.getCurrent().changeLevel = null;
            this.changeLevel = null;
            this.action = null;
        }
    }

    /**
     * Applies given configuration object: missing elements are deleted, different elements are updated, new elements
     * are created.
     * 
     * @throws ConfigurationException
     *             if configured change level does not allow requested changes
     */
    public void apply() throws ConfigurationException
    {
        // Current will contain what was specified last time. This will not include any newly added tables
        Configuration current = registry.getCurrent().export(false);
        try
        {
            initDao().begin();
            // for each table mentioned in this (updated) configuration
            for (TableConfig table : tables.values())
            {
                if (current.tables.containsKey(table.gtFullDbTableName())) // table exists - update it
                {
                    current.tables.get(table.gtFullDbTableName()).update(table);
                }
                else
                {
                    // Table was not in the old config - need to check if it exists in the database
                    TableConfig dbTableConfig = getExistingTableDef(table.gtFullDbTableName());
                    if (!dbTableConfig.isEmpty())
                    {
                        dbTableConfig.update(table);
                    }
                    else
                    {
                        // table does not exist - create it
                        table.create();
                    }
                }
            }
            // after updates the DB state has changed, need to update TAP metadata tables
            updateTap(current); // pass what used to be current
            initDao().commit();
        }
        catch (Exception e)
        {
            initDao().rollback();
            e.printStackTrace();
            throw new ConfigurationException(e);
        }
    }

    /**
     * Retrieve a TableConfig object representing the state of the table in the database. If the table is not present in
     * the database then the returned object will be empty.
     * 
     * @param fullDbTableName
     *            The schema qualified name of the table in the database.
     * @return The table config object
     * @throws ConfigurationException
     *             if there were connection problems
     */
    private TableConfig getExistingTableDef(String fullDbTableName) throws ConfigurationException
    {
        TableConfig tableConfig = new TableConfig(fullDbTableName);
        try
        {
            tableConfig.init();
            tableConfig.export(this, fullDbTableName);
            if (tableConfig.isEmpty())
            {
                // Table does not exist.
                logger.info("Table " + fullDbTableName + " does not exist.");
            }
            else
            {
                wireTable(tableConfig);
            }
        }
        catch (BadSqlGrammarException e)
        {
            // Table does not exist.
            logger.info("Table " + fullDbTableName + " does not exist.", e);
        }
        return tableConfig;
    }

    /**
     * Create TAP metadata where does not exist. NOTE: it creates TAP metadata skeleton only for tables listed in the
     * configuration. If these tables contain constraints referring to other tables, an error will result.
     * 
     * @throws ConfigurationException
     *             if the required update is not allowed or a database problem occurred
     */
    public void createTap() throws ConfigurationException
    {
        Configuration newState = this.export(false);
        newState.initDao();
        newState.setChangeLevel(Change.UPDATE);

        // Any new schemas created?
        for (String schemaName : newState.schemas.keySet())
        {
            newState.gtDao().createTapSchema(newState.schemas.get(schemaName));
        }

        Set<TableConfig> toUpdateReferences = new HashSet<TableConfig>();

        // Any new tables created?
        for (String tableName : newState.tables.keySet())
        {
            TableConfig newTable = newState.tables.get(tableName);
            toUpdateReferences.add(newTable);
            newState.gtDao().createTapTable(newState.tables.get(tableName));
        }

        for (TableConfig table : toUpdateReferences)
        {
            newState.gtDao().updateTapTableReferences(table, true);
        }

    }

    /**
     * Compare schemas and tables and update relevant information in TAP metadata tables if needed
     * 
     * @param pastState
     *            configuration state before changes
     * 
     * @throws ConfigurationException
     *             if the required update is not allowed or a database problem occurred
     */
    void updateTap(Configuration pastState) throws ConfigurationException
    {
        // Any schemas deleted?
        for (String schemaName : pastState.schemas.keySet())
        {
            if (!schemas.containsKey(schemaName))
            {
                dao.deleteTapSchema(pastState.schemas.get(schemaName));
            }
        }

        // Any new schemas created?
        for (String schemaName : schemas.keySet())
        {
            if (!pastState.schemas.containsKey(schemaName))
            {
                dao.createTapSchema(schemas.get(schemaName));
            }
        }

        // Any schemas changed?
        for (String schemaName : schemas.keySet())
        {
            SchemaConfig oldSchema = pastState.schemas.get(schemaName);
            SchemaConfig newSchema = schemas.get(schemaName);
            if (oldSchema != null && !oldSchema.equals(newSchema))
            {
                dao.updateTapSchema(newSchema, false);
            }
        }

        // Any tables deleted?
        for (String tableName : pastState.tables.keySet())
        {
            if (!tables.containsKey(tableName))
            {
                initDao().deleteTapTable(pastState.tables.get(tableName));
            }
        }
        Set<TableConfig> toUpdateReferences = new HashSet<TableConfig>();
        // Any new tables created?
        for (String tableName : tables.keySet())
        {
            if (!pastState.tables.containsKey(tableName))
            {
                dao.createTapTable(tables.get(tableName));
                toUpdateReferences.add(tables.get(tableName));
            }
        }

        // Any tables changed?
        for (String tableName : tables.keySet())
        {
            TableConfig oldTable = pastState.tables.get(tableName);
            TableConfig newTable = tables.get(tableName);
            if (oldTable != null && !oldTable.equals(newTable))
            {
                dao.updateTapTable(newTable, false);
                toUpdateReferences.add(newTable);
            }
        }

        for (TableConfig table : toUpdateReferences)
        {
            dao.updateTapTableReferences(table, false);
        }

    }

    /**
     * Learns available information about entries (tables) listed in input configuration
     * 
     * @param withPlaceholders
     *            if true replace missing values with placeholders
     * @return updated configuration
     * @throws ConfigurationException
     *             if finds table names that do not include schema
     */
    public Configuration export(boolean withPlaceholders) throws ConfigurationException
    {
        dao = initDao();
        // clone this object
        String text = parser.serialise(this);
        Configuration exported = new Configuration(parser, text);
        if (dao == null)
        {
            return exported; // can't read anything from the DB without a valid DAO
        }
        Set<String> union = new HashSet<String>();
        for (EndPoint point : exported.endPoints.values())
        {
            if (point.getTables() != null)
            {
                union.addAll(point.getTables());
                if (point.gtType() == EndPoint.Type.TAP)
                {
                    tapTables.addAll(point.getTables());
                }
                if (point.gtType() == EndPoint.Type.SCS)
                {
                    scsTables.addAll(point.getTables());
                }
            }
        }

        // check that all used in endpoints tables are represented
        for (String tableName : union)
        {
            if (!exported.tables.containsKey(tableName) && tableName.indexOf('.') < 0)
            {
                throw new ConfigurationException("Table name must include schema name: " + tableName);
            }
            exported.tables.put(tableName, new TableConfig(tableName));
        }

        // export endpoints configuration
        if (!exported.endPoints.isEmpty())
        {
            for (EndPoint point : exported.endPoints.values())
            {
                point.export();
            }
        }
        else
        {
            exported.endPoints.put("TAP and/or SCS", EndPoint.helpEndPoint());
        }

        // learn database structure
        Set<String> emptyTables = new HashSet<String>();
        for (String fullDbTableName : exported.tables.keySet())
        {
            TableConfig tableConfig = exported.tables.get(fullDbTableName);
            tableConfig.init();
            tableConfig.export(this, fullDbTableName);
            if (tableConfig.isEmpty())
            {
                emptyTables.add(fullDbTableName);
            }
        }
        
        // export schemas
        for (SchemaConfig schema : exported.schemas.values())
        {
            schema.export();
        }
                
        for (String fullDbTableName : emptyTables)
        {
            exported.tables.remove(fullDbTableName);
        }
        exported.wire();
        if (!withPlaceholders)
        {
            exported.stripPlaceholders();
        }
        exported.addOptions(registry.getCurrent());
        return exported;
    }

    /*
     * Add to exported object options that are available in the current configuration.
     */
    private void addOptions(Configuration other)
    {
        if (other == null || !StringUtils.equals(this.get(ConfigValueKeys.CONNECTION_URL),
                other.get(ConfigValueKeys.CONNECTION_URL)))
        { // Different databases, different configurations
            return;
        }
        // add options of the endpoints
        for (String key : endPoints.keySet())
        {
            EndPoint thisEp = endPoints.get(key);
            EndPoint otherEp = other.endPoints.get(key);
            thisEp.addOptions(otherEp);
        }

        // add options of the tables
        for (String key : tables.keySet())
        {
            TableConfig thisTc = tables.get(key);
            TableConfig otherTc = other.tables.get(key);
            thisTc.addOptions(otherTc);
        }
        super.addOptions(other);
    }

    /**
     * Puts in place references from constraints and indices to fields that they affect
     * 
     * @throws ConfigurationException
     *             if detects use of reserved words as column names
     */
    public void wire() throws ConfigurationException
    {
        for (String schemaName : schemas.keySet()) // for all schemas in this configuration
        {
            SchemaConfig schema = schemas.get(schemaName);
            if (schema == null) // may happen when parsing from text
            {
                schema = new SchemaConfig();
                schemas.put(schemaName, schema);
            }
            schema.setName(schemaName);
        }
        for (EndPoint point : endPoints.values())
        {
            if (point != null)
            {
                point.setConfig(this);
            }
        }
        for (SchemaConfig schema : schemas.values())
        {
            if (schema != null)
            {
                schema.setConfig(this);
            }
        }
        wireTables(tables);
    }

    /**
     * Parsing puts in place table references from constraints and indices to fields that they affect
     * 
     * @param tables
     *            tables to wire
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public void wireTables(Map<String, TableConfig> tables) throws ConfigurationException
    {
        for (String fullDbTableName : tables.keySet()) // for all tables in this configuration
        {
            TableConfig table = tables.get(fullDbTableName);
            if (table == null) // may happen when parsing from text
            {
                table = new TableConfig();
                tables.put(fullDbTableName, table);
            }
            table.setFullDbTableName(fullDbTableName);
            wireTable(table);
        }
    }

    private void wireTable(TableConfig table) throws ConfigurationException
    {
        table.setConfig(this);
        for (String columnName : table.getColumns().keySet())
        {
            ColumnConfig columnConfig = table.getColumns().get(columnName);
            columnConfig.setName(columnName);
            columnConfig.setTable(table);
        }
        table.gtIndexConfigs().clear();
        for (String indexDef : table.getIndexDefs().values()) // wire index <-> column references
        {
            IndexConfig indexConfig = new IndexConfig(table, indexDef);
            table.gtIndexConfigs().put(indexConfig.getName(), indexConfig);
            indexConfig.setTable(table);
            for (String column : indexConfig.getColumns())
            {
                ColumnConfig columnConfig = table.getColumns().get(column);
                if (columnConfig != null) // can be null when processing deletion of index
                {
                    columnConfig.gtIndices().add(indexConfig);
                }
            }
        }
        table.gtKeyConfigs().clear();
        for (String name : table.getKeys().keySet())
        {
            String keyDef = table.getKeys().get(name);
            KeyConfig keyConfig = new KeyConfig(table, name, keyDef);
            table.gtKeyConfigs().put(keyConfig.getName(), keyConfig);
        }
        table.gtConstraintConfigs().clear();
        for (String name : table.getConstraints().keySet()) // wire constraints <-> column references
        {
            String conDef = table.getConstraints().get(name);
            ConstraintConfig conConfig = new ConstraintConfig(table, name, conDef);
            table.gtConstraintConfigs().put(conConfig.getName(), conConfig);
            TableConfig srcTable = tables.get(conConfig.getSrcTable());
            TableConfig dstTable = tables.get(conConfig.getDstTable());
            if (srcTable != null) // null may happen if we work with a subset of tables in DB
            {
                for (String columnName : conConfig.getSrcColumns())
                {
                    ColumnConfig srcColumn = srcTable.getColumns().get(columnName);
                    if (srcColumn != null)
                    {
                        srcColumn.gtConstraintsOut().add(conConfig);
                    }
                }
            }
            if (dstTable != null)
            {
                for (String columnName : conConfig.getDstColumns())
                {
                    ColumnConfig dstColumn = dstTable.getColumns().get(columnName);
                    if (dstColumn != null)
                    {
                        dstColumn.gtConstraintsIn().add(conConfig);
                    }
                }
            }
        }
    }

    /**
     * Add end point to the list of end points
     * 
     * @param protocol
     *            end point protocol
     * @param endPoint
     *            end point to add
     */
    public void addEndPoint(String protocol, EndPoint endPoint)
    {
        this.endPoints.put(protocol, endPoint);
    }

    /**
     * Get end point by protocol name
     * 
     * @param protocol
     *            end point protocol
     * @return end point for the given protocol or null
     */
    public EndPoint getEndPoint(String protocol)
    {
        return endPoints == null ? null : endPoints.get(protocol);
    }

    public Map<String, EndPoint> getEndPoints()
    {
        return endPoints;
    }

    public void setEndPoints(Map<String, EndPoint> endPoints)
    {
        this.endPoints = endPoints;
    }

    public Map<String, TableConfig> getTables()
    {
        return tables;
    }

    public void setTables(Map<String, TableConfig> tables)
    {
        this.tables = tables;
    }

    /**
     * Get table config
     * 
     * @param schemaTableName
     *            schema and table names of the table in form schema.table
     * @return table configuration object
     */
    public TableConfig getTableConfig(String schemaTableName)
    {
        return tables.get(schemaTableName);
    }

    /**
     * Add table config
     * 
     * @param schemaName
     *            schema name of the table
     * @param tableName
     *            table name of the table
     * @param config
     *            table configuration object to add
     */
    public void addTableConfig(String schemaName, String tableName, TableConfig config)
    {
        tables.put(schemaName + "." + tableName, config);
    }

    /**
     * @param schemaTableName
     *            schema and table names of the table in form schema.table
     * @param config
     *            table configuration object to add
     */
    public void addTableConfig(String schemaTableName, TableConfig config)
    {
        tables.put(schemaTableName, config);
    }

    /**
     * Get DAO object. Used instead of a standard getter, to prevent DAO serialised in YAML.
     * 
     * @return DAO object
     */
    public ConfigurationDAO gtDao()
    {
        return dao;
    }

    /**
     * Create DAO if it does not exist.
     * 
     * @return DAO object
     * @throws ConfigurationException
     *             if there were JDBC Connection problems
     */
    public synchronized ConfigurationDAO initDao() throws ConfigurationException
    {
        if (dao == null)
        {
            try
            {
                dao = new ConfigurationDAOImpl(this);
            }
            catch (Exception e)
            {
                dao = null;
                String connectionUrl = get(ConfigValueKeys.CONNECTION_URL);
                // if looks like a configuration mistake, not an incomplete configuration
                if (connectionUrl != null && connectionUrl.contains("jdbc:"))
                {
                    throw new ConfigurationException(e);
                }
                return null;
            }
        }
        return dao;
    }

    public void setDao(ConfigurationDAO dao)
    {
        this.dao = dao;
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Change getChangeLevel()
    {
        return changeLevel;
    }

    public void setChangeLevel(Change changeLevel)
    {
        this.changeLevel = changeLevel;
    }

    public void setParser(YamlParser parser)
    {
        this.parser = parser;
    }

    /**
     * A replacement for getParser() to keep it hidden from the YAML parser
     * 
     * @return parser
     */
    public YamlParser gtParser()
    {
        return parser;
    }

    /**
     * Returns true if the table is used by a TAP service
     * 
     * @param tableName
     *            table name to check
     * @return true if the table with this name is available via TAP protocol
     */
    public boolean isUsedByTap(String tableName)
    {
        return tapTables.contains(tableName);
    }

    /**
     * Returns true if the table is used by an SCS service
     * 
     * @param tableName
     *            table name to check
     * @return true if the table with this name is available via SCS protocol
     */
    public boolean isUsedByScs(String tableName)
    {
        return scsTables.contains(tableName);
    }

    public Map<String, SchemaConfig> getSchemas()
    {
        return schemas;
    }

    public void setSchemas(Map<String, SchemaConfig> schemas)
    {
        this.schemas = schemas;
    }

    public static ConfigurationRegistry getRegistry()
    {
        return registry;
    }

    public static void setRegistry(ConfigurationRegistry registry)
    {
        Configuration.registry = registry;
    }
}
