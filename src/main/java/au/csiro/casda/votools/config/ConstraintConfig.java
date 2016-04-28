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

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Integrity constraint configuration object. Only foreign keys are supported.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class ConstraintConfig extends Options
{
    /** optional description */
    public static final String DESCRIPTION = "description";
    /** optional utype */
    public static final String UTYPE = "utype";
    /** the constraint is deferrable */
    public static final String DEFERRABLE = "DEFERRABLE";
    /** the constraint is initially deferred */
    public static final String DEFERRED = "DEFERRED";

    /** constraint name */
    private String name;

    /** table configuration object */
    private TableConfig table;

    /** full name of source table */
    private String srcTable;

    /** full name of destination table */
    private String dstTable;

    /** name of source column */
    private ArrayList<String> srcColumns;

    /** name of destination column */
    private ArrayList<String> dstColumns;

    /** true if deferrable */
    private boolean deferrable;

    /** true if deferrable */
    private boolean deferred;

    /**
     * Parameterless constructor
     */
    public ConstraintConfig()
    {
        super();
        srcColumns = new ArrayList<String>();
        dstColumns = new ArrayList<String>();
    }

    /**
     * Creates ConstraintConfig object based on string encoding in form
     * "FOREIGN KEY (scol1, scol2, ...) REFERENCES dst_schema.dst_table(dcol1, dcol2, ...) [,deferrable[,deferred]]"
     * 
     * @param table
     *            full name of the source table
     * @param name
     *            name of the constraint
     * @param str
     *            string encoding of the constraint
     * 
     */
    public ConstraintConfig(TableConfig table, String name, String str)
    {
        this();
        this.name = name;
        this.table = table;
        int srcNamesStart = str.indexOf('(');
        int srcNamesEnd = str.indexOf(')');
        int dstNamesStart = str.indexOf('(', srcNamesEnd);
        int dstNamesEnd = str.indexOf(')', dstNamesStart);
        int dstTableStart = str.indexOf("REFERENCES") + "REFERENCES ".length();
        String srcNamesStr = str.substring(srcNamesStart + 1, srcNamesEnd);
        String dstNamesStr = str.substring(dstNamesStart + 1, dstNamesEnd);
        this.dstTable = str.substring(dstTableStart, dstNamesStart).trim();
        if (dstTable.indexOf('.') < 0)
        {
            dstTable = table.gtSchema() + "." + dstTable;
        }
        this.srcTable = table.gtFullDbTableName();
        String[] names = srcNamesStr.trim().split(", ");
        Collections.addAll(srcColumns, names);
        names = dstNamesStr.trim().split(", ");
        Collections.addAll(dstColumns, names);
        deferrable = str.indexOf(DEFERRABLE, dstNamesEnd) > 0;
        deferred = str.indexOf(DEFERRED, dstNamesEnd) > 0;
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
        if (!(object instanceof ConstraintConfig))
        {
            return false;
        }
        ConstraintConfig other = (ConstraintConfig) object;
        return StringUtils.equals(name, other.name) && StringUtils.equals(srcTable, other.srcTable)
                && StringUtils.equals(dstTable, other.dstTable) && ListUtils.isEqualList(srcColumns, other.srcColumns)
                && ListUtils.isEqualList(srcColumns, other.srcColumns) && deferrable == other.deferrable
                && deferred == other.deferred;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#hashCode()
     */
    @Override
    public int hashCode()
    {
        int code = name.hashCode() + srcTable.hashCode() + dstTable.hashCode();
        for (String column : srcColumns)
        {
            code += column.hashCode();
        }
        for (String column : dstColumns)
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

    public String getSrcTable()
    {
        return srcTable;
    }

    public void setSrcTable(String srcTable)
    {
        this.srcTable = srcTable;
    }

    public String getDstTable()
    {
        return dstTable;
    }

    public void setDstTable(String dstTable)
    {
        this.dstTable = dstTable;
    }

    public boolean isDeferrable()
    {
        return deferrable;
    }

    public void setDeferrable(boolean deferrable)
    {
        this.deferrable = deferrable;
    }

    public boolean isDeferred()
    {
        return deferred;
    }

    public void setDeferred(boolean deferred)
    {
        this.deferred = deferred;
    }

    /**
     * Drops this constraint
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void delete() throws ConfigurationException
    {
        this.getTable().gtConfig().initDao().dropConstraint(this);
    }

    /**
     * Creates this constraint in the database
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void create() throws ConfigurationException
    {
        this.getTable().gtConfig().initDao().createConstraint(this);
    }

    /**
     * Updates this constraint
     * 
     * @throws ConfigurationException
     *             if configuration does not allow this change or a DB problem occurred
     */
    public void update() throws ConfigurationException
    {
        this.getTable().gtConfig().initDao().dropConstraint(this);
        this.getTable().gtConfig().initDao().createConstraint(this);
    }

    public ArrayList<String> getSrcColumns()
    {
        return srcColumns;
    }

    public void setSrcColumns(ArrayList<String> srcColumns)
    {
        this.srcColumns = srcColumns;
    }

    public ArrayList<String> getDstColumns()
    {
        return dstColumns;
    }

    public void setDstColumns(ArrayList<String> dstColumns)
    {
        this.dstColumns = dstColumns;
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
