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


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;

/**
 * A cache of linked TAP metadata objects with functions necessary for reading them from the database.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class TapObjectCache
{
    /** Tap schemas mapped to their names */
    private Map<String, TapSchema> schemas;

    /** Tap tables mapped to their full names */
    private Map<String, TapTable> tables;

    /** Tap columns mapped to their full (shema.table.column) names */
    private Map<String, TapColumn> columns;

    /** Tap keys mapped to their names */
    private Map<String, TapKey> keys;

    /** Tap key columns mapped to their integer ids */
    private Map<Integer, TapKeyColumn> keyColumns;

    /** Is true if cache is in synch with the database */
    private boolean valid;

    /** used to access the DB */
    private JdbcTemplate template;

    /** TAP metadata schema name */
    private String schema;

    /**
     * A constructor, sets containers
     */
    public TapObjectCache()
    {
        schemas = new LinkedHashMap<String, TapSchema>();
        tables = new LinkedHashMap<String, TapTable>();
        columns = new HashMap<String, TapColumn>();
        keys = new HashMap<String, TapKey>();
        keyColumns = new HashMap<Integer, TapKeyColumn>();
    }

    /**
     * A JdbcTemplate based constructor
     * 
     * @param template
     *            JdbcTemplate to use for DB access
     */
    public TapObjectCache(JdbcTemplate template)
    {
        this();
        this.template = template;
    }

    /**
     * Switch to new configuration
     * 
     * @param config
     *            configuration to set
     * @throws ConfigurationException
     *             if there were connection problems
     */
    public void setConfiguration(Configuration config) throws ConfigurationException
    {
        valid = false;
        template = config.gtDao().getTemplate();
        schema = config.gtDao().getSchema();
    }

    /**
     * Find all TAP schemas
     * 
     * @return a collection of TapSchema objects
     */
    public Collection<TapSchema> findAllSchemas()
    {
        makeSureIsValid();
        return schemas.values();
    }

    /**
     * Find all TAP tables
     * 
     * @return a collection of TapTable objects
     */
    public Collection<TapTable> findAllTables()
    {
        makeSureIsValid();
        return tables.values();
    }

    /**
     * Find all TAP columns
     * 
     * @return a collection of TapColumn objects
     */
    public Collection<TapColumn> findAllColumns()
    {
        makeSureIsValid();
        return columns.values();
    }

    /**
     * Find all TAP key columns
     * 
     * @return a collection of TapKeyColumn objects
     */
    public Collection<TapKeyColumn> findAllKeyColumns()
    {
        makeSureIsValid();
        return keyColumns.values();
    }

    /**
     * Find all TAP keys
     * 
     * @return a collection of TapKey objects
     */
    public Collection<TapKey> findAllKeys()
    {
        makeSureIsValid();
        return keys.values();
    }

    /**
     * Find a TAP schema by name
     * 
     * @param schemaName
     *            schema name
     * @return TapSchema object
     */
    public TapSchema findOneSchema(String schemaName)
    {
        makeSureIsValid();
        return schemas.get(schemaName);
    }

    /**
     * Find a TAP table by name
     * 
     * @param tableName
     *            column name
     * @return TapTable object
     */
    public TapTable findOneTable(String tableName)
    {
        makeSureIsValid();
        return tables.get(tableName);
    }

    /**
     * Find a TAP column by its key
     * 
     * @param tapColumnPK
     *            column key
     * @return TapColumn object
     */
    public TapColumn findOneColumn(TapColumnPK tapColumnPK)
    {
        makeSureIsValid();
        return columns.get(tapColumnPK.getTableName() + "." + tapColumnPK.getColumnName());
    }

    /**
     * Rebuilds cache if it is invalid.
     */
    private synchronized void makeSureIsValid()
    {
        if (valid)
        {
            return;
        }
        
        // clear the cache, in case tables etc have been deleted
        this.columns.clear();
        this.keyColumns.clear();
        this.keys.clear();
        this.schemas.clear();
        this.tables.clear();
        
        // read in the latest values of the tap metadata 
        readSchemas();
        readTables();
        readColumns();
        readKeys();
        readKeyColumns();
        
        valid = true;
    }
    
    /**
     * Triggers refresh of the tap metadata.
     */
    public void refresh()
    {
        valid = false;
    }
    
    /**
     * Reads all TAP schemas
     */
    private void readSchemas()
    {
        template.query(prepare(GET_SCHEMAS_SQL), new Object[] {}, new SchemaMapper());
    }

    /**
     * JdbcTemplate mapper class for TAP schemas
     */
    public class SchemaMapper implements RowMapper<TapSchema>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TapSchema mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            String name = rs.getString(TapSchema.NAME);
            TapSchema schema = new TapSchema(name);
            schema.setDescription(rs.getString(TapSchema.DESCRIPTION));
            schema.setDescription(rs.getString(TapSchema.UTYPE));
            try
            {
                schemas.put(name, schema);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new SQLException(e);
            }
            return schema;
        }
    }

    /**
     * 
     */
    private void readTables()
    {
        template.query(prepare(GET_TABLES_SQL), new Object[] {}, new TableMapper());
    }

    /**
     * JdbcTemplate mapper class for TAP tables
     */
    public class TableMapper implements RowMapper<TapTable>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TapTable mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            String name = rs.getString(TapTable.NAME);
            TapTable table = new TapTable(name);
            table.setTableType(rs.getString(TapTable.TYPE));
            table.setDescription(rs.getString(TapTable.DESCRIPTION));
            table.setDescriptionLong(rs.getString(TapTable.DESCRIPTION_LONG));
            table.setUtype(rs.getString(TapTable.UTYPE));
            table.setDbSchemaName(rs.getString(TapTable.DB_SCHEMA));
            table.setDbTableName(rs.getString(TapTable.DB_NAME));
            table.setScsEnabled(rs.getBoolean(TapTable.SCS_ENABLED));
            table.setReleaseRequired(rs.getBoolean(TapTable.RELEASE_REQUIRED));
            table.setParams(rs.getString(TapTable.PARAMS));
            String tapSchemaName = rs.getString(TapTable.SCHEMA_NAME);
            TapSchema schema = schemas.get(tapSchemaName);
            schema.addTable(table);
            table.setSchema(schema);
            tables.put(name, table);
            return table;
        }
    }

    /**
     * 
     */
    private void readColumns()
    {
        template.query(prepare(GET_COLUMNS_SQL), new Object[] {}, new ColumnMapper());
    }

    /**
     * JdbcTemplate mapper class for TAP columns
     */
    public class ColumnMapper implements RowMapper<TapColumn>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TapColumn mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            TapColumn column = new TapColumn();
            String name = rs.getString(TapColumn.NAME);
            String table = rs.getString(TapColumn.TABLE);
            TapColumnPK pk = new TapColumnPK(table, name);
            column.setId(pk);
            column.setDbColumnName(rs.getString(TapColumn.DB_COLUMN_NAME));
            column.setColumnOrder(rs.getInt(TapColumn.ORDER));
            column.setDescription(rs.getString(TapColumn.DESCRIPTION));
            column.setUnit(rs.getString(TapColumn.UNIT));
            column.setUcd(rs.getString(TapColumn.UCD));
            column.setUtype(rs.getString(TapColumn.UTYPE));
            column.setDatatype(rs.getString(TapColumn.DATATYPE));
            column.setSize(rs.getInt(TapColumn.SIZE));
            column.setPrincipal(rs.getInt(TapColumn.PRINCIPAL));
            column.setIndexed(rs.getInt(TapColumn.INDEXED));
            column.setStd(rs.getInt(TapColumn.STD));
            column.setScsVerbosity(rs.getInt(TapColumn.SCS_VERBOSITY));
            columns.put(table + "." + name, column);
            tables.get(table).addColumn(column);
            return column;
        }
    }

    /**
     * Read all TAP keys
     */
    private void readKeys()
    {
        template.query(prepare(GET_KEYS_SQL), new Object[] {}, new KeyMapper());
    }

    /**
     * Prepare SQL statement by inserting current schema name
     * 
     * @param sql
     *            SQL statement string
     * @return prepared SQL string
     */
    private String prepare(String sql)
    {
        return sql.replace("'schema'", schema);
    }

    /**
     * JdbcTemplate mapper class for TAP keys
     */
    public class KeyMapper implements RowMapper<TapKey>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TapKey mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            TapKey key = new TapKey();
            key.setKeyId(rs.getString(TapKey.ID));
            TapTable fromTable = tables.get(rs.getString(TapKey.FROM));
            key.setFromTable(fromTable);
            fromTable.addFromTableKey(key);
            TapTable targetTable = tables.get(rs.getString(TapKey.TARGET));
            key.setTargetTable(targetTable);
            targetTable.addTargetTableKey(key);
            key.setDescription(rs.getString(TapKey.DESCRIPTION));
            key.setUtype(rs.getString(TapKey.UTYPE));
            keys.put(key.getKeyId(), key);
            return key;
        }
    }

    /**
     * Read all TAP key columns
     */
    private void readKeyColumns()
    {
        template.query(prepare(GET_KEY_COLUMNS_SQL), new Object[] {}, new KeyColumnMapper());
    }

    /**
     * JdbcTemplate mapper class for TAP key columns
     */
    public class KeyColumnMapper implements RowMapper<TapKeyColumn>
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public TapKeyColumn mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            TapKeyColumn column = new TapKeyColumn();
            column.setId(rs.getInt(TapKeyColumn.ID));
            TapKey key = keys.get(rs.getString(TapKeyColumn.KEY_ID));
            column.setKey(key);
            key.addKeyColumn(column);
            column.setTargetColumn(columns.get(rs.getString(TapKeyColumn.TARGET_TABLE) + "."
                    + rs.getString(TapKeyColumn.TARGET_COLUMN)));
            column.setFromColumn(columns.get(rs.getString(TapKeyColumn.FROM_TABLE) + "."
                    + rs.getString(TapKeyColumn.FROM_COLUMN)));
            keyColumns.put(column.getId(), column);
            return column;
        }
    }

    private static final String GET_SCHEMAS_SQL = "SELECT schema_name, description, utype FROM 'schema'."
            + ConfigurationDAOImpl.SCHEMAS_TABLE_NAME + " ORDER BY upper(schema_name)";

    private static final String GET_TABLES_SQL = "SELECT table_name, table_type, schema_name, description, "
            + "utype, db_schema_name, db_table_name, scs_enabled, release_required, description_long, params FROM 'schema'."
            + ConfigurationDAOImpl.TABLES_TABLE_NAME + " ORDER BY schema_name, table_name";

    private static final String GET_COLUMNS_SQL = "SELECT column_name, table_name, db_column_name, description, unit, ucd, "
            + "utype, datatype, size, principal, indexed, std, scs_verbosity, column_order FROM 'schema'."
            + ConfigurationDAOImpl.COLUMNS_TABLE_NAME + " ORDER BY column_order";

    private static final String GET_KEYS_SQL = "SELECT key_id, from_table, target_table, description, utype FROM 'schema'."
            + ConfigurationDAOImpl.KEYS_TABLE_NAME;

    private static final String GET_KEY_COLUMNS_SQL = "SELECT id, key_id, from_column, target_column, from_table, "
            + "target_table FROM 'schema'." + ConfigurationDAOImpl.KEY_COLUMN_TABLE_NAME;

}
