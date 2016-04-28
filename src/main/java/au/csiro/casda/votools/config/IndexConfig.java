package au.csiro.casda.votools.config;

import java.util.HashSet;
import java.util.Set;

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
 * Index configuration object
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class IndexConfig extends Options
{
    /** table the index belongs to */
    private TableConfig table;

    /** index name */
    private String name;

    /** indexed columns names */
    private Set<String> columns;

    /** true if the index is unique */
    private boolean unique;

    /**
     * Parameterless constructor
     */
    public IndexConfig()
    {
        super();
        columns = new HashSet<String>();
    }

    /**
     * Creates IndexConfig object based on string encoding in form of index definition statement
     * 
     * @param table
     *            table the index belongs to
     * @param str
     *            string encoding of the index
     * @throws ConfigurationException
     *             if detects use of reserved words as column names
     * 
     */
    public IndexConfig(TableConfig table, String str) throws ConfigurationException
    {
        this();
        this.table = table;
        name = str.substring(str.indexOf(" INDEX ") + " INDEX ".length(), str.indexOf(" ON "));
        String[] columnNames = str.substring(str.indexOf("(") + 1, str.indexOf(")")).replace(" ", "").split(",");
        for (String columnName : columnNames)
        {
            if (columnName.contains("\"") || columnName.contains("\'") || columnName.contains("`"))
            {
                throw new ConfigurationException(columnName
                        + "is a reserved word. Using reserved words as column names is not supoported.");
            }
            columns.add(columnName);
        }
        unique = str.toLowerCase().contains("unique");
    }

    /**
     * Equivalence test
     * 
     * @param other
     *            IndexConfig to compare to
     * @return true if equal, else false
     */
    public boolean eqials(IndexConfig other)
    {
        return super.equals(other) && equalsXOptions(other);
    }
    
    /**
     * Equivalence test, ignoring options
     * 
     * @param other
     *            IndexConfig to compare to
     * @return true if equal, else false
     */
    public boolean equalsXOptions(IndexConfig other)
    {
        return columns.equals(other.columns) && name.equals(other.name)
                && unique == other.unique;
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Set<String> getColumns()
    {
        return columns;
    }

    public void setColumns(Set<String> columns)
    {
        this.columns = columns;
    }

    /**
     * Checks presence of the column
     * 
     * @param column
     *            column name
     * @return true if present
     */
    public boolean hasColumn(String column)
    {
        return columns.contains(column);
    }

    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(boolean unique)
    {
        this.unique = unique;
    }

    /**
     * Drops this index
     * 
     * @throws ConfigurationException
     *             if configuration does not allow to drop an index
     */
    public void delete() throws ConfigurationException
    {
        this.gtTable().gtConfig().initDao().dropIndex(this);
    }

    /**
     * Create this index
     * 
     * @throws ConfigurationException
     *             if this change is not allowed or a DB problem occurred
     */
    public void create() throws ConfigurationException
    {
        this.gtTable().gtConfig().initDao().createIndex(this);
    }

    /**
     * Modify this index
     * 
     * @throws ConfigurationException
     *             if dropping index is not allowed
     */
    public void update() throws ConfigurationException
    {
        delete();
        create();
    }

    /**
     * A replacement for getter to hide it from YAML parsers
     * 
     * @return table value
     */
    public TableConfig gtTable()
    {
        return table;
    }

    public void setTable(TableConfig table)
    {
        this.table = table;
    }

}
