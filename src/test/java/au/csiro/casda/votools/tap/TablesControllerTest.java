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


import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.BaseTest;
import au.csiro.casda.votools.jaxb.vodataservice.TableSet;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapColumnPK;
import au.csiro.casda.votools.jpa.TapKey;
import au.csiro.casda.votools.jpa.TapKeyColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;

/**
 * Tests the VO TAP Tables Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class TablesControllerTest extends BaseTest
{
    @InjectMocks
    private TablesController controller;

    @Mock
    private VoTableRepositoryService tableService;

    private MockMvc mockMvc;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        Mockito.doReturn(true).when(tableService).isReady();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private void setupBasicMockData()
    {
        TapTable table = new TapTable();
        table.setTableName("zzzz");
        TapColumn column = createTapColumn("zzzz", "yyyyy", 1, 4, 1, "bob");
        TapColumn column2 = createTapColumn("zzzz", "xxxxx", 1, 4, 1, "fred");
        
        TapTable table2 = new TapTable();
        table2.setTableName("aaaa");
        TapColumn column2_A = createTapColumn("aaaa", "bbbb", 1, 4, 1, "gus");
        TapColumn column2_fk = createTapColumn("aaaa", "xxxx_ref", 1, 4, 1, "fred");
        
        TapSchema schema = new TapSchema();
        schema.setSchemaName("XXXX");
        
        TapKey foreignKey = new TapKey();
        foreignKey.setDescription("fk description");
        foreignKey.setTargetTable(table);
        foreignKey.setUtype("fk utype");
        
        TapKeyColumn tapKeyColumn = new TapKeyColumn();
        tapKeyColumn.setFromColumn(column2);
        tapKeyColumn.setTargetColumn(column2_fk);
        foreignKey.setKeyColumns(Arrays.asList(tapKeyColumn));
        

        Mockito.when(tableService.getSchemas()).thenReturn(Arrays.asList(schema));
        Mockito.when(tableService.getSchemaTables("XXXX")).thenReturn(Arrays.asList(table, table2));
        Mockito.when(tableService.getTableColumns("zzzz")).thenReturn(Arrays.asList(column, column2));
        Mockito.when(tableService.getTableColumns("aaaa")).thenReturn(Arrays.asList(column2_A, column2_fk));
        Mockito.when(tableService.getFromKeys("aaaa")).thenReturn(Arrays.asList(foreignKey));
    }
    
    private TapColumn createTapColumn(String tableName, String columnName, int std, int size, int indexed, String utype)
    {
        TapColumn column = new TapColumn();
        column.setId(new TapColumnPK(tableName, columnName));
        column.setStd(new Integer(size));
        column.setSize(size);
        column.setIndexed(indexed);
        column.setUtype(utype);
        return column;
    }

    /**
     * Basic test - direct to controller
     */
    @Test
    public void testGetTables()
    {
        this.setupBasicMockData();
        TableSet result = controller.getTables();

        assertEquals("XXXX", result.getSchema().get(0).getName());
        assertEquals("zzzz", result.getSchema().get(0).getTable().get(0).getName());
        assertEquals("yyyyy", result.getSchema().get(0).getTable().get(0).getColumn().get(0).getName());
    }

    /**
     * Basic test - using mockMVC and checking xml returned
     * 
     * @throws XPathExpressionException
     *             if a problem occurs reading the response
     * @throws Exception
     *             if a problem occurs performing the request
     */
    @Test
    public void testMVCGetTables() throws XPathExpressionException, Exception
    {
        this.setupBasicMockData();

        this.mockMvc.perform(get("/tap/tables")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/tableset/schema/name").string("XXXX"))
                .andExpect(xpath("/tableset/schema/table/name").string("zzzz"))
                .andExpect(xpath("/tableset/schema/table/column/name").string("yyyyy"))
                .andExpect(xpath("/tableset/schema/table/column/dataType/@size").string("4"))
                .andExpect(xpath("/tableset/schema/table/column/utype").string("bob"))
                .andExpect(xpath("/tableset/schema/table/column/flag").string("indexed"))
                .andExpect(xpath("/tableset/schema/table/foreignKey/targetTable").string("zzzz"))
                .andExpect(xpath("/tableset/schema/table/foreignKey/fkColumn/fromColumn").string("xxxxx"))
                .andExpect(xpath("/tableset/schema/table/foreignKey/fkColumn/targetColumn").string("xxxx_ref"))
                .andExpect(xpath("/tableset/schema/table/foreignKey/description").string("fk description"))
                .andExpect(xpath("/tableset/schema/table/foreignKey/utype").string("fk utype"));

    }

}
