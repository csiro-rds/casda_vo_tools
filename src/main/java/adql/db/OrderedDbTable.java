package adql.db;

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
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of the adql {@link DBTable} that preserves column order.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class OrderedDbTable extends DefaultDBTable
{

    /** The list of columns for preserving order */
    private List<DBColumn> columns = new ArrayList<>();

    /**
     * Constructor with args
     * 
     * @param dbCatName
     *            catalogue name in the database
     * @param adqlCatName
     *            catalogue name referred to in adql
     * @param dbSchemName
     *            schema name in the database
     * @param adqlSchemName
     *            schema name referred to in the adql
     * @param dbName
     *            the table name in the database
     * @param adqlName
     *            the table name referred to in the adql
     */
    public OrderedDbTable(String dbCatName, String adqlCatName, String dbSchemName, String adqlSchemName,
            String dbName, String adqlName)
    {
        super(dbCatName, adqlCatName, dbSchemName, adqlSchemName, dbName, adqlName);
    }

    @Override
    public void addColumn(DBColumn dbColumn)
    {
        columns.add(dbColumn);
        super.addColumn(dbColumn);
    }

    @Override
    public Iterator<DBColumn> iterator()
    {
        return columns.iterator();
    }

}
