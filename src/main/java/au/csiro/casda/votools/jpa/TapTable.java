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

import org.joda.time.DateTime;

import au.csiro.casda.votools.config.TableConfig;

/**
 * The persistent class for the tables database table. Since the move from JPA repositories to DAO, this is a part of
 * TAP metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations
 * with other TAP metadata objects.
 * 
 * Represents one of the database tables that is exposed through TAP. It allows us to specify how the TAP users will
 * refer to the table and can hide how it is stored in the database.
 * 
 * A TapTable belongs to a TapSchema. A TapTable has a name, description, type, utype which are the names that the TAP
 * users will be aware of, and references to the table and schema names that are in the database. It also has a list of
 * "fromTableKeys" and "targetTableKeys" which are references to the TapKey table. A TapKey represents a link between
 * two tables (eg a foreign key relationship), so the list of "fromTableKeys" is the list of all the relationships
 * between tables where this table is the source of the relationship (eg contains a column with the foreign key) and the
 * list of "targetTableKeys" is the list of all relationships where the this table is the destination for the
 * relationship (eg contains the primary key).
 */
// @Entity
// @Table(name = "tables", schema = "casda")
public class TapTable implements Serializable
{
    /** name of ucd column in the TAP metadata table */
    private static final long serialVersionUID = 1L;
    /** name of table_name column in the TAP metadata table */
    public static final String NAME = "table_name";
    /** name of schema_name column in the TAP metadata table */
    public static final String SCHEMA_NAME = "schema_name";
    /** name of table_type column in the TAP metadata table */
    public static final String TYPE = "table_type";
    /** name of description column in the TAP metadata table */
    public static final String DESCRIPTION = "description";    
    /** name of long description column in the TAP metadata table */
    public static final String DESCRIPTION_LONG = "description_long";
    /** name of utype column in the TAP metadata table */
    public static final String UTYPE = "UTYPE";
    /** name of db_table_name column in the TAP metadata table */
    public static final String DB_NAME = "db_table_name";
    /** name of db_schema column in the TAP metadata table */
    public static final String DB_SCHEMA = "db_schema_name";
    /** name of scs_enabled column in the TAP metadata table */
    public static final String SCS_ENABLED = "scs_enabled";
    /** name of parmas column in the TAP metadata table */
    public static final String PARAMS = "params";
    /**
     * Name of the release_required column in the TAP metadata table. If the value in this field is true, the data in
     * the table should restrict access to unreleased data by unauthorised users.
     */
    public static final String RELEASE_REQUIRED = "release_required";
    
    /**
     * Name of the released_date column in the TAP metadata table. This is the date on which the table contents will 
     * become public.
     */
    public static final String RELEASED_DATE = "released_date";

    // @Id
    // @Column(name = "table_name")
    private String tableName;

    private String description;
    
    private String descriptionLong;

    // @Column(name = "table_type")
    private String tableType;

    private String utype;

    // bi-directional many-to-one association to TapColumn
    // @OneToMany(mappedBy = "table")
    private List<TapColumn> columns;

    // bi-directional many-to-one association to TapKey
    // @OneToMany(mappedBy = "fromTable")
    private List<TapKey> fromTableKeys;

    // bi-directional many-to-one association to TapKey
    // @OneToMany(mappedBy = "targetTable")
    private List<TapKey> targetTableKeys;

    // bi-directional many-to-one association to TapSchema
    // @ManyToOne
    // @JoinColumn(name = "schema_name")
    private TapSchema schema;

    // @Column(name = "db_schema_name")
    private String dbSchemaName;

    // @Column(name = "db_table_name")
    private String dbTableName;

    /** Should this table have simple cone search enabled? */
    // @Column(name = "scs_enabled")
    private Boolean scsEnabled;

    /** Should access to this table be restricted so that only authorised users can view released data? */
    private Boolean releaseRequired;

    /** The date on which the contents of the table become public. */
    private DateTime releaseDate;
    
    private String params;

    /**
     * Empty constructor
     */
    public TapTable()
    {
        columns = new LinkedList<TapColumn>();
        fromTableKeys = new LinkedList<TapKey>();
        targetTableKeys = new LinkedList<TapKey>();
    }

    /**
     * Name based constructor
     * 
     * @param name
     *            table name
     */
    public TapTable(String name)
    {
        this();
        this.tableName = name;
    }

    /**
     * Creates a new object based on the table configuration object
     * 
     * @param cfgTable
     *            table configuration
     */
    public TapTable(TableConfig cfgTable)
    {
        this(cfgTable.gtFullTapTableName());
        update(cfgTable);
    }

    /**
     * Updates this object based on the table configuration object
     * 
     * @param cfgTable
     *            table configuration
     */
    public void update(TableConfig cfgTable)
    {
        description = cfgTable.get(TableConfig.DESCRIPTION);
        descriptionLong = cfgTable.get(TableConfig.DESCRIPTION_LONG);
        params = cfgTable.get(TableConfig.PARAMS);
        tableType = cfgTable.get(TableConfig.TYPE);
        utype = cfgTable.get(TableConfig.UTYPE);
        dbSchemaName = cfgTable.gtSchema();
        dbTableName = cfgTable.gtShortName();
        scsEnabled = cfgTable.getBoolean(TableConfig.SCS_ENABLED);
        tableName = cfgTable.gtFullTapTableName();
    }

    public String getTableName()
    {
        return this.tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTableType()
    {
        return this.tableType;
    }

    public void setTableType(String tableType)
    {
        this.tableType = tableType;
    }

    public String getUtype()
    {
        return this.utype;
    }

    public void setUtype(String utype)
    {
        this.utype = utype;
    }

    public List<TapColumn> getColumns()
    {
        return this.columns;
    }

    public void setColumns(List<TapColumn> columns)
    {
        this.columns = columns;
    }

    /**
     * Adds a column to the table definition exposed through TAP
     * 
     * @param column
     *            the column in the table to add to the list of exposed columns
     * @return the column that was added
     */
    public TapColumn addColumn(TapColumn column)
    {
        getColumns().add(column);
        column.setTable(this);

        return column;
    }

    /**
     * Remove a column from the list of columns exposed for this table.
     * 
     * @param column
     *            the column to remove from the list
     * @return the column that was removed.
     */
    public TapColumn removeColumn(TapColumn column)
    {
        getColumns().remove(column);
        column.setTable(null);

        return column;
    }

    public List<TapKey> getFromTableKeys()
    {
        return this.fromTableKeys;
    }

    public void setFromTableKeys(List<TapKey> keys1)
    {
        this.fromTableKeys = keys1;
    }

    /**
     * Adds a TapKey (represents a foreign key link between two tables) where this table is the source of the
     * relationship (eg contains a column with the foreign key).
     * 
     * @param fromTableKey
     *            adds a TapKey to the list of TapKey that refers to this table as a "fromTable"
     * @return the TapKey that was added to the list
     */
    public TapKey addFromTableKey(TapKey fromTableKey)
    {
        getFromTableKeys().add(fromTableKey);
        fromTableKey.setFromTable(this);

        return fromTableKey;
    }

    /**
     * Removes a TapKey from the fromTables list
     * 
     * @param key
     *            the key to remove
     * @return the key that was removed
     */
    public TapKey removeFromTableKey(TapKey key)
    {
        getFromTableKeys().remove(key);
        key.setFromTable(null);

        return key;
    }

    public List<TapKey> getTargetTableKeys()
    {
        return this.targetTableKeys;
    }

    public void setTargetTableKeys(List<TapKey> targetTableKeys)
    {
        this.targetTableKeys = targetTableKeys;
    }

    /**
     * Adds a TapKey (represents a foreign key link between two tables) where this table is the destination of the
     * relationship (eg contains the primary key).
     * 
     * @param targetTableKey
     *            adds a TapKey to the list of TapKey that refers to this table as a "targetTable"
     * @return the TapKey that was added to the list
     */
    public TapKey addTargetTableKey(TapKey targetTableKey)
    {
        getTargetTableKeys().add(targetTableKey);
        targetTableKey.setTargetTable(this);

        return targetTableKey;
    }

    /**
     * Removes a TapKey from the targetTables list
     * 
     * @param targetTableKey
     *            the key to remove
     * @return the key that was removed
     */
    public TapKey removeTargetTableKey(TapKey targetTableKey)
    {
        getTargetTableKeys().remove(targetTableKey);
        targetTableKey.setTargetTable(null);

        return targetTableKey;
    }

    public TapSchema getSchema()
    {
        return this.schema;
    }

    public void setSchema(TapSchema schema)
    {
        this.schema = schema;
    }

    public String getDbSchemaName()
    {
        return dbSchemaName;
    }

    public void setDbSchemaName(String dbSchemaName)
    {
        this.dbSchemaName = dbSchemaName;
    }

    public String getDbTableName()
    {
        return dbTableName;
    }

    public void setDbTableName(String dbTableName)
    {
        this.dbTableName = dbTableName;
    }

    public Boolean getScsEnabled()
    {
        return scsEnabled;
    }

    public void setScsEnabled(Boolean scsEnabled)
    {
        this.scsEnabled = scsEnabled;
    }

    public Boolean getReleaseRequired()
    {
        return releaseRequired;
    }

    public void setReleaseRequired(Boolean releaseRequired)
    {
        this.releaseRequired = releaseRequired;
    }

    public DateTime getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(DateTime releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public String getParams()
    {
        return params;
    }

    public void setParams(String params)
    {
        this.params = params;
    }

    public String getDescriptionLong()
    {
        return descriptionLong;
    }

    public void setDescriptionLong(String descriptionLong)
    {
        this.descriptionLong = descriptionLong;
    }

    @Override
    public String toString()
    {
        return String.format("[TAPTable - id: %s,  name: %s, type: %s, desc: %s]\n", "THE NAME", tableName, tableType,
                description);
    }

}