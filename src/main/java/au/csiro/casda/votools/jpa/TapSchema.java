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

/**
 * The persistent class for the schemas database table. Since the move from JPA repositories to DAO, this is a part of
 * TAP metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations with
 * other TAP metadata objects.
 * 
 * Represents the CASDA database schema exposed through TAP, and has a name, description, utype and a reference to all
 * of the tables that are available.
 * 
 */
//@Entity
//@Table(name = "schemas", schema = "casda")
public class TapSchema implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** name of schema_name column in the TAP metadata table */
    public static final String NAME = "schema_name" ;
    /** name of description column in the TAP metadata table */
    public static final String DESCRIPTION = "description" ;
    /** name of utype column in the TAP metadata table */
    public static final String UTYPE = "utype" ;
    /** name of schema order column in the TAP metadata table */
    public static final String SCHEMA_ORDER = "schema_order" ;

    @Override
    public String toString()
    {
        return String.format("[TapSchema - name:%s,  desc: %s]\n", schemaName, description);
    }

    //@Id
    //@Column(name = "schema_name")
    private String schemaName;

    private String description;

    private String utype;

    private int schemaOrder;

    // bi-directional many-to-one association to TapTable
    //@OneToMany(mappedBy = "schema")
    private List<TapTable> tables;

    /**
     * Constructor
     * 
     */
    public TapSchema()
    {
        tables = new LinkedList<TapTable>() ;
    }

    /**
     * Constructor
     * 
     * @param schemaName
     *            schema name
     */
    public TapSchema(String schemaName)
    {
        this.schemaName = schemaName;
        tables = new LinkedList<TapTable>() ;
    }

    public String getSchemaName()
    {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getUtype()
    {
        return this.utype;
    }

    public void setUtype(String utype)
    {
        this.utype = utype;
    }

    public int getSchemaOrder()
    {
        return schemaOrder;
    }

    public void setSchemaOrder(int schemaOrder)
    {
        this.schemaOrder = schemaOrder;
    }

    /**
     * Gets the list of tables that are exposed through TAP
     * 
     * @return the list of tables.
     */
    public List<TapTable> getTables()
    {
        return this.tables;
    }

    /**
     * Sets the list of tables that are exposed through TAP
     * 
     * @param tables
     *            the list of tables.
     */
    public void setTables(List<TapTable> tables)
    {
        this.tables = tables;
    }

    /**
     * Adds details of a database table to the list of tables that are exposed through TAP
     * 
     * @param table
     *            the TAP table details
     * @return the table that was added
     */
    public TapTable addTable(TapTable table)
    {
        getTables().add(table);
        table.setSchema(this);

        return table;
    }

    /**
     * Removes details of a given database table from the list.
     * 
     * @param table
     *            the table to remove
     * @return the table that was removed from the list
     */
    public TapTable removeTable(TapTable table)
    {
        getTables().remove(table);
        table.setSchema(null);

        return table;
    }

}