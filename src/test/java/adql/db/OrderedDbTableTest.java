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


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests our implementation of the adql {@link DBTable} that preserves column order
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class OrderedDbTableTest
{

    /**
     * Tests that column order is preserved when the addAll function is used
     */
    @Test
    public void testColumnsPreserveOrderUsingAddAll()
    {
        String[] columnNames = new String[] { "one", "two", "three", "four", "five", "six" };
        OrderedDbTable orderedDbTable = new OrderedDbTable("dbCatName", "adqlCatName", "dbSchemName", "adqlSchemName",
                "dbName", "adqlName");
        List<DBColumn> columnsToAdd = new ArrayList<>();
        for (String columnName : columnNames)
        {
            columnsToAdd.add(new DefaultDBColumn(columnName, columnName, orderedDbTable));
        }
        orderedDbTable.addAllColumns(columnsToAdd);
        Iterator<DBColumn> columns = orderedDbTable.iterator();
        int count = 0;
        while (columns.hasNext())
        {
            DBColumn column = columns.next();
            assertEquals(columnNames[count], column.getDBName());
            count++;
        }
    }
    
    /**
     * Tests that the column order is preserved when the add function is used
     */
    @Test
    public void testColumnsPreserveOrderUsingAdd()
    {
        String[] columnNames = new String[] { "six", "five", "four", "three", "two", "one" };
        
        OrderedDbTable orderedDbTable = new OrderedDbTable("dbCatName", "adqlCatName", "dbSchemName", "adqlSchemName",
                "dbName", "adqlName");
        for (String columnName : columnNames)
        {
            orderedDbTable.addColumn(new DefaultDBColumn(columnName, columnName, orderedDbTable));
        }
        Iterator<DBColumn> columns = orderedDbTable.iterator();
        int count = 0;
        while (columns.hasNext())
        {
            DBColumn column = columns.next();
            assertEquals(columnNames[count], column.getDBName());
            count++;
        }
    }
}
