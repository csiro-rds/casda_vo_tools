package au.csiro.casda.votools.jpa;

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


import java.io.Serializable;

/**
 * The primary key class for the columns database table. Since the move from JPA repositories to DAO, this is a part of
 * TAP metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations with
 * other TAP metadata objects.
 * 
 * The TapColumnPK is the primary key for the TapColumn table, which is a combination of the table name and column name.
 * 
 */
// @Embeddable
public class TapColumnPK implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Override
    public String toString()
    {
        return String.format("[TapColPK - tableName: %s, colName: %s]", tableName, columnName);
    }

    // @Column(name = "table_name")
    private String tableName;

    // @Column(name = "column_name")
    private String columnName;

    /**
     * Empty constructor
     */
    public TapColumnPK()
    {
    }

    /**
     * Constructor with args
     * 
     * @param tableName
     *            the table name
     * @param columnName
     *            the column name
     */
    public TapColumnPK(String tableName, String columnName)
    {
        this();
        this.setColumnName(columnName);
        this.setTableName(tableName);
    }

    public String getTableName()
    {
        return this.tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getColumnName()
    {
        return this.columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof TapColumnPK))
        {
            return false;
        }
        TapColumnPK castOther = (TapColumnPK) other;
        return this.tableName.equals(castOther.tableName) && this.columnName.equals(castOther.columnName);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        final int initialHashValue = 17;
        int hash = initialHashValue;
        hash = hash * prime + this.tableName.hashCode();
        hash = hash * prime + this.columnName.hashCode();

        return hash;
    }
}