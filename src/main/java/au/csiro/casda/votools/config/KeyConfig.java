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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Primary keys constraint configuration object.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class KeyConfig extends Options
{
    /** constraint name */
    private String name;

    /** table configuration object */
    private TableConfig table;

    /** name of source column */
    private List<String> columns;

    /**
     * Parameterless constructor
     */
    public KeyConfig()
    {
        super();
        columns = new ArrayList<String>();
    }

    /**
     * Creates KeyConfig object based on string encoding in form "PRIMARY KEY (scol1, scol2, ...)"
     * 
     * @param table
     *            full name of the source table
     * @param name
     *            name of the constraint
     * @param str
     *            string encoding of the constraint
     * 
     */
    public KeyConfig(TableConfig table, String name, String str)
    {
        this();
        this.name = name;
        this.table = table;
        int columnsStart = str.indexOf('(');
        int columnsEnd = str.indexOf(')');
        String columnsStr = str.substring(columnsStart + 1, columnsEnd);
        String[] columns = columnsStr.trim().split(", ");
        Collections.addAll(this.columns, columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object)
    {
        return equalsXOptions(object) && super.equals(object);
    }

    /**
     * Equivalence test, ignoring options
     * 
     * @param object
     *            ConstraintConfig to compare to
     * @return true if equivalent, else false
     */
    public boolean equalsXOptions(Object object)
    {
        if (!(object instanceof KeyConfig))
        {
            return false;
        }
        KeyConfig other = (KeyConfig) object;
        return StringUtils.equals(name, other.name) && ListUtils.isEqualList(columns, other.columns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#hashCode()
     */
    @Override
    public int hashCode()
    {
        int code = name.hashCode();
        for (String column : columns)
        {
            code += column.hashCode();
        }
        return code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Drops this constraint
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void delete() throws ConfigurationException
    {
        throw new ConfigurationException("Changing primary keys is not supported. Please re-create the table.");
    }

    /**
     * Creates this constraint in the database
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void create() throws ConfigurationException
    {
        throw new ConfigurationException("Changing primary keys is not supported. Please re-create the table.");
    }

    /**
     * Updates this constraint
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void update() throws ConfigurationException
    {
        throw new ConfigurationException("Changing primary keys is not supported. Please re-create the table.");
    }

    public List<String> getColumns()
    {
        return columns;
    }

    public void setColumns(List<String> columns)
    {
        this.columns = columns;
    }

    public TableConfig getTable()
    {
        return table;
    }

    public void setTable(TableConfig table)
    {
        this.table = table;
    }

}
