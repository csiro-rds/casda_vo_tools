package au.csiro.casda.votools;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;

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



/**
 * A set of utilities used when testing VO Tools functions. 
 * 
 * Copyright 2015, CSIRO Australia 
 * All rights reserved.
 */
public class TestUtils
{

    /**
     * Create a new TapTable instance.
     * 
     * @param dbSchemaName
     *            The database schema name.
     * @param dbTableName
     *            The database table name.
     * @param tapSchema
     *            The name of the schema advertised in TAP.
     * @param tableName
     *            The name of the table as advertised in tap. Will normally with the schema name. e.g. ivoa.obscore
     * @param scsEnabled
     *            Flag to indicate if the table should have cone search provided.
     * @param releaseRequired
     *            flag to indicate if the table should restrict access to unreleased data
     * @return The TapTable instance.
     */
    public static TapTable createTapTable(String dbSchemaName, String dbTableName, TapSchema tapSchema, String tableName,
            boolean scsEnabled, boolean releaseRequired)
    {
        TapTable table = new TapTable();
        table.setDbSchemaName(dbSchemaName);
        table.setDbTableName(dbTableName);
        table.setSchema(tapSchema);
        table.setTableName(tableName);
        table.setScsEnabled(scsEnabled);
        table.setReleaseRequired(releaseRequired);
        tapSchema.addTable(table);
        return table;
    }

    /**
     * Create a new TapColumn instance.
     * 
     * @param table The table this column should be part of.
     * @param columnName The name of the column.
     * @param dataType The IVOA data type of the column.
     * @param size The length of the column.
     * @param ucd The Unified Content Descriptor.
     * @param scsVerbosity The verbosity level, normal values are 1, 2, 3 or null. 
     * @param columnOrder the display order for the column, random if set to 0
     * @return The TapColumn instance. 
     */
    public static TapColumn createTapColumn(TapTable table, String columnName, String dataType, int size, String ucd,
            Integer scsVerbosity, int columnOrder)
    {
        TapColumn tapColumn = new TapColumn();
        tapColumn.setTable(table);
        tapColumn.setId(new TapColumnPK(table.getTableName(), columnName));
        tapColumn.setDbColumnName(columnName);
        tapColumn.setDatatype(dataType);
        tapColumn.setSize(size);
        tapColumn.setUcd(ucd);
        tapColumn.setScsVerbosity(scsVerbosity);
        tapColumn.setColumnOrder(columnOrder);
        table.addColumn(tapColumn);
        return tapColumn;
    }
    
    /**
     * Create a new TapColumn instance.
     * 
     * @param table The table this column should be part of.
     * @param columnName The name of the column.
     * @param dataType The IVOA data type of the column.
     * @param size The length of the column.
     * @param ucd The Unified Content Descriptor.
     * @param scsVerbosity The verbosity level, normal values are 1, 2, 3 or null. 
     * @param columnOrder the display order for the column, random if set to 0
     * @return The TapColumn instance. 
     */
    public static TapColumn createTapColumnWithDifferentDbName(TapTable table, String columnName, String dataType,
            int size, String ucd, Integer scsVerbosity, int columnOrder)
    {
        TapColumn tapColumn = new TapColumn();
        tapColumn.setTable(table);
        tapColumn.setId(new TapColumnPK(table.getTableName(), columnName));
        tapColumn.setDbColumnName("db" + columnName);
        tapColumn.setDatatype(dataType);
        tapColumn.setSize(size);
        tapColumn.setUcd(ucd);
        tapColumn.setScsVerbosity(scsVerbosity);
        tapColumn.setColumnOrder(columnOrder);
        return tapColumn;
    }
}
