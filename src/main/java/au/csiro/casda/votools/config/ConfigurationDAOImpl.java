package au.csiro.casda.votools.config;

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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.utils.Utils;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * Database interface for Configuration
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class ConfigurationDAOImpl implements ConfigurationDAO
{
    private static Logger logger = LoggerFactory.getLogger(ConfigurationDAOImpl.class);

    private JdbcTemplate template;
    
    private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

    /** TAP metadata schemas table */
    public static final String SCHEMAS_TABLE_NAME = "tap_schemas";

    /** TAP metadata tables table */
    public static final String TABLES_TABLE_NAME = "tap_tables";

    /** TAP metadata columns table */
    public static final String COLUMNS_TABLE_NAME = "tap_columns";

    /** TAP metadata key columns table */
    public static final String KEY_COLUMN_TABLE_NAME = "tap_key_columns";

    /** TAP metadata foreign keys table */
    public static final String KEYS_TABLE_NAME = "tap_keys";

    /** Default schema name - this might need to be public for other installs? */
    public static final String DEFAULT_SCHEMA_NAME = "casda";

    /** TAP protocol schema name for metadata tables */
    public static final String TAP_SCHEMA_NAME = "TAP_SCHEMA";

    /** Schema name for obscore table */
    public static final String IVOA_SCHEMA_NAME = "ivoa";

    /** Code for primary keys */
    public static final String PRIMARY_KEYS = "p";

    /** Code for foreign keys */
    public static final String FOREIGN_KEYS = "f";

    /** Table does not exist response */
    public static final String TABLE_DOES_NOT_EXIST = "Table does not exist";

    private TapObjectCache tapCache;

    private Configuration config;

    private static String schema;

    /**
     * Parameterless constructor
     */
    public ConfigurationDAOImpl()
    {
    }

    /**
     * Just a DAO with default configuration
     * 
     * @param template
     *            JdbcTemplate object
     */
    public ConfigurationDAOImpl(JdbcTemplate template)
    {
        this.template = template;
        config = new Configuration();
        config.setDao(this);
        tapCache = new TapObjectCache(template);
    }

    /**
     * A configuration based constructor
     * 
     * @param configuration
     *            configuration object
     * @throws ConfigurationException
     *             if there were connection problems
     */
    public ConfigurationDAOImpl(Configuration configuration) throws ConfigurationException
    {
        config = configuration;
        config.setDao(this);
        tapCache = new TapObjectCache();
        init();
    }

    /**
     * Initialisation
     * 
     * @throws ConfigurationException
     *             if there were connection problems
     */
    private void init() throws ConfigurationException
    {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(config.get(ConfigValueKeys.CONNECTION_URL));
        dataSource.setUsername(config.get("connection.username"));
        dataSource.setPassword(config.get("connection.password"));
        dataSource.setDriverClassName(config.get("connection.driverClassName", "org.postgresql.Driver"));
        template = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        tapCache.setConfiguration(config);
    }

    @Override
    public void refreshObjectCache()
    {
        tapCache.refresh();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#checkTapDbVersion()
     */
    @Override
    public void checkTapDbVersion() throws ConfigurationException
    {
        ConfigurationRegistry registry = Configuration.getRegistry();
        boolean isVerifiedTap = registry.isVerifiedTap(config);
        if (!isVerifiedTap)
        {
            String tapConfigText = readTapConfiguration();
            enforceTapVersion(tapConfigText);
            registry.setVerifiedTap(config);
        }
    }

    /**
     * Reads TAP configuration text
     * 
     * @return text of TAP configuration
     * 
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public String readTapConfiguration() throws ConfigurationException
    {
        try
        {
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(Configuration.DEFAULT_TAP_CONFIG);
            if (in == null)
            {
                throw new IOException("Could not open file " + Configuration.DEFAULT_TAP_CONFIG);
            }
            String content = IOUtils.toString(in).replaceAll("'schema'", getSchema());
            return content;
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Error while reading TAP configuration file: " + e.getMessage(), e);
        }
    }

    /**
     * Makes sure that TAP tables have structure that matches the tap_configuration.yaml file shipped with the package
     * 
     * @param tapConfigText
     *            text of TAP configuration to enforce
     * 
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    public void enforceTapVersion(String tapConfigText) throws ConfigurationException
    {
        try
        {
            begin();
            YamlParser parser = new YamlBeansParser();
            Configuration tapConfig = new Configuration(parser, tapConfigText);
            tapConfig.put(ConfigValueKeys.CONNECTION_URL, config.get(ConfigValueKeys.CONNECTION_URL));
            tapConfig.put("connection.username", config.get("connection.username"));
            tapConfig.put("connection.password", config.get("connection.password"));
            tapConfig.put("connection.driverClassName", config.get("connection.driverClassName"));
            tapConfig.setChangeLevel(config.getChangeLevel());
            tapConfig.initDao();
            String text = parser.serialise(tapConfig);
            Configuration tapCurrent = new Configuration(parser, text);
            tapCurrent.setChangeLevel(config.getChangeLevel());
            try
            // TAP tables may not exist yet
            {
                String listSql = String.format(
                        "SELECT db_schema_name||'.'||db_table_name AS name FROM %s.%s where schema_name='%s'",
                        tapConfig.get(Configuration.DEFAULT_DB_SCHEMA), TABLES_TABLE_NAME, TAP_SCHEMA_NAME);
                List<String> tapTables = template.queryForList(listSql, String.class);
                for (String dbTableName : tapTables)
                {
                    tapCurrent.getEndPoint("TAP").addTable(dbTableName);
                }
            }
            catch (BadSqlGrammarException e)
            {
                String msg = e.getMessage();
                if (!msg.contains("relation") || !msg.contains("not exist"))
                {
                    throw e;
                }
            }
            // Read information relevant to the configuration
            tapCurrent = tapCurrent.export(false);
            // Update tables structure
            // Arrange tables in the order of creation
            TableConfig[] tables = new TableConfig[tapConfig.getTables().size()];
            try
            {
                for (TableConfig table : tapConfig.getTables().values())
                {
                    if (tables[table.getInt("creation.order")] != null)
                    {
                        throw new ConfigurationException("Creation.order values must be unique:"
                                + table.get("creation.order"));
                    }
                    tables[table.getInt("creation.order")] = table;
                    TableConfig currentTable = tapCurrent.getTables().get(table.gtFullDbTableName());
                    if (currentTable != null) // to make equivalence test possible
                    {
                        currentTable.put("creation.order", table.get("creation.order"));
                    }
                }
            }
            catch (Exception e)
            {
                throw new ConfigurationException(
                        "All TAP tables must have unique creation.order parameters in the range from 0 to N-1");
            }
            // Need to use the array for this because need to create the tables in particular order
            for (TableConfig table : tables)
            {
                if (tapCurrent.getTables().containsKey(table.gtFullDbTableName())) // table exists - update it
                {
                    tapCurrent.getTables().get(table.gtFullDbTableName()).update(table);
                }
                else
                // table does not exist - create it
                {
                    table.create();
                }
            }
            // after updates the DB state has changed, need to update TAP metadata tables
            tapConfig.updateTap(tapCurrent);
            commit();
        }
        catch (Exception e)
        {
            rollback();
            throw new ConfigurationException("Can't verify or update TAP tables: " + e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#exportColumns(java.lang.String)
     */
    @Override
    public Map<String, ColumnConfig> exportColumns(String fullTableName)
    {
        int colCounter = 1;
        String[] params = fullTableName.split("\\.");
        List<ColumnConfig> columns = template.query(GET_COLUMNS_SQL, params, new ColumnConfigMapper(null));
        Map<String, ColumnConfig> map = new LinkedHashMap<String, ColumnConfig>();

        for (ColumnConfig columnConfig : columns)
        {
            map.put(columnConfig.gtName(), columnConfig);
            columnConfig.setName(null);
            columnConfig.put(ColumnConfig.ORDER, String.valueOf(colCounter++)); 
            columnConfig.put(ColumnConfig.SIZE, sizeByType(columnConfig.getType()));
        }
        addColumnComments(fullTableName, map);
        return map;
    }

    /**
     * @param dataType
     *            the data type of a column
     * @return the size of this data type
     */
    private String sizeByType(String dataType)
    {
        if (dataType.startsWith(FieldTypes.TYPE_VARCHAR.keyword))
        {
            return dataType.replace(FieldTypes.TYPE_VARCHAR.keyword, "").replace(")", "");
        }
        for (FieldTypes ft : FieldTypes.values())
        {
            if (dataType.equals(ft.keyword))
            {
                return ft.size;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#addColumnComments(java.lang.String, java.util.Map)
     */
    @Override
    public void addColumnComments(String fullTableName, Map<String, ColumnConfig> configs)
    {
        String[] params = fullTableName.split("\\.");
        template.query(GET_COLUMN_COMMENTS_SQL, params, new ColumnConfigMapper(configs));
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#updateColumnsFromTap(java.lang.String, java.util.Map)
     */
    @Override
    public void updateColumnsFromTap(String fullDbTableName, Map<String, ColumnConfig> configs)
    {
        String dbSchemaName = fullDbTableName.substring(0, fullDbTableName.indexOf('.'));
        String dbTableName = fullDbTableName.substring(fullDbTableName.indexOf('.') + 1);
        String sql = String.format("SELECT table_name FROM %s.%s WHERE db_schema_name='%s' AND db_table_name='%s'",
                getSchema(), TABLES_TABLE_NAME, dbSchemaName, dbTableName);
        try
        {
            String fullTapTableName = template.queryForObject(sql, String.class);
            String getColumnsFromTapSql = "SELECT column_name as name, db_column_name, "
                    + "description, unit, ucd, utype, principal, indexed, "
                    + "std, scs_verbosity FROM " + getSchema() + "." + COLUMNS_TABLE_NAME + " WHERE table_name = ? ";

            template.query(getColumnsFromTapSql, new Object[] { fullTapTableName }, new ColumnConfigMapper(configs));
        }
        catch (BadSqlGrammarException | EmptyResultDataAccessException e) // TAP does not have this information yet
        {
            return;
        }
    }

    /** indices of index name in case if it is unique and not unique */
    static final int IX_3 = 3, IX_2 = 2;

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#exportIndexDefs(java.lang.String)
     */
    @Override
    public Map<String, String> exportIndexDefs(String fullTableName)
    {
        String shortTableName = fullTableName.substring(fullTableName.indexOf('.') + 1);
        List<String> defs = queryForStrings(GET_INDEX_DEFINITION_SQL, new Object[] { fullTableName });
        Map<String, String> map = new HashMap<String, String>();
        for (String def : defs)
        {
            String[] arr = def.split(" ");
            String name = arr[1].equalsIgnoreCase("UNIQUE") ? arr[IX_3] : arr[IX_2];
            if (!(name.contains(shortTableName) && name.endsWith("_pkey"))) // ignore default primary keys
            {
                map.put(name, def);
            }
        }
        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#exportConstraints(java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, String> exportConstraints(String fullDbTableName, String constrType)
    {
        String[] params = new String[] { constrType, fullDbTableName };
        List<String> constraints = template.query(GET_CONSTRAINTS_SQL, params, new ConstraintConfigMapper());
        Map<String, String> map = new HashMap<String, String>();
        for (String constraintConfig : constraints)
        {
            String[] arr = constraintConfig.split(" ", 2);
            map.put(arr[0], arr[1]);
        }
        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#queryForStrings(java.lang.String, java.lang.Object[])
     */
    @Override
    public List<String> queryForStrings(String sql, Object[] params)
    {
        return template.query(sql, params, new StringMapper());
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#exportTableDescription(java.lang.String)
     */
    @Override
    public String exportTableDescription(String fullDbTableName)
    {
        try
        {
            return template.queryForObject(GET_TABLE_DESCRIPTION_SQL, String.class, new Object[] { fullDbTableName });
        }
        catch (Exception e)
        {
            String msg = e.getMessage();
            if (msg.contains("ERROR") && msg.contains("does not exist"))
            {
                return TABLE_DOES_NOT_EXIST;
            }
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#updateTableFromTap(java.lang.String,
     * au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public boolean updateTableFromTap(String fullDbTableName, TableConfig config)
    {
        // Check if the table exists
        String desc = exportTableDescription(fullDbTableName);
        if (TABLE_DOES_NOT_EXIST.equals(desc))
        {
            return false;
        }
        String dbSchemaName = fullDbTableName.substring(0, fullDbTableName.indexOf('.'));
        String dbTableName = fullDbTableName.substring(fullDbTableName.indexOf('.') + 1);
        String getTableFromTapSql = "SELECT description, description_long, utype, scs_enabled, release_required, "
                + "params, schema_name, table_name FROM " + getSchema() + "." + TABLES_TABLE_NAME
                + " WHERE db_table_name = ? AND db_schema_name = ?";

        template.query(getTableFromTapSql, new Object[] { dbTableName, dbSchemaName }, new TapTableMapper(config));
        return true;
    }

    /**
     * A row mapper for reading TAP table information
     */
    public class TapTableMapper implements RowMapper<TableConfig>
    {
        private TableConfig table;

        /**
         * A constructor
         * 
         * @param config
         *            table configuration object to save information to
         */
        public TapTableMapper(TableConfig config)
        {
            table = config;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TableConfig mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            table.put(TableConfig.DESCRIPTION, rs.getString(TableConfig.DESCRIPTION));
            table.put(TableConfig.DESCRIPTION_LONG, rs.getString(TableConfig.DESCRIPTION_LONG));
            table.put(TableConfig.UTYPE, rs.getString(TableConfig.UTYPE));
            table.put(TableConfig.PARAMS, rs.getString(TableConfig.PARAMS));
            String schema = rs.getString(TableConfig.SCHEMA_NAME);
            table.put(TableConfig.TAP_SCHEMA_NAME, schema);
            boolean value = rs.getBoolean(TableConfig.SCS_ENABLED);
            table.put(TableConfig.SCS_ENABLED, value ? "1" : "0");
            value = rs.getBoolean(TableConfig.RELEASE_REQUIRED);
            table.put(TableConfig.RELEASE_REQUIRED, value ? "1" : "0");
            String name = rs.getString(TableConfig.TABLE_NAME);
            table.put(TableConfig.TAP_TABLE_NAME, name.substring(name.indexOf('.') + 1));
            return table;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#execute(java.lang.String)
     */
    @Override
    public void execute(String statement)
    {
        template.execute(statement);
        logger.info("Executed SQL: " + statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.csiro.casda.votools.config.ConfigurationDAO#dropConstraint(au.csiro.casda.votools.config.ConstraintConfig)
     */
    @Override
    public void dropConstraint(ConstraintConfig constraint) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.DROP);
        String statement = String.format("ALTER TABLE %s DROP CONSTRAINT IF EXISTS %s", constraint.getSrcTable(),
                constraint.getName());
        execute(statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#dropIndex(au.csiro.casda.votools.config.IndexConfig)
     */
    @Override
    public void dropIndex(IndexConfig index) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.DROP);
        String statement = String.format("DROP INDEX IF EXISTS %s.%s", index.gtTable().gtSchema(), index.getName());
        execute(statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#dropColumn(au.csiro.casda.votools.config.ColumnConfig,
     * au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void dropColumn(ColumnConfig columnConfig) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.DROP, "drop column " + columnConfig.gtTable().gtFullDbTableName() + "."
                + columnConfig.gtName());
        execute("ALTER TABLE " + columnConfig.gtTable().gtFullDbTableName() + " DROP IF EXISTS "
                + columnConfig.gtName() + " CASCADE");

    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#addIndex(au.csiro.casda.votools.config.IndexConfig)
     */
    @Override
    public void addIndex(IndexConfig index) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.REINDEX);
        String columns = "(";
        for (String column : index.getColumns())
        {
            if (columns.length() > 1)
            {
                columns += ", ";
            }
            columns += column;
        }
        columns += ")";
        String unique = index.isUnique() ? "UNIQUE" : "";
        String statement = String.format("CREATE %s INDEX %s ON %s %s", unique, index.getName(), index.gtTable(),
                columns);
        execute(statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#addConstraint(au.csiro.casda.votools.config.ConstraintConfig)
     */
    @Override
    public void addConstraint(ConstraintConfig constraint) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);
        String srcColumns = String.join(", ", constraint.getSrcColumns());
        String dstColumns = String.join(", ", constraint.getDstColumns());
        String statement = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
                constraint.getSrcTable(), constraint.getName(), srcColumns, constraint.getDstTable(), dstColumns);
        if (constraint.isDeferrable())
        {
            statement += " DEFERRABLE";
        }
        if (constraint.isDeferred())
        {
            statement += " INITIALLY DEFERRED";
        }
        execute(statement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#createColumn(au.csiro.casda.votools.config.ColumnConfig,
     * au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void createColumn(ColumnConfig columnConfig) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);
        String sql = "ALTER TABLE " + columnConfig.gtTable().gtFullDbTableName() + " ADD "
                + getColumnLine(columnConfig);
        execute(sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#createTable(au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void createTable(TableConfig table) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE, "CREATE TABLE " + table.gtFullDbTableName());
        ColumnConfig[] columns = new ColumnConfig[table.getColumns().size()];
        for (ColumnConfig column : table.getColumns().values())
        {
            int order = column.getInt("column_order");
            if (order < 0 || order > columns.length || columns[order - 1] != null)
            {
                throw new ConfigurationException("All columns must have defined unique column order between 1 and N:"
                        + table.gtFullDbTableName() + "." + column.gtName());
            }
            columns[order-1] = column;
        }

        StringBuilder builder = new StringBuilder();
        for (ColumnConfig column : columns)
        {
            if (builder.length() > 0)
            {
                builder.append(",\n");
            }
            builder.append(getColumnLine(column));
        }
        for (String keyName : table.getKeys().keySet())
        {
            String keyLine = table.getKeys().get(keyName);
            builder.append(",\n CONSTRAINT ").append(keyName).append(" ");
            builder.append(keyLine);
        }
        builder.append("\n)");
        String sql = "CREATE TABLE " + table.gtFullDbTableName() + " (\n" + builder.toString();
        execute(sql);
        if (table.get(TableConfig.DESCRIPTION) != null)
        {
            execute(String.format("COMMENT ON TABLE %s IS %s", table.gtFullDbTableName(),
                    table.sql(TableConfig.DESCRIPTION)));
        }
        for (ColumnConfig column : table.getColumns().values())
        {
            if (column.get(ColumnConfig.DESCRIPTION) != null)
            {
                execute(String.format("COMMENT ON COLUMN %s.%s IS %s", table.gtFullDbTableName(), column.gtName(),
                        column.sql(ColumnConfig.DESCRIPTION)));
            }

        }
    }

    /**
     * Construct a table column entry based on column configuration attributes
     * 
     * @param columnConfig
     *            column configuration object
     * @return column entry
     */
    public String getColumnLine(ColumnConfig columnConfig)
    {
        String name = columnConfig.gtName();
        String type = columnConfig.getType();
        String notNull = columnConfig.getNotnull() != null && columnConfig.getNotnull().equals("true") ? "NOT NULL"
                : "";
        String unique = columnConfig.getUnique() != null && columnConfig.getUnique().equals("true") ? "UNIQUE" : "";
        String defaultValue = columnConfig.getDefaultvalue() != null ? "DEFAULT " + columnConfig.getDefaultvalue() : "";
        // if autoincrement replace with safe declaration
        if (defaultValue.contains("nextval('") && defaultValue.contains("::regclass)"))
        {
            defaultValue = "";
            type = type.toLowerCase().contains("big") ? "bigserial" : "serial";
        }
        String line = String.format("%s %s %s %s %s", name, type, notNull, defaultValue, unique);
        return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#updateColumn(au.csiro.casda.votools.config.ColumnConfig,
     * au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void updateColumn(ColumnConfig current, ColumnConfig updated) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        if (different(current.getUnique(), updated.getUnique()))
        {
            throw new ConfigurationException("Changes not supported, drop the column first");
        }
        String start = "ALTER TABLE " + current.gtTable().gtFullDbTableName() + " ALTER " + current.gtName();
        if (!current.getType().equals(updated.getType())) // update type
        {
            try
            {
                String sql = start + " TYPE " + updated.getType();
                execute(sql);
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Type change is not supported, drop the column first");
            }
        }
        if (different(current.getDefaultvalue(), updated.getDefaultvalue())) // update default value
        {
            String defaultValue = updated.getDefaultvalue() == null ? " DROP DEFAULT " : " SET DEFAULT "
                    + updated.getDefaultvalue();
            String sql = start + defaultValue;
            execute(sql);
        }
        if (different(current.getNotnull(), updated.getNotnull())) // update default value
        {
            String notNull = updated.getNotnull() == null || updated.getNotnull().equals("false") ? " DROP NOT NULL "
                    : " SET NOT NULL ";
            String sql = start + notNull;
            execute(sql);
        }
        if (different(current.get(ColumnConfig.DESCRIPTION), updated.get(ColumnConfig.DESCRIPTION)))
        {
            String comment = updated.sql(ColumnConfig.DESCRIPTION);
            String sql = String.format("COMMENT ON COLUMN %s.%s IS %s", current.gtTable().gtFullDbTableName(),
                    updated.gtName(), comment);
            execute(sql);
        }

    }

    /**
     * Returns true if two String objects are different
     * 
     * @param str1
     *            String 1
     * @param str2
     *            String 2
     * @return true if the strings are different
     */
    public boolean different(String str1, String str2)
    {
        if (str1 == null && str2 == null)
        {
            return false;
        }
        return str1 == null && str2 != null || str1 != null && str2 == null || !str1.equals(str2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.csiro.casda.votools.config.ConfigurationDAO#createConstraint(au.csiro.casda.votools.config.ConstraintConfig)
     */
    @Override
    public void createConstraint(ConstraintConfig conf) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        String srcColumns = String.join(", ", conf.getSrcColumns());
        String dstColumns = String.join(", ", conf.getDstColumns());
        String sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
                conf.getSrcTable(), conf.getName(), srcColumns, conf.getDstTable(), dstColumns);
        execute(sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#createIndex(au.csiro.casda.votools.config.TableConfig,
     * au.csiro.casda.votools.config.IndexConfig)
     */
    @Override
    public void createIndex(IndexConfig conf) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.REINDEX);

        String create = conf.gtTable().getIndexDefs().get(conf.getName());
        String drop = "DROP INDEX IF EXISTS " + conf.getName() + " CASCADE";
        execute(drop);
        try
        {
            execute(create);
        }
        catch (Exception e) // my have been caused by trying re-create a primary key
        {
            if (!e.getMessage().contains("already exists"))
            {
                throw new ConfigurationException(e);
            }
        }
    }

    private void checkAllowed(Configuration.Change change) throws ConfigurationException
    {
        if (!config.getChangeLevel().allows(change))
        {
            throw new ConfigurationException("Requested action requires " + change
                    + " change level, which is not allowed.");
        }
    }

    private void checkAllowed(Configuration.Change change, String changeDescription) throws ConfigurationException
    {
        if (!config.getChangeLevel().allows(change))
        {
            throw new ConfigurationException("Requested action requires " + change + " change level to '"
                    + changeDescription + "', which is not allowed.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#begin()
     */
    @Override
    public void begin()
    {
        execute("BEGIN READ WRITE");
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#commit()
     */
    @Override
    public void commit()
    {
        execute("COMMIT");
    }

    public Configuration getConfig()
    {
        return config;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#rollback()
     */
    @Override
    public void rollback()
    {
        execute("ROLLBACK");
    }

    /**
     * JdbcTemplate mapper class for constraints configs
     */
    public static class ConstraintConfigMapper implements RowMapper<String>
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            String line = String.format("%s %s", rs.getString("conname"), rs.getString("condef"));
            return line;
        }
    }

    /**
     * JdbcTemplate mapper class for column configs
     */
    public static class ColumnConfigMapper implements RowMapper<ColumnConfig>
    {
        private Map<String, ColumnConfig> configs;
        private List<String> fieldNames;
        private boolean updateOnly;

        /**
         * Constructor
         * 
         * @param configs
         *            a map of existing ColumnConfig objects to update or null
         */
        public ColumnConfigMapper(Map<String, ColumnConfig> configs)
        {
            this.configs = configs;
            updateOnly = configs != null && !configs.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public ColumnConfig mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            String name = rs.getString(ColumnConfig.NAME);
            if (fieldNames == null)
            {
                readFieldNames(rs);
            }
            ColumnConfig columnConfig = null;
            if (configs != null)
            {
                columnConfig = configs.get(name);
            }
            if (columnConfig == null)
            {
                if (updateOnly) // ignore columns that are not in the map yet
                {
                    return null;
                }
                columnConfig = new ColumnConfig();
                columnConfig.setName(name);
                if (configs != null)
                {
                    configs.put(name, columnConfig);
                }
            }
            Class<?> clasz = columnConfig.getClass();
            for (String fieldName : fieldNames)
            {
                String value = rs.getString(fieldName);
                if (value == null || value.equals("$default"))
                {
                    continue;
                }
                try
                {
                    String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    Method method = clasz.getMethod(methodName, String.class);
                    method.invoke(columnConfig, value);
                }
                catch (NoSuchMethodException e) // set value of this field as an option
                {
                    columnConfig.put(fieldName, value);
                }
                catch (SecurityException | IllegalArgumentException | IllegalAccessException
                        | InvocationTargetException e)
                {
                    throw new SQLException("Failed to set " + fieldName + " in ColumnConfig", e);
                }
            }

            return columnConfig;
        }

        /**
         * Reads returned field names from query metadata
         * 
         * @param rs
         *            query result set
         * @throws SQLException
         *             if there is a problem with reading metadata
         */
        void readFieldNames(ResultSet rs) throws SQLException
        {
            fieldNames = new ArrayList<String>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            // The column count starts from 1
            for (int i = 1; i <= columnCount; i++)
            {
                String name = rsmd.getColumnName(i);
                if (!name.equals(ColumnConfig.NAME))
                {
                    fieldNames.add(name);
                }
            }
        }
    }

    /**
     * JdbcTemplate mapper class for Strings
     */
    public static class StringMapper implements RowMapper<String>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            return rs.getString(1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#deleteTapSchema(au.csiro.casda.votools.config.SchemaConfig)
     */
    @Override
    public void deleteTapSchema(SchemaConfig schemaConfig) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        String sqlFind = String.format("SELECT db_schema_name||'.'||db_table_name FROM %s.%s WHERE schema_name='%s'",
                getSchema(), TABLES_TABLE_NAME, schemaConfig.gtName());
        List<String> tables = template.queryForList(sqlFind, String.class);

        for (String fullDbTableName : tables)
        {
            TableConfig table = new TableConfig(fullDbTableName, this);
            table.put(TableConfig.TAP_SCHEMA_NAME, schemaConfig.gtName());
            // Note that this does not delete the table itself, only its TAP records
            deleteTapTable(table);
        }
        // Now we can delete the schema itself
        String sqlDelete = String.format("DELETE FROM %s.%s WHERE schema_name='%s'", getSchema(), SCHEMAS_TABLE_NAME,
                schemaConfig.gtName());
        template.execute(sqlDelete);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#createTapSchema(au.csiro.casda.votools.config.SchemaConfig)
     */
    @Override
    public void createTapSchema(SchemaConfig schemaConfig) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        updateTapSchema(schemaConfig, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#updateTapSchema(au.csiro.casda.votools.config.SchemaConfig)
     */
    @Override
    public void updateTapSchema(SchemaConfig schemaConfig, boolean createOnly) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        SchemaConfig s = schemaConfig; // a shortcut
        insertUpdate(createOnly, SCHEMAS_TABLE_NAME, "schema_name='" + s.gtName() + "'", new String[] { "schema_name",
                "description", "utype" },
                new String[] { Utils.sql(s.gtName()), s.sql(SchemaConfig.DESCRIPTION), s.sql(SchemaConfig.UTYPE) });
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#deleteTapTable(au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void deleteTapTable(TableConfig cfgTable) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        // remove references to table columns from key tables
        removeKeys(cfgTable.gtFullTapTableName(), false);
        // remove columns records
        String sqlDelete = String.format("DELETE FROM %s.%s WHERE table_name='%s'", getSchema(), COLUMNS_TABLE_NAME,
                cfgTable.gtFullTapTableName());
        template.execute(sqlDelete);
        // now can remove table record
        sqlDelete = String.format("DELETE FROM %s.%s WHERE table_name='%s'", getSchema(), TABLES_TABLE_NAME,
                cfgTable.gtFullTapTableName());
        template.execute(sqlDelete);
        logger.info("Delete TAP metadata for table " + cfgTable.gtFullTapTableName());
    }

    /**
     * Removes references to this table from the keys table
     * 
     * @param tapTableName
     *            full TAP name of the table to remove references to
     * @param sourceOnly
     *            delete only keys that originate from this table
     */
    private void removeKeys(String tapTableName, boolean sourceOnly)
    {
        // find all keys where the column is involved
        String sqlFind = sourceOnly ? String.format("SELECT key_id FROM %s.%s WHERE from_table='%s'", getSchema(),
                KEYS_TABLE_NAME, tapTableName) : String.format(
                "SELECT key_id FROM %s.%s WHERE from_table='%s' OR target_table='%s'", getSchema(), KEYS_TABLE_NAME,
                tapTableName, tapTableName);
        String sqlDeleteKeys = sourceOnly ? String.format("DELETE FROM %s.%s WHERE from_table='%s'", getSchema(),
                KEYS_TABLE_NAME, tapTableName) : String.format(
                "DELETE FROM %s.%s WHERE from_table='%s' OR target_table='%s'", getSchema(), KEYS_TABLE_NAME,
                tapTableName, tapTableName);
        List<String> keys = template.queryForList(sqlFind, String.class);
        // Delete references to the keys
        for (String key : keys)
        {
            String sqlDeleteRef = String.format("DELETE FROM %s.%s WHERE key_id='%s'", getSchema(),
                    KEY_COLUMN_TABLE_NAME, key);
            template.execute(sqlDeleteRef);
        }
        // Now delete the keys
        template.execute(sqlDeleteKeys);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#createTapTable(au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void createTapTable(TableConfig tableConfig) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        updateTapTable(tableConfig, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#updateTapTable(au.csiro.casda.votools.config.TableConfig)
     */
    @Override
    public void updateTapTable(TableConfig cfgTable, boolean createOnly) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE, "Update TAP metadata");

        // Make sure the schema exists because there is a reference to it from the tables table
        insertUpdate(true, SCHEMAS_TABLE_NAME, "schema_name='" + cfgTable.gtTapSchemaName() + "'",
                new String[] { "schema_name" }, new String[] { Utils.sql(cfgTable.gtTapSchemaName()) });

        // if the table record does not exist create it based on the configuration object, else update
        String tName = cfgTable.gtFullDbTableName();
        String schemaName = cfgTable.gtTapSchemaName();
        String tapTableName = cfgTable.gtFullTapTableName();
        insertUpdate(
                createOnly,
                TABLES_TABLE_NAME,
                String.format("table_name='%s'", tapTableName),
                new String[] { "table_name", "table_type", "schema_name", "description", "utype", "db_schema_name",
                        "db_table_name", "scs_enabled", "release_required", "description_long", "params" },
                new String[] { Utils.sql(tapTableName), Utils.sql(cfgTable.getType()), Utils.sql(schemaName),
                        cfgTable.sql(TableConfig.DESCRIPTION), cfgTable.sql(TableConfig.UTYPE),
                        Utils.sql(cfgTable.gtSchema()), Utils.sql(cfgTable.gtShortName()),
                        String.valueOf(cfgTable.getBoolean(TableConfig.SCS_ENABLED)),                
                        String.valueOf(cfgTable.getBoolean(TableConfig.RELEASE_REQUIRED)),                 
                        cfgTable.sql(TableConfig.DESCRIPTION_LONG), cfgTable.sql(TableConfig.PARAMS) });

        // for each column in the configuration table
        for (ColumnConfig c : cfgTable.getColumns().values())
        {
            String dbColName = c.get(ColumnConfig.DB_COLUMN_NAME) == null ? Utils.sql(c.gtName())
                    : c.sql(ColumnConfig.DB_COLUMN_NAME);
            insertUpdate(
                    createOnly,
                    COLUMNS_TABLE_NAME,
                    String.format("column_name='%s' AND table_name='%s'", c.gtName(), tapTableName),
                    new String[] { "column_name", "table_name", "db_column_name", "description", "unit", "ucd", "utype",
                            "datatype", "size", "principal", "indexed", "std", "scs_verbosity", "column_order" },
                    new String[] { Utils.sql(c.gtName()), Utils.sql(tapTableName), dbColName,
                            c.sql(ColumnConfig.DESCRIPTION), c.sql(ColumnConfig.UNIT), c.sql(ColumnConfig.UCD),
                            c.sql(ColumnConfig.UTYPE), Utils.sql(c.getType()), c.sql(ColumnConfig.SIZE),
                            c.sql(ColumnConfig.PRINCIPAL), c.sql(ColumnConfig.INDEXED), c.sql(ColumnConfig.STD),
                            Utils.sql(c.get(ColumnConfig.SCS_VERBOSITY, "3")),
                            // column order can't be null, setting 0 as the default means the columns are unordered
                            Utils.sql(c.get(ColumnConfig.ORDER, "0")) });

        }
        if (!createOnly)
        {
            // Read existing columns and delete any that are not in the cfgTable
            Map<String, ColumnConfig> columns = new HashMap<String, ColumnConfig>();
            updateColumnsFromTap(tName, columns);
            for (ColumnConfig cc : columns.values())
            {
                if (!cfgTable.getColumns().containsKey(cc.gtName()))
                {
                    // delete references to the column
                    String deleteSql = String.format(
                            "DELETE FROM %s.%s WHERE (from_table='%s' AND from_column='%s') OR "
                                    + "(target_table='%s' AND target_column='%s') ", getSchema(),
                            KEY_COLUMN_TABLE_NAME, tapTableName, cc.gtName(), tapTableName, cc.gtName());
                    template.execute(deleteSql);
                    deleteSql = String.format("DELETE FROM %s.%s WHERE table_name='%s' AND column_name='%s'",
                            getSchema(), COLUMNS_TABLE_NAME, tapTableName, cc.gtName());
                    template.execute(deleteSql);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * au.csiro.casda.votools.config.ConfigurationDAO#updateTapTableReferences(au.csiro.casda.votools.config.TableConfig
     * )
     */
    @Override
    public void updateTapTableReferences(TableConfig cfgTable, boolean createOnly) throws ConfigurationException
    {
        checkAllowed(Configuration.Change.UPDATE);

        if (!createOnly)
        {
            // remove existing constraints
            removeKeys(cfgTable.gtFullTapTableName(), true);
        }

        // Get id counter because the table is defined without a serial id
        String maxIdSelect = String.format("SELECT max(id) from %s.%s", getSchema(), KEY_COLUMN_TABLE_NAME);
        Integer maxId = template.queryForObject(maxIdSelect, Integer.class);
        int idCounter = maxId == null ? 0 : maxId;

        for (ConstraintConfig c : cfgTable.gtConstraintConfigs().values())
        {
            TableConfig dstConfig = cfgTable.gtConfig().getTableConfig(c.getDstTable());
            String tapDstTableName = dstConfig.gtFullTapTableName();
            TableConfig srcConfig = cfgTable.gtConfig().getTableConfig(c.getSrcTable());
            String tapSrcTableName = srcConfig.gtFullTapTableName();

            // insert key record
            insertUpdate(
                    createOnly,
                    KEYS_TABLE_NAME,
                    String.format("key_id='%s'", c.getName()),
                    new String[] { "key_id", "from_table", "target_table", "description", "utype" },
                    new String[] { Utils.sql(c.getName()), Utils.sql(tapSrcTableName), Utils.sql(tapDstTableName),
                            c.sql(ConstraintConfig.DESCRIPTION), c.sql(ConstraintConfig.UTYPE), });
            // insert or update column key references
            for (int i = 0; i < c.getSrcColumns().size(); i++)
            {
                String srcColumn = c.getSrcColumns().get(i);
                String dstColumn = c.getDstColumns().get(i);
                String where = String.format(
                        "key_id='%s' AND from_column=%s AND target_column=%s AND from_table=%s AND target_table=%s",
                        c.getName(), Utils.sql(srcColumn), Utils.sql(dstColumn), Utils.sql(tapSrcTableName),
                        Utils.sql(tapDstTableName));

                String idSelect = String.format("SELECT (SELECT id FROM %s.%s WHERE %s)", getSchema(),
                        KEY_COLUMN_TABLE_NAME, where);
                Integer idObject = template.queryForObject(idSelect, Integer.class);
                int id = idObject == null ? ++idCounter : idObject;

                insertUpdate(
                        createOnly,
                        KEY_COLUMN_TABLE_NAME,
                        where,
                        new String[] { "id", "key_id", "from_column", "target_column", "from_table", "target_table" },
                        new String[] { String.valueOf(id), Utils.sql(c.getName()), Utils.sql(srcColumn),
                                Utils.sql(dstColumn), Utils.sql(tapSrcTableName), Utils.sql(tapDstTableName) });
            }
        }
    }

    /**
     * Creates or updates a record in the DB
     * 
     * @param createOnly
     *            if true, do not update the record if it exists
     * @param tableName
     *            name of table of the record
     * @param where
     *            where clause, e.g. 'id = 1'
     * @param columns
     *            array of column names
     * @param values
     *            array of corresponding column values
     */
    private void insertUpdate(boolean createOnly, String tableName, String where, String[] columns, String[] values)
    {
        tableName = getSchema() + "." + tableName;
        String countSql = String.format("SELECT count(*) FROM %s WHERE %s", tableName, where);
        Integer count = template.queryForObject(countSql, Integer.class);
        if (count == null || count <= 0)
        {
            StringBuilder columnList = new StringBuilder();
            StringBuilder valueList = new StringBuilder();
            for (int i = 0; i < values.length; i++)
            {
                columnList.append(i > 0 ? ", " + columns[i] : columns[i]);
                valueList.append(i > 0 ? ", " + values[i] : values[i]);
            }
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnList, valueList);
            template.execute(sql);
        }
        else
        {
            if (!createOnly)
            {
                StringBuilder list = new StringBuilder();
                for (int i = 0; i < values.length; i++)
                {
                    list.append(i > 0 ? ", " + columns[i] : columns[i]);
                    list.append("=").append(values[i]);
                }
                String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, list, where);
                template.execute(sql);
            }
        }
    }

    public JdbcTemplate getTemplate()
    {
        return template;
    }

    public void setTemplate(JdbcTemplate template)
    {
        this.template = template;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findAllSchemas()
     */
    @Override
    public Collection<TapSchema> findAllSchemas()
    {
        return tapCache.findAllSchemas();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findAllTables()
     */
    @Override
    public Collection<TapTable> findAllTables()
    {
        return tapCache.findAllTables();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findOneSchema(java.lang.String)
     */
    @Override
    public TapSchema findOneSchema(String schemaName)
    {
        return tapCache.findOneSchema(schemaName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findAllColumns()
     */
    @Override
    public Collection<TapColumn> findAllColumns()
    {
        return tapCache.findAllColumns();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findOneTable(java.lang.String)
     */
    @Override
    public TapTable findOneTable(String tableName)
    {
        return tapCache.findOneTable(tableName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findAllKeyColumns()
     */
    @Override
    public Collection<TapKeyColumn> findAllKeyColumns()
    {
        return tapCache.findAllKeyColumns();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findOneColumn(au.csiro.casda.votools.jpa.TapColumnPK)
     */
    @Override
    public TapColumn findOneColumn(TapColumnPK tapColumnPK)
    {
        return tapCache.findOneColumn(tapColumnPK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.ConfigurationDAO#findAllKeys()
     */
    @Override
    public Collection<TapKey> findAllKeys()
    {
        return tapCache.findAllKeys();
    }

    /**
     * Get schema name
     * 
     * @return schema name or default schema name if not found
     */
    public String getSchema()
    {
        String schemaName = config.get(Configuration.DEFAULT_DB_SCHEMA);
        if (schemaName != null)
        {
            setSchema(schemaName);
        }
        if (schema == null)
        {
            setSchema(DEFAULT_SCHEMA_NAME);
        }
        return schema;
    }

    public static void setSchema(String schema)
    {
        ConfigurationDAOImpl.schema = schema;
    }

    // @formatter:off
    /** Get column SQL */
    private static final String GET_COLUMNS_SQL = "SELECT f.attname AS name, f.attnum AS column_order, "
                    + "pg_catalog.format_type(f.atttypid,f.atttypmod) AS type, "
                    + "CASE WHEN f.attnotnull = 't' THEN 'true' ELSE '$default' END AS notnull, "
                    + "CASE WHEN p.contype = 'p' THEN 'true' ELSE '$default' END AS primarykey, "
                    + "CASE WHEN p.contype = 'u' THEN 'true' ELSE '$default' END AS unique, "
                    + "CASE WHEN f.atthasdef = 't' THEN d.adsrc END AS defaultvalue FROM pg_attribute f "
                    + "JOIN pg_class c ON c.oid = f.attrelid " + "JOIN pg_type t ON t.oid = f.atttypid "
                    + "LEFT JOIN pg_attrdef d ON d.adrelid = c.oid AND d.adnum = f.attnum "
                    + "LEFT JOIN pg_namespace n ON n.oid = c.relnamespace "
                    + "LEFT JOIN pg_constraint p ON p.conrelid = c.oid AND f.attnum = ANY (p.conkey) "
                    + "LEFT JOIN pg_class AS g ON p.confrelid = g.oid "
                    + "WHERE (c.relkind = 'r'::char OR c.relkind = 'v'::char) AND n.nspname = ? AND c.relname = ? "
                    + "AND f.attnum > 0 order by column_order";

    /** Get column comment SQL */
    private static final String GET_COLUMN_COMMENTS_SQL =
            "SELECT c.column_name AS name, pgd.description AS description "
                    + "FROM pg_catalog.pg_statio_all_tables AS st "
                    + "INNER JOIN pg_catalog.pg_description pgd ON (pgd.objoid=st.relid) "
                    + "INNER JOIN information_schema.columns c ON (pgd.objsubid=c.ordinal_position "
                    + "AND c.table_schema=st.schemaname AND c.table_name=st.relname) "
                    + "WHERE c.table_schema=? AND c.table_name=?";

    /** Get index definition SQL */
    private static final String GET_INDEX_DEFINITION_SQL =
            "SELECT pg_get_indexdef(indexrelid) AS indexLine FROM pg_index WHERE indrelid = ?::regclass";

    /**
     * Get table names SQL private String getTableNamesSql = "SELECT table_name FROM information_schema.tables " +
     * "WHERE table_type = 'BASE TABLE' AND table_schema = ?" ;
     */

    /** Get constraints SQL */
    private static final String GET_CONSTRAINTS_SQL =
            "SELECT conname, pg_get_constraintdef(c.oid) as condef FROM pg_constraint c JOIN pg_namespace n ON "
                    + "n.oid = c.connamespace WHERE contype = ? AND conrelid::regclass=?::regclass";

    /** Get table description SQL */
    private static final String GET_TABLE_DESCRIPTION_SQL = "SELECT obj_description(?::regclass, 'pg_class')";

    private static enum FieldTypes
    {
        TYPE_REAL("real", "4"),
        TYPE_SMALL_INT("smallint", "2"),
        TYPE_TIMESTAMP_WITH_ZONE("timestamp with time zone", "8"),
        TYPE_TIMESTAMP_WITHOUT_ZONE("timestamp without time zone", "8"),
        TYPE_INTEGER("integer", "4"),
        TYPE_BIG_INT("bigint", "8"),
        TYPE_DOUBLE("double precision", "8"),
        TYPE_BOOLEAN("boolean", "1"),
        TYPE_GEOMETRY("geometry", null),
        TYPE_VARCHAR("character varying(",null);
        
        
        private String keyword;
        private String size;

        FieldTypes(String keyword, String size)
        {
            this.keyword = keyword;
            this.size = size;
        }
    }
    
    /**
     * Convert project codes to project ids
     * 
     * @param projectCodes
     *            list of project codes
     * @param schema
     *            Schema of the projects table
     * @return projectIds List of project ids for codes
     */
    public List<Long> convertProjectCodesToIds(List<String> projectCodes, String schema)
    {
        if (CollectionUtils.isEmpty(projectCodes))
        {
            return null;
        }

        List<Long> projectIds = new ArrayList<>();
        String sql = "select id from " + schema + "." + VoKeys.STR_PROJECT_TABLE_NAME
                + " where opal_code in (:codes)";
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("codes", projectCodes);
        projectIds.addAll(namedParameterJdbcTemplate.queryForList(sql, parameters, Long.class));

        return projectIds;
    }
}
