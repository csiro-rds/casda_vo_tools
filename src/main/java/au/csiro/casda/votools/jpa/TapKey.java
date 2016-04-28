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
 * The persistent class for the keys database table. Since the move from JPA repositories to DAO, this is a part of TAP
 * metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations with
 * other TAP metadata objects.
 * 
 * A TapKey represents a key link between two tables. The "fromTable" is the source of the link (eg table that has the
 * foreign key) and the "targetTable" is the destination of the link (eg table that has the primary key). The list of
 * "keyColumns" is the list of TapKeyColumns that refer to this TapKey, and a TapKeyColumn contains details of the
 * columns that are involved in the key relationship.
 * 
 * 
 */
// @Entity
// @Table(name = "keys", schema = "casda")
public class TapKey implements Serializable
{
    private static final long serialVersionUID = 1L;
    /** name of key_id column in the TAP metadata table */
    public static final String ID = "key_id";
    /** name of from_table column in the TAP metadata table */
    public static final String FROM = "from_table";
    /** name of target column in the TAP metadata table */
    public static final String TARGET = "target_table";
    /** name of description column in the TAP metadata table */
    public static final String DESCRIPTION = "description";
    /** name of utype column in the TAP metadata table */
    public static final String UTYPE = "utype";

    // @Id
    // @Column(name = "key_id")
    private String keyId;

    private String description;

    private String utype;

    // bi-directional many-to-one association to TapKeyColumn
    // @OneToMany(mappedBy = "key")
    private List<TapKeyColumn> keyColumns;

    // bi-directional many-to-one association to TapTable
    // @ManyToOne
    // @JoinColumn(name = "from_table")
    private TapTable fromTable;

    // bi-directional many-to-one association to TapTable
    // @ManyToOne
    // @JoinColumn(name = "target_table")
    private TapTable targetTable;

    /**
     * Empty constructor
     */
    public TapKey()
    {
        keyColumns = new LinkedList<TapKeyColumn>() ;
    }

    public String getKeyId()
    {
        return this.keyId;
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
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

    public List<TapKeyColumn> getKeyColumns()
    {
        return this.keyColumns;
    }

    public void setKeyColumns(List<TapKeyColumn> keyColumns)
    {
        this.keyColumns = keyColumns;
    }

    /**
     * Adds details of a key columns that refers to this key. A key column contains details of the columns that are
     * involved in the key relationship.
     * 
     * @param keyColumn
     *            the key column details
     * @return the key column that was added
     */
    public TapKeyColumn addKeyColumn(TapKeyColumn keyColumn)
    {
        getKeyColumns().add(keyColumn);
        keyColumn.setKey(this);

        return keyColumn;
    }

    /**
     * Remove one of the key columns from the list.
     * 
     * @param keyColumn
     *            the key column to remove
     * @return the key column that was removed
     */
    public TapKeyColumn removeKeyColumn(TapKeyColumn keyColumn)
    {
        getKeyColumns().remove(keyColumn);
        keyColumn.setKey(null);

        return keyColumn;
    }

    public TapTable getFromTable()
    {
        return this.fromTable;
    }

    public void setFromTable(TapTable fromTable)
    {
        this.fromTable = fromTable;
    }

    public TapTable getTargetTable()
    {
        return this.targetTable;
    }

    public void setTargetTable(TapTable targetTable)
    {
        this.targetTable = targetTable;
    }

}