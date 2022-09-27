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


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.csiro.casda.votools.TestUtils;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationDAO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;

/**
 * Tests the generate sql for query method from the TapService.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class TapServiceGenerateSqlQueryTest
{
    @Mock
    private ConfigurationRegistry configRegistry;

    @Mock
    private VoTableRepositoryService voTableRepositoryService;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private Configuration config;
    
    private TapService tapService;

    /* Todays date */ 
    DateTime date = new DateTime(DateTimeZone.UTC);

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        List<TapTable> tableList = new ArrayList<>();

        // sample table that doesn't require released_date to be populated to expose data
        TapSchema ivoaSchema = new TapSchema();
        ivoaSchema.setSchemaName("ivoa");

        TapTable obscore = TestUtils.createTapTable("casda", "obs_core", ivoaSchema, "ObsCore", true, false);
        tableList.add(obscore);

        List<TapColumn> columnList = new ArrayList<>();
        TapColumn dataproductType = TestUtils.createTapColumn(obscore, "dataproduct_type", "VARCHAR", 255, "ucd", 2, 1);
        TapColumn foreignKeyId =
                TestUtils.createTapColumn(obscore, "obs_publisher_did", "VARCHAR", 255, "meta.id", 2, 2);
        obscore.addColumn(dataproductType);
        obscore.addColumn(foreignKeyId);
        columnList.add(dataproductType);
        columnList.add(foreignKeyId);

        // sample table that does require released_date to be populated to expose data
        TapSchema casdaSchema = new TapSchema();
        casdaSchema.setSchemaName("casda");

        TapTable continuumComponentTable =
                TestUtils
                        .createTapTable("casda", "continuum_component", casdaSchema, "continuum_component", true, true);
        tableList.add(continuumComponentTable);

        TapColumn id = TestUtils.createTapColumn(continuumComponentTable, "id", "BIGINT", 24, "meta.id", 1, 1);
        TapColumn releasedDate =
                TestUtils.createTapColumn(continuumComponentTable, "released_date", "TIMESTAMP", 24, "meta.id", 2, 2);
        TapColumn catalogueId =
                TestUtils.createTapColumn(continuumComponentTable, "catalogue_id", "BIGINT", 24, "meta.id", 2, 3);
        columnList.add(id);
        columnList.add(releasedDate);
        columnList.add(catalogueId);

        TapTable catalogueTable = TestUtils.createTapTable("casda", "catalogue", casdaSchema, "catalogue", true, true);
        tableList.add(catalogueTable);

        TapColumn catalogueTableId = TestUtils.createTapColumn(catalogueTable, "id", "BIGINT", 24, "meta.id", 2, 1);
        TapColumn catalogueReleasedDate =
                TestUtils.createTapColumn(catalogueTable, "released_date", "TIMESTAMP", 24, "meta.id", 2, 2);
        TapColumn imageId = TestUtils.createTapColumn(catalogueTable, "image_id", "BIGINT", 24, "meta.id", 2, 3);
        columnList.add(catalogueTableId);
        columnList.add(catalogueReleasedDate);
        columnList.add(imageId);

        TapSchema level7Schema = new TapSchema();
        level7Schema.setSchemaName("AS033");
        TapTable level7Table = TestUtils.createTapTable("casda", "AS033_targets", level7Schema, "targets", true, true);
        level7Table.setReleaseDate(new DateTime(2519, 01, 01, 15, 0));
        tableList.add(level7Table);

        TapColumn level7TableId = TestUtils.createTapColumn(level7Table, "id", "BIGINT", 24, "meta.id", 2, 1);
        columnList.add(level7TableId);

        TapTable level7TableOutOfEmbargo =
                TestUtils.createTapTable("casda", "AS033_public_targets", level7Schema, "public_targets", true, true);
        level7TableOutOfEmbargo.setReleaseDate(new DateTime(2019, 01, 01, 15, 0));
        tableList.add(level7TableOutOfEmbargo);

        TapColumn level7TableOutOfEmbargoId =
                TestUtils.createTapColumn(level7TableOutOfEmbargo, "id", "BIGINT", 24, "meta.id", 2, 1);
        columnList.add(level7TableOutOfEmbargoId);
        
        TapSchema tapSchema = new TapSchema();
        tapSchema.setSchemaName("TAP_SCHEMA");
        TapTable columnsTable = TestUtils.createTapTable("casda", "tap_columns", tapSchema, "columns", true, true);
        tableList.add(columnsTable);
        TapColumn size =
                TestUtils.createTapColumn(columnsTable, "\"size\"", "VARCHAR", 255, "meta.id", 2, 2);
        columnList.add(size);

        
        when(voTableRepositoryService.getTables()).thenReturn(tableList);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList);
        
        ConfigurationDAO dao = Mockito.mock(ConfigurationDAO.class);
        when(dao.getSchema()).thenReturn("casda");
        when(config.gtDao()).thenReturn(dao);

        tapService = Mockito.spy(new TapService(configRegistry, voTableRepositoryService));
        tapService.setConfiguration(config);
        tapService.init();
        doReturn(true).when(tapService).isReady();
        tapService.createDbChecker();
        
        when(tapService.getCurrentUTCDateTime()).thenReturn(date);
    }
    
    private void setup2() throws ConfigurationException
    {
        MockitoAnnotations.initMocks(this);
        
        tapService = null;
        configRegistry = Mockito.mock(ConfigurationRegistry.class);
        voTableRepositoryService = Mockito.mock(VoTableRepositoryService.class);
        namedParameterJdbcTemplate =  Mockito.mock(NamedParameterJdbcTemplate.class);

        List<TapTable> tableList = new ArrayList<>();

        // sample table that doesn't require released_date to be populated to expose data
        TapSchema ivoaSchema = new TapSchema();
        ivoaSchema.setSchemaName("ivoa");

        TapTable obscore = TestUtils.createTapTable("casda", "obs_core", ivoaSchema, "ObsCore", true, false);
        tableList.add(obscore);

        List<TapColumn> columnList = new ArrayList<>();
        TapColumn dataproductType = TestUtils.createTapColumnWithDifferentDbName(obscore, "dataproduct_type", "VARCHAR",
                255, "ucd", 2, 1);
        TapColumn foreignKeyId = TestUtils.createTapColumnWithDifferentDbName(obscore, "obs_publisher_did", "VARCHAR",
                255, "meta.id", 2, 2);
        obscore.addColumn(dataproductType);
        obscore.addColumn(foreignKeyId);
        columnList.add(dataproductType);
        columnList.add(foreignKeyId);

        // sample table that does require released_date to be populated to expose data
        TapSchema casdaSchema = new TapSchema();
        casdaSchema.setSchemaName("casda");

        TapTable continuumComponentTable = TestUtils.createTapTable("casda", "continuum_component", casdaSchema,
                "continuum_component", true, true);
        tableList.add(continuumComponentTable);

        TapColumn id = TestUtils.createTapColumnWithDifferentDbName(continuumComponentTable, "id", "BIGINT", 24,
                "meta.id", 1, 1);
        TapColumn releasedDate = TestUtils.createTapColumnWithDifferentDbName(continuumComponentTable, "released_date",
                "TIMESTAMP", 24, "meta.id", 2, 2);
        TapColumn catalogueId = TestUtils.createTapColumnWithDifferentDbName(continuumComponentTable, "catalogue_id",
                "BIGINT", 24, "meta.id", 2, 3);
        continuumComponentTable.addColumn(id);
        continuumComponentTable.addColumn(releasedDate);
        continuumComponentTable.addColumn(catalogueId);
        columnList.add(id);
        columnList.add(releasedDate);
        columnList.add(catalogueId);

        TapTable catalogueTable = TestUtils.createTapTable("casda", "catalogue", casdaSchema, "catalogue", true, true);
        tableList.add(catalogueTable);

        TapColumn catalogueTableId = TestUtils.createTapColumnWithDifferentDbName(catalogueTable, "id", "BIGINT", 24,
                "meta.id", 2, 1);
        TapColumn catalogueReleasedDate = TestUtils.createTapColumnWithDifferentDbName(catalogueTable, "released_date",
                "TIMESTAMP", 24, "meta.id", 2, 2);
        TapColumn imageId = TestUtils.createTapColumnWithDifferentDbName(catalogueTable, "image_id", "BIGINT", 24,
                "meta.id", 2, 3);
        catalogueTable.addColumn(catalogueTableId);
        catalogueTable.addColumn(catalogueReleasedDate);
        catalogueTable.addColumn(imageId);
        columnList.add(catalogueTableId);
        columnList.add(catalogueReleasedDate);
        columnList.add(imageId);

        TapSchema tapSchema = new TapSchema();
        tapSchema.setSchemaName("TAP_SCHEMA");
        TapTable columnsTable = TestUtils.createTapTable("casda", "tap_columns", tapSchema, "columns", true, true);
        tableList.add(columnsTable);
        TapColumn size = TestUtils.createTapColumnWithDifferentDbName(columnsTable, "\"size\"", "VARCHAR", 255,
                "meta.id", 2, 2);
        columnList.add(size);

        when(voTableRepositoryService.getTables()).thenReturn(tableList);
        when(voTableRepositoryService.getColumns()).thenReturn(columnList);
        
        ConfigurationDAO dao = Mockito.mock(ConfigurationDAO.class);
        when(dao.getSchema()).thenReturn("casda");
        when(config.gtDao()).thenReturn(dao);

        tapService = Mockito.spy(new TapService(configRegistry, voTableRepositoryService));
        tapService.setConfiguration(config);
        tapService.init();
        doReturn(true).when(tapService).isReady();
        tapService.createDbChecker();
        when(tapService.getCurrentUTCDateTime()).thenReturn(date);
    }

    @Test
    public void testGenerateSqlForQueryReleaseNotRequiredSelectAll() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select * from ivoa.ObsCore", true, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\nFROM casda.obs_core"));
        assertThat(tapService.generateSqlForQuery("select * from ivoa.ObsCore", false, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\nFROM casda.obs_core"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseNotRequiredSelectItem() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select dataproduct_type as dt from ivoa.obscore o where "
                + "o.dataproduct_type = 'image_cube'", true, null),
                is("SELECT o.dataproduct_type AS dt\nFROM casda.obs_core AS o\n"
                        + "WHERE o.dataproduct_type = 'image_cube'"));
        assertThat(tapService.generateSqlForQuery("select dataproduct_type AS dt from ivoa.obscore where "
                + "dataproduct_type = 'image_cube'", false, null),
                is("SELECT casda.obs_core.dataproduct_type AS dt\nFROM casda.obs_core\n"
                        + "WHERE casda.obs_core.dataproduct_type = 'image_cube'"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseNotRequiredSelectTop() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select top 100 * from ivoa.obscore", true, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\n"
                        + "FROM casda.obs_core\nLimit 100"));

        assertThat(tapService.generateSqlForQuery("select\t TOP \r\n100 * \n  from ivoa.obscore", true, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\n"
                        + "FROM casda.obs_core\nLimit 100"));

        assertThat(tapService.generateSqlForQuery("select top 100 * from ivoa.obscore", false, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\n"
                        + "FROM casda.obs_core\nLimit 100"));

        assertThat(tapService.generateSqlForQuery("select\t TOP \r\n100 * \n  from ivoa.obscore", false, null),
                is("SELECT casda.obs_core.dataproduct_type AS \"dataproduct_type\","
                        + "casda.obs_core.obs_publisher_did AS \"obs_publisher_did\"\n"
                        + "FROM casda.obs_core\nLimit 100"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseRequiredSelectAll() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component", true, null),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date "
                        + "AS \"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component"));
        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component", false, null),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date "
                        + "AS \"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component\nWHERE (casda.continuum_component.released_date "
                        + "IS NOT NULL AND casda.continuum_component.released_date <= '"+date+"')"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseRequiredSelectAllWithTableAlias() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component as cc", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS"
                        + " \"catalogue_id\"\nFROM casda.continuum_component AS cc\nWHERE "
                        + "(cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component as cc", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\nFROM casda.continuum_component AS cc"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseRequiredSelectAllWithTableAliasAndReleasedDateConstraint()
            throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc where released_date is null", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component AS cc\nWHERE cc.released_date IS NULL AND"
                        + " (cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc where released_date is null", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\nFROM casda.continuum_component AS cc\n"
                        + "WHERE cc.released_date IS NULL"));
    }

    @Test
    public void testGenerateSqlForQueryTwoTablesReleaseRequiredSelectAllTableWithAliasJoin() throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select cc.* from casda.continuum_component as cc, casda.catalogue as ca where "
                        + "cc.catalogue_id = ca.id", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS "
                        + "\"catalogue_id\"\nFROM casda.continuum_component AS cc CROSS JOIN "
                        + "casda.catalogue AS ca \nWHERE cc.catalogue_id = ca.id AND "
                        + "(cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"
                        + " AND (ca.released_date IS NOT NULL AND ca.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select cc.* from casda.continuum_component as cc, casda.catalogue as ca where "
                        + "cc.catalogue_id = ca.id", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component AS cc CROSS JOIN casda.catalogue AS ca \n"
                        + "WHERE cc.catalogue_id = ca.id"));
    }

    @Test
    public void testGenerateSqlForQueryTableReleaseRequiredJoinWithOneReleaseNotRequired() throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select cc.* from casda.continuum_component as cc, ivoa.ObsCore as ob where "
                        + "cc.id = ob.obs_publisher_did", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS"
                        + " \"catalogue_id\"\nFROM casda.continuum_component AS cc"
                        + " CROSS JOIN casda.obs_core AS ob \nWHERE cc.id = ob.obs_publisher_did"
                        + " AND (cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select cc.* from casda.continuum_component as cc, ivoa.ObsCore as ob where "
                        + "cc.id = ob.obs_publisher_did", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component AS cc CROSS JOIN casda.obs_core AS ob \n"
                        + "WHERE cc.id = ob.obs_publisher_did"));
    }

    @Test
    public void testGenerateSqlForQueryTwoTablesReleaseRequiredSelectAll() throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc, casda.catalogue as ca where "
                        + "cc.catalogue_id = ca.id", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS "
                        + "\"catalogue_id\",ca.id AS \"id\",ca.released_date AS \"released_date\",ca.image_id "
                        + "AS \"image_id\"\nFROM casda.continuum_component AS cc CROSS JOIN casda.catalogue "
                        + "AS ca \nWHERE cc.catalogue_id = ca.id AND (cc.released_date IS NOT NULL "
                        + "AND cc.released_date <= '"+date+"') AND (ca.released_date "
                        + "IS NOT NULL AND ca.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc, casda.catalogue as ca where "
                        + "cc.catalogue_id = ca.id", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\",ca.id AS \"id\","
                        + "ca.released_date AS \"released_date\",ca.image_id AS \"image_id\"\n"
                        + "FROM casda.continuum_component AS cc CROSS JOIN casda.catalogue AS ca \n"
                        + "WHERE cc.catalogue_id = ca.id"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseRequiredInClause() throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc WHERE cc.catalogue_id IN "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100)", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS "
                        + "\"catalogue_id\"\nFROM casda.continuum_component AS cc\nWHERE cc.catalogue_id"
                        + " IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\nWHERE ca.image_id < 100"
                        + " AND (ca.released_date IS NOT NULL AND ca.released_date <= '"+date+"'))"
                        + " AND (cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select * from casda.continuum_component as cc WHERE cc.catalogue_id IN "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100)", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component AS cc\nWHERE cc.catalogue_id "
                        + "IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\n" + "WHERE ca.image_id < 100)"));
    }

    @Test
    public void testGenerateSqlForQueryReleaseRequiredExistsClause() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component WHERE EXISTS "
                + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                + "AND casda.continuum_component.catalogue_id=ca.id)", false, null),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date"
                        + " AS \"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\""
                        + "\nFROM casda.continuum_component\nWHERE EXISTS(SELECT ca.id AS \"id\"\nFROM "
                        + "casda.catalogue AS ca\nWHERE ca.image_id < 100 AND casda.continuum_component.catalogue_id "
                        + "= ca.id AND (ca.released_date IS NOT NULL AND ca.released_date <= '"+date+"')) "
                        + "AND (casda.continuum_component.released_date IS NOT NULL "
                        + "AND casda.continuum_component.released_date"
                        + " <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component WHERE EXISTS "
                + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                + "AND casda.continuum_component.catalogue_id=ca.id)", true, null),
                is("SELECT casda.continuum_component.id AS \"id\","
                        + "casda.continuum_component.released_date AS \"released_date\","
                        + "casda.continuum_component.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component\n"
                        + "WHERE EXISTS(SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\n"
                        + "WHERE ca.image_id < 100 AND casda.continuum_component.catalogue_id = ca.id)"));
    }

    @Test
    public void testGenerateSqlForQueryAllNotConstraintClause() throws Exception
    {
        assertThat(tapService.generateSqlForQuery(
                "select ALL * from casda.continuum_component WHERE NOT catalogue_id IN "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                        + "AND casda.continuum_component.catalogue_id=ca.id)", false, null),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date "
                        + "AS \"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component\nWHERE NOT casda.continuum_component.catalogue_id"
                        + " IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\nWHERE ca.image_id < 100 "
                        + "AND casda.continuum_component.catalogue_id = ca.id AND (ca.released_date IS NOT NULL"
                        + " AND ca.released_date <= '"+date+"')) AND (casda.continuum_component.released_date"
                        + " IS NOT NULL AND casda.continuum_component.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery(
                "select ALL * from casda.continuum_component WHERE NOT catalogue_id IN "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                        + "AND casda.continuum_component.catalogue_id=ca.id)", true, null),
                is("SELECT casda.continuum_component.id AS \"id\","
                        + "casda.continuum_component.released_date AS \"released_date\","
                        + "casda.continuum_component.catalogue_id AS \"catalogue_id\"\n"
                        + "FROM casda.continuum_component\n" + "WHERE NOT casda.continuum_component.catalogue_id "
                        + "IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\n"
                        + "WHERE ca.image_id < 100 AND casda.continuum_component.catalogue_id = ca.id)"));

    }

    @Test
    public void testGenerateSqlForQueryJoin() throws Exception
    {
        assertThat(
                tapService.generateSqlForQuery("select * from casda.continuum_component as cc "
                        + "INNER JOIN (SELECT * from casda.catalogue where id < 1000) as c ON "
                        + "cc.catalogue_id=c.id", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS "
                        + "\"catalogue_id\",c.id AS \"id\",c.released_date AS \"released_date\",c.image_id AS "
                        + "\"image_id\"\nFROM casda.continuum_component AS cc INNER JOIN (SELECT casda.catalogue.id"
                        + " AS \"id\",casda.catalogue.released_date AS \"released_date\",casda.catalogue.image_id AS"
                        + " \"image_id\"\nFROM casda.catalogue\nWHERE casda.catalogue.id < 1000 AND "
                        + "(casda.catalogue.released_date IS NOT NULL AND casda.catalogue.released_date <= '"+date+"')) "
                        + "AS c ON cc.catalogue_id = c.id\nWHERE (cc.released_date IS NOT NULL AND cc.released_date <="
                        + " '"+date+"')"));

        assertThat(
                tapService.generateSqlForQuery("select * from casda.continuum_component as cc "
                        + "INNER JOIN (SELECT * from casda.catalogue where id < 1000) as c ON "
                        + "cc.catalogue_id=c.id", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\",c.id AS \"id\","
                        + "c.released_date AS \"released_date\",c.image_id AS \"image_id\"\n"
                        + "FROM casda.continuum_component AS cc INNER JOIN (SELECT casda.catalogue.id AS \"id\","
                        + "casda.catalogue.released_date AS \"released_date\","
                        + "casda.catalogue.image_id AS \"image_id\"\n"
                        + "FROM casda.catalogue\nWHERE casda.catalogue.id < 1000) " + "AS c ON cc.catalogue_id = c.id"));
    }

    @Test
    public void testMultiNested() throws Exception
    {
        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component as cc "
                + "WHERE ((cc.catalogue_id IN (select ca.id from casda.catalogue as ca "
                + "WHERE ca.image_id > 1)) AND (cc.catalogue_id IN (select ca.id from "
                + "casda.catalogue as ca WHERE ca.image_id < 3)))", false, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\",cc.catalogue_id AS "
                        + "\"catalogue_id\"\nFROM casda.continuum_component AS cc\nWHERE ((cc.catalogue_id "
                        + "IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\nWHERE ca.image_id > 1 AND"
                        + " (ca.released_date IS NOT NULL AND ca.released_date <= '"+date+"')))"
                        + " AND (cc.catalogue_id IN (SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\nWHERE "
                        + "ca.image_id < 3 AND (ca.released_date IS NOT NULL AND ca.released_date <= '"+date+"')))) "
                        + "AND (cc.released_date IS NOT NULL AND cc.released_date <= '"+date+"')"));

        assertThat(tapService.generateSqlForQuery("select * from casda.continuum_component as cc "
                + "WHERE ((cc.catalogue_id IN (select ca.id from casda.catalogue as ca "
                + "WHERE ca.image_id > 1)) AND (cc.catalogue_id IN (select ca.id from "
                + "casda.catalogue as ca WHERE ca.image_id < 3)))", true, null),
                is("SELECT cc.id AS \"id\",cc.released_date AS \"released_date\","
                        + "cc.catalogue_id AS \"catalogue_id\"\nFROM casda.continuum_component AS cc\n"
                        + "WHERE ((cc.catalogue_id IN " + "(SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\n"
                        + "WHERE ca.image_id > 1)) AND (cc.catalogue_id IN "
                        + "(SELECT ca.id AS \"id\"\nFROM casda.catalogue AS ca\nWHERE ca.image_id < 3)))"));
    }

    @Test
    public void testGenerateSqlForQueryProjectIdsConstraintClause() throws Exception
    {
        assertThat(
                tapService.generateSqlForQuery("select * from casda.continuum_component WHERE EXISTS "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                        + "AND casda.continuum_component.catalogue_id=ca.id)", false,
                        new ArrayList<Long>(Arrays.asList(new Long[] { 1l, 2l, 3l, 4l }))),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date AS "
                        + "\"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\"\nFROM "
                        + "casda.continuum_component\nWHERE EXISTS(SELECT ca.id AS \"id\"\nFROM casda.catalogue "
                        + "AS ca\nWHERE ca.image_id < 100 AND casda.continuum_component.catalogue_id = ca.id "
                        + "AND (ca.project_id IN (1 , 2 , 3 , 4) OR (ca.released_date IS NOT NULL AND " 
                        + "ca.released_date <= '"+date+"'))) AND "
                        + "(casda.continuum_component.project_id IN (1 , 2 , 3 , 4) OR "
                        + "(casda.continuum_component.released_date IS NOT NULL AND " 
                        + "casda.continuum_component.released_date <= '"+date+"'))"));


        assertThat(
                tapService.generateSqlForQuery("select * from casda.continuum_component WHERE EXISTS "
                        + "(select ca.id from casda.catalogue as ca WHERE ca.image_id < 100 "
                        + "AND casda.continuum_component.catalogue_id=ca.id)", true,
                        new ArrayList<Long>(Arrays.asList(new Long[] { 1l, 2l, 3l, 4l }))),
                is("SELECT casda.continuum_component.id AS \"id\",casda.continuum_component.released_date AS "
                        + "\"released_date\",casda.continuum_component.catalogue_id AS \"catalogue_id\"\nFROM "
                        + "casda.continuum_component\nWHERE EXISTS(SELECT ca.id AS \"id\"\nFROM casda.catalogue "
                        + "AS ca\nWHERE ca.image_id < 100 AND casda.continuum_component.catalogue_id = ca.id)"));
    }   
    
    @Test
    public void testGenerateSqlForQuerySearchBySbid() throws Exception
    {
       setup2();
        assertThat(
                tapService.generateSqlForQuery(
                        "select * from casda.continuum_component as cc, casda.catalogue as ca where "
                                + "cc.catalogue_id = ca.id",
                        false, null),
                is("SELECT cc.dbid AS \"id\",cc.dbreleased_date AS \"released_date\",cc.dbcatalogue_id AS "
                        + "\"catalogue_id\",ca.dbid AS \"id\",ca.dbreleased_date AS \"released_date\",ca.dbimage_id "
                        + "AS \"image_id\"\nFROM casda.continuum_component AS cc CROSS JOIN casda.catalogue AS ca"
                        + " \nWHERE cc.dbcatalogue_id = ca.dbid AND (cc.released_date IS NOT NULL AND cc.released_date"
                        + " <= '"+date+"') AND (ca.released_date IS NOT NULL AND ca.released_date <= "
                        + "'"+date+"')"));

        assertThat(
                tapService.generateSqlForQuery(
                        "select * from casda.continuum_component as cc, casda.catalogue as ca where "
                                + "cc.catalogue_id = ca.id",
                        true, null),
                is("SELECT cc.dbid AS \"id\",cc.dbreleased_date AS \"released_date\",cc.dbcatalogue_id "
                        + "AS \"catalogue_id\",ca.dbid AS \"id\",ca.dbreleased_date AS \"released_date\","
                        + "ca.dbimage_id AS \"image_id\"\nFROM casda.continuum_component AS cc "
                        + "CROSS JOIN casda.catalogue AS ca \nWHERE cc.dbcatalogue_id = ca.dbid"));
    }


    @Test
    public void testGenerateSqlForQueryWholeTableEmbargoed() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(any(), any()))
                .thenReturn(Collections.singletonList(15L));
        
        // Admin user has access
        assertThat(tapService.generateSqlForQuery("select * from AS033.targets", true, null),
                is("SELECT casda.AS033_targets.id AS \"id\"\n"
                        + "FROM casda.AS033_targets"));
        
        // Project team member user has access
        assertThat(tapService.generateSqlForQuery("select * from AS033.targets", false, Collections.singletonList(15L)),
                is("SELECT casda.AS033_targets.id AS \"id\"\n"
                        + "FROM casda.AS033_targets"));
        // Anonymous user does not
        assertThat(tapService.generateSqlForQuery("select * from AS033.targets", false, null),
                is("SELECT casda.AS033_targets.id AS \"id\"\n"
                        + "FROM casda.AS033_targets\nWHERE 2 < 1"));
    }


    @Test
    public void testGenerateSqlForQueryWholeTableOutOfEmbargo() throws Exception
    {
        when(voTableRepositoryService.fetchProjectIdsFromCodes(any(), any()))
                .thenReturn(Collections.singletonList(15L));
        
        // Admin user has access
        assertThat(tapService.generateSqlForQuery("select * from AS033.public_targets", true, null),
                is("SELECT casda.AS033_public_targets.id AS \"id\"\n"
                        + "FROM casda.AS033_public_targets"));
        
        // Project team member user has access
        assertThat(
                tapService.generateSqlForQuery("select * from AS033.public_targets", false,
                        Collections.singletonList(15L)),
                is("SELECT casda.AS033_public_targets.id AS \"id\"\n" + "FROM casda.AS033_public_targets"));
        // Anonymous user has access as it is now public
        assertThat(tapService.generateSqlForQuery("select * from AS033.public_targets", false, null),
                is("SELECT casda.AS033_public_targets.id AS \"id\"\n"
                        + "FROM casda.AS033_public_targets"));
    }

}
