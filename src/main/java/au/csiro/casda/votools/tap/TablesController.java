package au.csiro.casda.votools.tap;

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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.jaxb.vodataservice.FKColumn;
import au.csiro.casda.votools.jaxb.vodataservice.ForeignKey;
import au.csiro.casda.votools.jaxb.vodataservice.TAPType;
import au.csiro.casda.votools.jaxb.vodataservice.Table;
import au.csiro.casda.votools.jaxb.vodataservice.TableParam;
import au.csiro.casda.votools.jaxb.vodataservice.TableSchema;
import au.csiro.casda.votools.jaxb.vositables.Tableset;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;

/**
 * RESTful web service controller. Metadata query to allow a client to discover the names of tables and columns to be
 * used in data queries
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RestController
@RequestMapping("/tap/tables")
public class TablesController
{

    private static Logger logger = LoggerFactory.getLogger(TablesController.class);

    private final VoTableRepositoryService tableService;

    /**
     * Constructor
     * 
     * @param tableService
     *            the vo table repository service
     */
    @Autowired
    public TablesController(VoTableRepositoryService tableService)
    {
        this.tableService = tableService;
    }

    /**
     * Gets the information about the tables we are exposing through TAP.
     * 
     * @return Tableset the tables exposed through TAP
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public @ResponseBody Tableset getTables()
    {
        logger.info("Hit the controller for the '/tap/tables' url mapping - servicing request");
        checkReady();
        Tableset ts = new Tableset();
        List<TapSchema> schemas = tableService.getSchemas();
        for (TapSchema tSchema : schemas)
        {
            ts.getSchema().add(getSchema(tSchema));
        }
        return ts;
    }

    private TableParam getColumn(TapColumn tCol)
    {
        TableParam col = new TableParam();
        col.setName(tCol.getId().getColumnName());
        TAPType tdt = new TAPType();
        if (tCol.getSize() != null)
        {
            tdt.setSize(BigInteger.valueOf(tCol.getSize().longValue()));
        }
        tdt.setValue(tCol.getDatatype());
        col.setDataType(tdt);
        col.setUcd(tCol.getUcd());
        col.setDescription(tCol.getDescription());
        col.setUnit(tCol.getUnit());
        col.setStd(tCol.getStd().intValue() == 1);
        col.setUtype(tCol.getUtype());
        // IsIndexed <flag>indexed</flag>
        if (tCol.getIndexed().intValue() == 1)
        {
            col.getFlag().add("indexed");
        }
        return col;
    }

    private Table getTable(TapTable tTable)
    {
        Table table = new Table();
        table.setName(tTable.getTableName());
        table.setDescription(tTable.getDescription());
        table.setType(tTable.getTableType());
        table.setUtype(tTable.getUtype());
        List<TapColumn> columns = tableService.getTableColumns(tTable.getTableName());
        for (TapColumn col : columns)
        {
            table.getColumn().add(getColumn(col));
        }
        return table;
    }

    private TableSchema getSchema(TapSchema tSchema)
    {
        TableSchema schemas = new TableSchema();
        schemas.setName(tSchema.getSchemaName());
        schemas.setUtype(tSchema.getUtype());
        schemas.setDescription(tSchema.getDescription());
        List<TapTable> tapTables = tableService.getSchemaTables(tSchema.getSchemaName());
        for (TapTable tapTable : tapTables)
        {
            Table table = this.getTable(tapTable);
            schemas.getTable().add(table);
            table.getForeignKey().addAll(getForeignKeys(tapTable));
        }
        return schemas;
    }

    private List<ForeignKey> getForeignKeys(TapTable tapTable)
    {
        List<TapKey> foreignKeys = tableService.getFromKeys(tapTable.getTableName());
        List<ForeignKey> voTableForeignKeys = new ArrayList<>();
        for (TapKey foreignKey : foreignKeys)
        {
            ForeignKey voTableForeignKey = new ForeignKey();
            voTableForeignKey.setDescription(foreignKey.getDescription());
            voTableForeignKey.setTargetTable(foreignKey.getTargetTable().getTableName());
            voTableForeignKey.setUtype(foreignKey.getUtype());
            List<TapKeyColumn> keyColumns = foreignKey.getKeyColumns();
            for (TapKeyColumn keyColumn : keyColumns)
            {
                FKColumn fkColumn = new FKColumn();
                fkColumn.setFromColumn(keyColumn.getFromColumn().getId().getColumnName());
                fkColumn.setTargetColumn(keyColumn.getTargetColumn().getId().getColumnName());
                voTableForeignKey.getFkColumn().add(fkColumn);
            }
            voTableForeignKeys.add(voTableForeignKey);
        }
        return voTableForeignKeys;
    }
    
    /**
     * Checks is this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    private void checkReady()
    {
        try
        {
            if (tableService == null || !tableService.isReady())
            {
                throw new ConfigurationException("TableController is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }


}