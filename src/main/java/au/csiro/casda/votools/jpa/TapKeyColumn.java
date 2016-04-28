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
 * The persistent class for the key_columns database table. Since the move from JPA repositories to DAO, this is a part of
 * TAP metadata objects cache. All JPA related annotations have been disabled, but left in place to explain relations with
 * other TAP metadata objects.
 * 
 * A TapKeyColumn represents a key relationship between two columns, eg foreign key links. The "fromColumn" is the
 * source of the relationship (eg foreign key column), and the "targetColumn" is the destination of the relationship (eg
 * primary key column).
 * 
 */
//@Entity
//@Table(name = "key_columns", schema = "casda")
public class TapKeyColumn implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** name of id column in the TAP metadata table */
    public static final String ID = "id" ;
    /** name of key_id column in the TAP metadata table */
    public static final String KEY_ID = "key_id" ;
    /** name of from_column column in the TAP metadata table */
    public static final String FROM_COLUMN = "from_column" ;
    /** name of target_column column in the TAP metadata table */
    public static final String TARGET_COLUMN = "target_column" ;
    /** name of from_table column in the TAP metadata table */
    public static final String FROM_TABLE = "from_table" ;
    /** name of target_table column in the TAP metadata table */
    public static final String TARGET_TABLE = "target_table" ;

    // @Id
    private Integer id;

    // bi-directional many-to-one association to TapColumn
    //@ManyToOne
    //@JoinColumns({ @JoinColumn(name = "from_column", referencedColumnName = "column_name"),
    //        @JoinColumn(name = "from_table", referencedColumnName = "table_name") })
    private TapColumn fromColumn;

    // bi-directional many-to-one association to TapColumn
    //@ManyToOne
    //@JoinColumns({ @JoinColumn(name = "target_column", referencedColumnName = "column_name"),
    //        @JoinColumn(name = "target_table", referencedColumnName = "table_name") })
    private TapColumn targetColumn;

    // bi-directional many-to-one association to TapKey
    //@ManyToOne
    //@JoinColumn(name = "key_id")
    private TapKey key;

    /**
     * Empty constructor
     */
    public TapKeyColumn()
    {
    }

    public Integer getId()
    {
        return this.id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public TapColumn getFromColumn()
    {
        return this.fromColumn;
    }

    public void setFromColumn(TapColumn column1)
    {
        this.fromColumn = column1;
    }

    public TapColumn getTargetColumn()
    {
        return this.targetColumn;
    }

    public void setTargetColumn(TapColumn column2)
    {
        this.targetColumn = column2;
    }

    public TapKey getKey()
    {
        return this.key;
    }

    public void setKey(TapKey key)
    {
        this.key = key;
    }

}