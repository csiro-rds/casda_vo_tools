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
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EmbeddedId;

import au.csiro.casda.votools.config.ColumnConfig;

/**
 * The persistent class for the columns database table. Since the move from JPA repositories to DAO, this is a part of
 * TAP metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations with
 * other TAP metadata objects.
 * 
 * Represents one of the database columns that is exposed through TAP. It allows us to specify how the TAP users will
 * refer to the column and can hide how it is stored in the database.
 * 
 * A TapColumn belongs to a TapTable. A TapColumn has a combined primary key of the table and column name (see
 * TapColumnPK), it also has a description, unit, ucd, utype, datatype, size, etc. It also has a list of
 * "fromKeyColumns" and "targetKeyColumns" which are references to the TapKeyColumn table. A TapKeyColumn represents a
 * key relationship between two columns, eg foreign key links. So the list of "fromKeyColumns" is the list of all the
 * key relationships where this column is the source (eg foreign key), and list of "targetKeyColumns" is the list of all
 * key relationships where this column is the destination (eg primary key).
 * 
 */
// @Entity
// @Table(name = "columns", schema = "casda")
public class TapColumn implements Serializable
{
    /** name of column_name column in the TAP metadata table */
    public static final String NAME = "column_name";
    /** name of db_column_name column in the TAP metadata table */
    public static final String DB_COLUMN_NAME = "db_column_name";
    /** name of table_name column in the TAP metadata table */
    public static final String TABLE = "table_name";
    /** name of column_order column in the TAP metadata table */
    public static final String ORDER = "column_order";
    /** name of description column in the TAP metadata table */
    public static final String DESCRIPTION = "description";
    /** name of unit column in the TAP metadata table */
    public static final String UNIT = "unit";
    /** name of ucd column in the TAP metadata table */
    public static final String UCD = "ucd";
    /** name of utype column in the TAP metadata table */
    public static final String UTYPE = "utype";
    /** name of datatype column in the TAP metadata table */
    public static final String DATATYPE = "datatype";
    /** name of size column in the TAP metadata table */
    public static final String SIZE = "size";
    /** name of principal column in the TAP metadata table */
    public static final String PRINCIPAL = "principal";
    /** name of indexed column in the TAP metadata table */
    public static final String INDEXED = "indexed";
    /** name of std column in the TAP metadata table */
    public static final String STD = "std";
    /** name of scs_verboisity column in the TAP metadata table */
    public static final String SCS_VERBOSITY = "scs_verbosity";
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private TapColumnPK id;

    private int columnOrder;

    private String datatype;

    private String description;
    
    private String dbColumnName;

    private Integer indexed;

    private Integer principal;

    private Integer size;

    private Integer std;

    private String ucd;

    private String unit;

    private String utype;

    private Integer scsVerbosity;

    // bi-directional many-to-one association to TapTable
    // @ManyToOne
    // @JoinColumn(name = "table_name", insertable = false, updatable = false)
    private TapTable table;

    // bi-directional many-to-one association to TapKeyColumn
    // @OneToMany(mappedBy = "fromColumn")
    private List<TapKeyColumn> fromKeyColumns;

    // bi-directional many-to-one association to TapKeyColumn
    // @OneToMany(mappedBy = "targetColumn")
    private List<TapKeyColumn> targetKeyColumns;

    /**
     * Empty constructor
     */
    public TapColumn()
    {
        fromKeyColumns = new LinkedList<TapKeyColumn>();
        targetKeyColumns = new LinkedList<TapKeyColumn>();
    }

    /**
     * Constructor based on ColumnConfig
     * 
     * @param key
     *            column Id
     * @param table
     *            table the column belongs to
     * @param cfgColumn
     *            column configuration object
     */
    public TapColumn(TapColumnPK key, TapTable table, ColumnConfig cfgColumn)
    {
        this();
        this.id = key;
        this.table = table;
        update(cfgColumn);
    }

    /**
     * Update this TapColumn with information from CfgColumn
     * 
     * @param cfgColumn
     *            CfgColumn input
     */
    public void update(ColumnConfig cfgColumn)
    {
        this.datatype = cfgColumn.getType();
        this.description = cfgColumn.get(ColumnConfig.DESCRIPTION);
        this.dbColumnName = cfgColumn.get(ColumnConfig.NAME);
        this.indexed = cfgColumn.getInt(ColumnConfig.INDEXED);
        this.principal = cfgColumn.getInt(ColumnConfig.PRINCIPAL);
        this.size = cfgColumn.getInt(ColumnConfig.SIZE);
        this.std = cfgColumn.getInt(ColumnConfig.STD);
        this.ucd = cfgColumn.get(ColumnConfig.UCD);
        this.unit = cfgColumn.get(ColumnConfig.UNIT);
        this.utype = cfgColumn.get(ColumnConfig.UTYPE);
        this.scsVerbosity = cfgColumn.getInt(ColumnConfig.SCS_VERBOSITY);
        this.columnOrder = cfgColumn.getInt(ColumnConfig.ORDER);
    }

    public TapColumnPK getId()
    {
        return this.id;
    }

    public void setId(TapColumnPK id)
    {
        this.id = id;
    }

    public int getColumnOrder()
    {
        return columnOrder;
    }

    public void setColumnOrder(int columnOrder)
    {
        this.columnOrder = columnOrder;
    }

    public String getDatatype()
    {
        return this.datatype;
    }

    public void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getIndexed()
    {
        return this.indexed;
    }

    public void setIndexed(Integer indexed)
    {
        this.indexed = indexed;
    }

    public Integer getPrincipal()
    {
        return this.principal;
    }

    public void setPrincipal(Integer principal)
    {
        this.principal = principal;
    }

    public Integer getSize()
    {
        return this.size;
    }

    public void setSize(Integer size)
    {
        this.size = size;
    }

    public Integer getStd()
    {
        return this.std;
    }

    public void setStd(Integer std)
    {
        this.std = std;
    }

    public String getUcd()
    {
        return this.ucd;
    }

    public void setUcd(String ucd)
    {
        this.ucd = ucd;
    }

    public String getUnit()
    {
        return this.unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUtype()
    {
        return this.utype;
    }

    public void setUtype(String utype)
    {
        this.utype = utype;
    }

    public Integer getScsVerbosity()
    {
        return scsVerbosity;
    }

    public void setScsVerbosity(Integer scsVerbosity)
    {
        this.scsVerbosity = scsVerbosity;
    }

    public TapTable getTable()
    {
        return this.table;
    }

    public void setTable(TapTable table)
    {
        this.table = table;
    }

    /**
     * Gets the list of "fromKeyColumns" which represents the key relationships with another table, where this column is
     * the source (eg foreign key)
     * 
     * @return the list of fromKeyColumns
     */
    public List<TapKeyColumn> getFromKeyColumns()
    {
        return this.fromKeyColumns;
    }

    /**
     * Sets the list of "fromKeyColumns" which represents the key relationships with another table, where this column is
     * the source (eg foreign key)
     * 
     * @param fromKeyColumns
     *            the list of fromKeyColumns
     */
    public void setFromKeyColumns(List<TapKeyColumn> fromKeyColumns)
    {
        this.fromKeyColumns = fromKeyColumns;
    }

    /**
     * Adds an entry to the "fromKeyColumns" list. This represents a key relationships with another table, where this
     * column is the source (eg foreign key)
     * 
     * @param fromKeyColumn
     *            the fromKeyColumn to add to the list
     * @return the fromKeyColumn added to the list
     */
    public TapKeyColumn addFromColumn(TapKeyColumn fromKeyColumn)
    {
        getFromKeyColumns().add(fromKeyColumn);
        fromKeyColumn.setFromColumn(this);

        return fromKeyColumn;
    }

    /**
     * Removes an entry to the "fromKeyColumns" list.
     * 
     * @param fromKeyColumn
     *            the fromKeyColumn to remove from the list
     * @return the fromKeyColumn that was removed from the list
     */
    public TapKeyColumn removeFromColumn(TapKeyColumn fromKeyColumn)
    {
        getFromKeyColumns().remove(fromKeyColumn);
        fromKeyColumn.setFromColumn(null);

        return fromKeyColumn;
    }

    /**
     * Gets the list of "targetKeyColumns" which represents the key relationships with another table, where this column
     * is the destination (eg primary key)
     * 
     * @return the list of targetKeyColumns
     */
    public List<TapKeyColumn> getTargetKeyColumns()
    {
        return this.targetKeyColumns;
    }

    /**
     * Sets the list of "targetKeyColumns" which represents the key relationships with another table, where this column
     * is the destination (eg primary key)
     * 
     * @param targetKeyColumns
     *            list of targetKeyColumns
     */
    public void setTargetKeyColumns(List<TapKeyColumn> targetKeyColumns)
    {
        this.targetKeyColumns = targetKeyColumns;
    }

	public String getDbColumnName() 
	{
		return dbColumnName;
	}

	public void setDbColumnName(String dbColumnName) 
	{
		this.dbColumnName = dbColumnName;
	}

	/**
     * Adds an entry to the "targetKeyColumns" list. This represents a key relationships with another table, where this
     * column is the destination (eg primary key)
     * 
     * @param targetKeyColumn
     *            the targetKeyColumn to add to the list
     * @return the targetKeyColumn added to the list
     */
    public TapKeyColumn addTargetColumn(TapKeyColumn targetKeyColumn)
    {
        getTargetKeyColumns().add(targetKeyColumn);
        targetKeyColumn.setTargetColumn(this);

        return targetKeyColumn;
    }

    /**
     * Removes an entry to the "targetKeyColumns" list.
     * 
     * @param targetKeyColumn
     *            the targetKeyColumn to remove from the list
     * @return the targetKeyColumn that was removed from the list
     */
    public TapKeyColumn removeTargetKeyColumn(TapKeyColumn targetKeyColumn)
    {
        getTargetKeyColumns().remove(targetKeyColumn);
        targetKeyColumn.setTargetColumn(null);

        return targetKeyColumn;
    }

    @Override
    public String toString()
    {
        return String.format("[TapColumn - id: (%s), type: %s, desc: %s]\n", id, datatype, description);
    }

}