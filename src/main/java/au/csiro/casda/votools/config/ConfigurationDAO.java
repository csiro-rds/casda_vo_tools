package au.csiro.casda.votools.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;

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
 * A set of configuration related DB methods
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public interface ConfigurationDAO
{
    /**
     * Export table configuration information
     * 
     * @param fullTableName
     *            full table name, including schema
     * @return column configurations mapped by column name
     */
    public Map<String, ColumnConfig> exportColumns(String fullTableName);

    /**
     * Adds column comments to column configs
     * 
     * @param fullTableName
     *            full table name
     * @param configs
     *            ColumnConfig objects mapped to column names
     */
    public void addColumnComments(String fullTableName, Map<String, ColumnConfig> configs);

    /**
     * Updates table configuration based on information available from TAP tables table
     * 
     * @param fullTableName
     *            full name of table to update
     * @param config
     *            table configuration
     * @return false if the table does not exist, else true
     */
    public boolean updateTableFromTap(String fullTableName, TableConfig config);

    /**
     * Updates columns configuration based on information available from TAP columns table
     * 
     * @param fullTableName
     *            full name of table that the columns belong to
     * @param configs
     *            columns configurations
     */
    public void updateColumnsFromTap(String fullTableName, Map<String, ColumnConfig> configs);

    /**
     * Get index definition statements
     * 
     * @param fullTableName
     *            full name of table that the columns belong to
     * @return index definition statements as Strings in a List
     */
    public Map<String, String> exportIndexDefs(String fullTableName);

    /**
     * Get constraints information
     * 
     * @param fullDbTableName
     *            full database name of table that the columns belong to
     * @param constraintType
     *            ConfigurationDAOImpl.FOREIGN_KEYS for foreign keys, ConfigurationDAOImpl.PRIMARY_KEYS for primary keys
     * @return Map constraint name -$gt; encoded constraint string
     */
    public Map<String, String> exportConstraints(String fullDbTableName, String constraintType);

    /**
     * Query for strings using the template
     * 
     * @param sql
     *            sql query with params placeholders
     * @param params
     *            an array of params
     * @return a list of String results
     */
    public List<String> queryForStrings(String sql, Object[] params);

    /**
     * Get description of given table in given schema
     * 
     * @param fullTableName
     *            full table name
     * 
     * @return table description as a String
     */
    public String exportTableDescription(String fullTableName);

    /**
     * Executes a statement
     * 
     * @param statement
     *            sql statement
     */
    public void execute(String statement);

    /**
     * Drops a constraint
     * 
     * @param constraint
     *            constraint to drop
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void dropConstraint(ConstraintConfig constraint) throws ConfigurationException;

    /**
     * Adds a constraint
     * 
     * @param constraint
     *            constraint to add
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void addConstraint(ConstraintConfig constraint) throws ConfigurationException;

    /**
     * Drops an index
     * 
     * @param index
     *            index to drop
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void dropIndex(IndexConfig index) throws ConfigurationException;

    /**
     * Adds an index
     * 
     * @param index
     *            index to add
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void addIndex(IndexConfig index) throws ConfigurationException;

    /**
     * Begin transaction
     */
    public void begin();

    /**
     * Commit transaction
     */
    public void commit();

    /**
     * Rollback transaction
     */
    public void rollback();

    /**
     * Drops column
     * 
     * @param columnConfig
     *            column configuration object of the column to drop
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void dropColumn(ColumnConfig columnConfig) throws ConfigurationException;

    /**
     * Creates column
     * 
     * @param columnConfig
     *            column attributes
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void createColumn(ColumnConfig columnConfig) throws ConfigurationException;

    /**
     * Updates column
     * 
     * @param current
     *            current column attributes
     * @param updated
     *            desired column attributes
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void updateColumn(ColumnConfig current, ColumnConfig updated) throws ConfigurationException;

    /**
     * Creates foreign key constraint
     * 
     * @param constraintConfig
     *            constraint configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void createConstraint(ConstraintConfig constraintConfig) throws ConfigurationException;

    /**
     * Creates index
     * 
     * @param indexConfig
     *            index configuration
     * @throws ConfigurationException
     *             if change is not allowed
     * 
     */
    public void createIndex(IndexConfig indexConfig) throws ConfigurationException;

    /**
     * Creates a table
     * 
     * @param tableConfig
     *            table configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void createTable(TableConfig tableConfig) throws ConfigurationException;

    /**
     * Delete schema from TAP metadata tables
     * 
     * @param schemaConfig
     *            schema configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void deleteTapSchema(SchemaConfig schemaConfig) throws ConfigurationException;

    /**
     * Create schema in TAP metadata tables
     * 
     * @param schemaConfig
     *            schema configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void createTapSchema(SchemaConfig schemaConfig) throws ConfigurationException;

    /**
     * Update schema in TAP metadata tables
     * 
     * @param newSchema
     *            schema configuration
     * @param createOnly
     *            do not update the record if it exists
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void updateTapSchema(SchemaConfig newSchema, boolean createOnly) throws ConfigurationException;

    /**
     * Delete table from TAP metadata tables
     * 
     * @param tableConfig
     *            table configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void deleteTapTable(TableConfig tableConfig) throws ConfigurationException;

    /**
     * Create table in TAP metadata tables
     * 
     * @param tableConfig
     *            table configuration
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void createTapTable(TableConfig tableConfig) throws ConfigurationException;

    /**
     * Update table in TAP metadata tables
     * 
     * @param newTable
     *            table configuration
     * @param createOnly
     *            do not update the record if it exists
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void updateTapTable(TableConfig newTable, boolean createOnly) throws ConfigurationException;

    /**
     * Adds missing records and references defined by the table's keys
     * 
     * @param cfgTable
     *            TableConfig object to process
     * @param createOnly
     *            do not update the record if it exists
     * @throws ConfigurationException
     *             if change is not allowed
     */
    void updateTapTableReferences(TableConfig cfgTable, boolean createOnly) throws ConfigurationException;

    /* =============================================================================== */
    /* Replacements for former JPA repositories functions */
    /* =============================================================================== */

    /**
     * Find all TAP schemas
     * 
     * @return a collection of TapSchema objects
     */
    public Collection<TapSchema> findAllSchemas();

    /**
     * Find all TAP tables
     * 
     * @return a collection of TapTable objects
     */
    public Collection<TapTable> findAllTables();

    /**
     * Find a TAP schema by name
     * 
     * @param schemaName
     *            schema name
     * @return TapSchema object
     */
    public TapSchema findOneSchema(String schemaName);

    /**
     * Find all TAP columns
     * 
     * @return a collection of TapColumn objects
     */
    public Collection<TapColumn> findAllColumns();

    /**
     * Find a TAP table by name
     * 
     * @param tableName
     *            column name
     * @return TapTable object
     */
    public TapTable findOneTable(String tableName);

    /**
     * Find all TAP key columns
     * 
     * @return a collection of TapKeyColumn objects
     */
    public Collection<TapKeyColumn> findAllKeyColumns();

    /**
     * Find a TAP column by its key
     * 
     * @param tapColumnPK
     *            column key
     * @return TapColumn object
     */
    public TapColumn findOneColumn(TapColumnPK tapColumnPK);

    /**
     * Find all TAP keys
     * 
     * @return a collection of TapKey objects
     */
    public Collection<TapKey> findAllKeys();

    /**
     * Get JdbcTemplate
     * 
     * @return JdbcTemplate used by this DAO
     */
    public JdbcTemplate getTemplate();

    /**
     * Get schema name
     * 
     * @return schema name of any existing table or default schema name
     */
    public String getSchema();

    /**
     * Make sure that TAP tables satisfy current version requirements
     * 
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    void checkTapDbVersion() throws ConfigurationException;

    /**
     * Refreshes the object cache, for TAP metadata cached by the application.
     */
    public void refreshObjectCache();
    
    /**
     * Convert project codes to project ids
     * 
     * @param projectCodes
     *            list of project codes
     * @param schema
     *            Schema of the projects table
     * @return projectIds List of project ids for codes
     */
    public List<Long> convertProjectCodesToIds(List<String> projectCodes, String schema);
    
}
