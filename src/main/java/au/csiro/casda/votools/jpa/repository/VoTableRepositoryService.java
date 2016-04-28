package au.csiro.casda.votools.jpa.repository;

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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationDAO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;

/**
 * Service to act as a gateway to all the other VOSI Table Repositories. In the initial implementation this doesn't do
 * much more than mirror what the 'child' repositories do. That will change over time.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.NO)
public class VoTableRepositoryService extends Configurable
{
    /** database access */
    private ConfigurationDAO dao;


    /**
     * A constructor
     * @param configRegistry 
     * @throws ConfigurationException
     *             if a configuration problem occurs
     */
    @Autowired
    public VoTableRepositoryService(ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        configRegistry.register(this);
    }

    /**
     * Refreshes the object cache, which cached tap metadata cached by the application.
     */
    public void refreshObjectCache()
    {
        dao.refreshObjectCache();
    }
    
    /**
     * Get all schemas
     * 
     * @return ALL TapSchemas
     */
    public List<TapSchema> getSchemas()
    {
        return dao == null ? null : new ArrayList<>(dao.findAllSchemas());
    }

    /**
     * Get all tables
     * 
     * @return ALL TapTables
     */
    public List<TapTable> getTables()
    {
        return dao == null ? null : new ArrayList<>(dao.findAllTables());
    }
    
    /**
     * Find TapTable by table name
     * 
     * @param tableName
     *            Name of the table
     * @return TapTable
     */
    public TapTable getTableByName(String tableName)
    {
        return dao.findOneTable(tableName);
    }

    /**
     * Given a schema name return the tables that belong to it.
     * 
     * @param schemaName
     *            the schema to get tables from
     * @return the tables that belong to the given schema
     */
    public List<TapTable> getSchemaTables(String schemaName)
    {
        TapSchema tapSchema = dao == null ? null : dao.findOneSchema(schemaName);

        return dao == null ? null : tapSchema.getTables();
    }

    /**
     * Get all columns
     * 
     * @return ALL columns
     */
    public List<TapColumn> getColumns()
    {
        return dao == null ? null : new ArrayList<>(dao.findAllColumns());
    }

    /**
     * Given a table name return the TapColums that belong to it.
     * 
     * @param tableName
     *            the table to get columns for
     * @return the TapColums that belong to the given table.
     */
    public List<TapColumn> getTableColumns(String tableName)
    {
        TapTable tapTable = dao == null ? null : dao.findOneTable(tableName);

        return dao == null ? null : tapTable.getColumns();
    }

    /**
     * Get all KeyColumns
     * 
     * @return ALL KeyColumns
     */
    public List<TapKeyColumn> getKeyColumns()
    {
        return dao == null ? null : new ArrayList<>(dao.findAllKeyColumns());
    }

    /**
     * Given a column = (tableName, columnName), it returns the KeyColumns that have that column as a 'from column'
     * 
     * @param tableName
     *            The schema and name of the table to be queried. e.g. casda.continuum
     * @param columnName
     *            The column name ot be queried. e.g. catalogue_id
     * @return the TapKeyColumns that have the given column as a 'from column'
     */
    public List<TapKeyColumn> getColumnFromKeyColumns(String tableName, String columnName)
    {
        TapColumn tapColumn = dao == null ? null : dao.findOneColumn(new TapColumnPK(tableName, columnName));

        return dao == null ? null : tapColumn.getFromKeyColumns();
    }

    /**
     * Given a column = (tableName, columnName), it returns the KeyColumns that have that column as a 'target column'
     * 
     * @param tableName
     *            The schema and name of the table to be queried. e.g. casda.continuum
     * @param columnName
     *            The column name ot be queried. e.g. catalogue_id
     * @return the TapKeyColumns that have the given column as a 'target column'
     */
    public List<TapKeyColumn> getColumnTargetColumnKeyColumns(String tableName, String columnName)
    {
        TapColumn tapColumn = dao == null ? null : dao.findOneColumn(new TapColumnPK(tableName, columnName));

        return dao == null ? null : tapColumn.getTargetKeyColumns();
    }

    /**
     * Get all TapKeys
     * 
     * @return ALL TapKeys.
     */
    public List<TapKey> getKeys()
    {
        return dao == null ? null : new ArrayList<>(dao.findAllKeys());
    }

    /**
     * Given a table name return the TapKeys that have this table as a 'from table'
     * 
     * @param tableName
     *            The schema and name of the table to be queried. e.g. casda.continuum
     * @return from keys from given tableName
     */
    public List<TapKey> getFromKeys(String tableName)
    {
        return dao == null ? null : dao.findOneTable(tableName).getFromTableKeys();
    }

    /**
     * Given a table name return the TapKeys that have this table as a 'target table'
     * 
     * @param tableName
     *            The schema and name of the table to be queried. e.g. casda.continuum
     * @return target keys for given tableName
     */
    public List<TapKey> getTargetKeys(String tableName)
    {
        return dao == null ? null : dao.findOneTable(tableName).getTargetTableKeys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        try
        {
            dao = config.initDao();
        }
        catch (ConfigurationException e)
        {
            dao = null; // must be incomplete configuration
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        return dao != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        dao = null;
    }
    
    /**
     * Fetch matching project Ids for project codes
     * 
     * @param projectCodes
     *            Project codes list
     * @param schema
     *            Database schema
     * @return List of Projects Ids as Long
     */
    public List<Long> fetchProjectIdsFromCodes(List<String> projectCodes, String schema)
    {
        return dao.convertProjectCodesToIds(projectCodes, schema);
    }

    // TODO - Rather than getFromKeyColumns(), getTargetKeyColumns(), getFromKeys(), getTargetKeys() etc... we more
    // likely require something about FK relationships but will defer this until needed.
}
