package au.csiro.casda.votools.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.votools.VoToolsApplication;
import au.csiro.casda.votools.VoToolsApplication.ConfigLocation;
import au.csiro.casda.votools.config.Configuration.Action;
import au.csiro.casda.votools.config.Configuration.Change;
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
 * 
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationTest.Config.class })
@Ignore("Database modification tests should be used only when modifying the config handling")
public class ConfigurationTest
{
    @Autowired
    private ConfigurationRegistry configRegistry;

    private String datasourceUrl;

    private String userName;

    private String password;

    private String driverClassName;

    private DriverManagerDataSource dataSource;
    private ConfigurationDAOImpl dao;
    private Configuration original;
    private Configuration clone;
    private YamlParser parser;
    private boolean haveDB;

    @Before
    public void setUp() throws Exception
    {
        if (datasourceUrl == null) // properties not found, set default values spring.datasource.url
        {
            datasourceUrl = "jdbc:postgresql://localhost:5432/casda";
            userName = "casdbusr";
            password = "password";
            driverClassName = "org.postgresql.Driver";
        }
        if (dao == null)
        {
            try
            {
                if (datasourceUrl == null || !datasourceUrl.contains("jdbc:postgresql://"))
                {
                    throw new Exception("Wrong database");
                }
                dataSource = new DriverManagerDataSource();
                dataSource.setUrl(datasourceUrl);
                dataSource.setUsername(userName);
                dataSource.setPassword(password);
                dataSource.setDriverClassName(driverClassName);
                dao = new ConfigurationDAOImpl(new JdbcTemplate(dataSource));
                dao.execute("DROP TABLE IF EXISTS casda.does_not_exist_anyway");
                haveDB = true;
            }
            catch (Exception e)
            {
                dao = null;
                haveDB = false;
            }
        }
        if (haveDB)
        {
            for (String statement : sql)
            {
                dao.execute(statement);
            }
            Configuration.setRegistry(configRegistry);
            parser = new YamlBeansParser();
            original = new Configuration();
            configRegistry.switchConfiguration(original, false);
            original.put("connection.url", datasourceUrl);
            original.put("connection.username", userName);
            original.put("connection.password", password);
            original.put("connection.driverClassName", driverClassName);
            original.put(Configuration.DEFAULT_DB_SCHEMA, "casda");
            EndPoint endPoint = new EndPoint();
            endPoint.setType(EndPoint.Type.TAP);
            endPoint.addTable("casda.test_files");
            endPoint.addTable("casda.test_table");
            endPoint.addTable("casda.test_columns");
            original.addEndPoint("TAP", endPoint);
            original.setParser(parser);
            original.initDao();
            // Have to create basic TAP metadata, else there will be constraint violations when trying to modify a
            // single test table that refers to other test tables
            original.setChangeLevel(Configuration.Change.DROP);
            // May have to drop leftovers from previous tests, especially if they crashed
            original.gtDao().checkTapDbVersion();
            ConfigurationDAO dao = original.gtDao();
            TapTable table = dao.findOneTable("casda.tables");
            // Collection<TapColumn> columns = dao.findAllColumns();

            original = original.export(false); // learn db information
            String text = original.toString();
            if (text == null && table != null)
            {
                throw new ConfigurationException("This does not happen");
            }
            clone = new Configuration(parser, text);
            configRegistry.switchConfiguration(original, true);
        }
    }

    @Test
    public void testAddDropTapTable() throws ConfigurationException
    {
        String find = "tables: \n" + "   casda.tap_columns:";
        String replace = "tables: \n" + "   casda.new_table:\n" + "      columns:\n" + "         test_id: !Column\n"
                + "            options: !Map\n" + "               description: description of test_id\n"
                + "               column_order: 1\n" + "            type: character varying(64)\n"
                + "      constraints:\n"
                + "         new_table_fkey: FOREIGN KEY (test_id) REFERENCES casda.tap_tables(table_name)\n"
                + "      options: !Map\n" + "         description: new table description\n"
                + "         tap.schema.name: TAP_SCHEMA\n" + "         creation.order: 5\n"
                + "         description_long: new table long description\n" + "   casda.tap_columns:";
        if (haveDB)
        {
            ConfigurationDAOImpl dao = (ConfigurationDAOImpl) original.gtDao();
            JdbcTemplate template = dao.getTemplate();
            String tapConfigText = dao.readTapConfiguration();
            tapConfigText = tapConfigText.replaceAll("\\r\\n", "\n");
            original.setChangeLevel(Configuration.Change.UPDATE);
            String dropQuery = "DROP TABLE IF EXISTS casda.new_table";
            template.execute(dropQuery);
            dao.enforceTapVersion(tapConfigText);
            tapConfigText = tapConfigText.replace(find, replace);
            dao.enforceTapVersion(tapConfigText);
            // check that the new table has been created
            String testQuery = "SELECT count(*) FROM casda.new_table";
            template.queryForObject(testQuery, Integer.class);
            // check that its column options have been saved to tap tables
            String colDecsQuery =
                    String.format("SELECT description from casda.%s where table_name='TAP_SCHEMA.new_table'"
                            + " and column_name='test_id'", ConfigurationDAOImpl.COLUMNS_TABLE_NAME);
            String colDesc = template.queryForObject(colDecsQuery, String.class);
            assertEquals("Failed to save field options", "description of test_id", colDesc);
            // check that its table options have been saved to tap tables
            String tableDesc = template.queryForObject(
                    String.format("SELECT description from casda.%s where table_name='TAP_SCHEMA.new_table'",
                            ConfigurationDAOImpl.TABLES_TABLE_NAME),
                    String.class);
            assertEquals("Failed to save table options", "new table description", tableDesc);
            // check that its constraints have been saved to tap tables
            int count =
                    template.queryForObject(String.format("SELECT count(*) from casda.%s where key_id='new_table_fkey'",
                            ConfigurationDAOImpl.KEYS_TABLE_NAME), Integer.class);
            assertEquals("Failed to save foreign key", 1, count);

        }
    }

    @Test
    public void testAddDropTapColumn() throws ConfigurationException
    {
        String find = "tables: \n" + "   casda.tap_columns: \n" + "      columns: \n";
        String replace = "tables: \n" + "   casda.tap_columns: \n" + "      columns: \n" + "         test_id: !Column\n"
                + "            options: !Map\n" + "               description: description of test_id\n"
                + "            type: character varying(64)\n";
        if (haveDB)
        {
            ConfigurationDAOImpl dao = (ConfigurationDAOImpl) original.gtDao();
            JdbcTemplate template = dao.getTemplate();
            String tapConfigText = dao.readTapConfiguration();
            tapConfigText = tapConfigText.replaceAll("\\r\\n", "\n");
            original.setChangeLevel(Configuration.Change.UPDATE);
            String dropQuery = "ALTER TABLE casda.tap_columns DROP COLUMN IF EXISTS test_id CASCADE";
            template.execute(dropQuery);
            dao.enforceTapVersion(tapConfigText);
            tapConfigText = tapConfigText.replace(find, replace);
            dao.enforceTapVersion(tapConfigText);
            // check that the new column has been added
            String testQuery = "SELECT test_id FROM casda.tap_columns";
            template.queryForList(testQuery, String.class);
            // check that its column options have been saved to tap tables
            String colDecsQuery = String.format("SELECT description from casda.%s where table_name='TAP_SCHEMA.columns'"
                    + " and column_name='test_id'", ConfigurationDAOImpl.COLUMNS_TABLE_NAME);
            String colDesc = template.queryForObject(colDecsQuery, String.class);
            assertEquals("Failed to save field options", "description of test_id", colDesc);

            // Testing dropping TAP table column

            tapConfigText = tapConfigText.replace(replace, find);
            original.setChangeLevel(Configuration.Change.DROP);
            dao.enforceTapVersion(tapConfigText);
            // check that the column has been dropped
            try
            {
                template.queryForList(testQuery, String.class);
                fail("Dropped column still exists.");
            }
            catch (Exception e)
            {
                // check that the column options have been removed from tap tables
                testQuery = String.format("SELECT count(*) from casda.%s where table_name='TAP_SCHEMA.columns'"
                        + " and column_name='test_id'", ConfigurationDAOImpl.COLUMNS_TABLE_NAME);
                int count = template.queryForObject(testQuery, Integer.class);
                assertEquals("Failed to clean dropped fields options", 0, count);
            }

        }
    }

    @Test
    public void testChangeTapColumn() throws ConfigurationException
    {
        String find = "tables: \n" + "   casda.tap_columns: \n" + "      columns: \n";
        String replace = "tables: \n" + "   casda.tap_columns: \n" + "      columns: \n" + "         test_id: !Column\n"
                + "            options: !Map\n" + "               description: description of test_id\n"
                + "            type: character varying(64)\n";
        String replace2 =
                "tables: \n" + "   casda.tap_columns: \n" + "      columns: \n" + "         test_id: !Column\n"
                        + "            options: !Map\n" + "               description: description 2 of test_id\n"
                        + "            type: character varying(641)\n";
        if (haveDB)
        {
            ConfigurationDAOImpl dao = (ConfigurationDAOImpl) original.gtDao();
            String tapConfigText = dao.readTapConfiguration();
            tapConfigText = tapConfigText.replaceAll("\\r\\n", "\n");
            original.setChangeLevel(Configuration.Change.UPDATE);
            tapConfigText = tapConfigText.replace(find, replace);
            dao.enforceTapVersion(tapConfigText); // This adds a test column test_id to casda.columns

            // Test change to compatible type
            tapConfigText = tapConfigText.replace(replace, replace2);
            dao.enforceTapVersion(tapConfigText);// This changes type of this column
            Configuration config = new Configuration(original.gtParser(), tapConfigText);
            config = config.export(false);
            ColumnConfig cc = config.getTables().get("casda.tap_columns").getColumns().get("test_id");
            assertEquals("Failed to change field type", cc.getType(), "character varying(641)");
            assertEquals("Failed to change field description", cc.get(ColumnConfig.DESCRIPTION),
                    "description 2 of test_id");
        }
    }

    @Test
    public void testDropColumn() throws ConfigurationException
    {
        if (haveDB)
        {
            // remove a column
            clone.getTables().get("casda.test_table").getColumns().remove("file_id");
            clone.getTables().get("casda.test_table").gtConstraintConfigs().remove("fkey_test_table_file_id");
            clone.getTables().get("casda.test_table").getConstraints().remove("fkey_test_table_file_id");
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                fail("unauthorised change succeeded");
            }
            catch (ConfigurationException e)
            {
                String text = clone.act(Configuration.Action.APPLY, Configuration.Change.DROP);
                assertFalse(text.contains("file_id: !Column"));
                assertTrue(text.contains("casda.test_table:"));
            }
        }
    }

    @Test
    public void testDropIndex() throws ConfigurationException
    {
        if (haveDB)
        {
            // remove a column
            clone.getTables().get("casda.test_table").getIndexDefs().remove("idx_test_table_file_id");
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                fail("unauthorised change succeeded");
            }
            catch (ConfigurationException e)
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.DROP);
                String text = clone.export(false).toString();
                assertFalse(text.contains("idx_test_table_file_id:"));
                assertTrue(text.contains("casda.test_table:"));
            }
        }
    }

    @Test
    public void testAddIndex() throws ConfigurationException
    {
        if (haveDB)
        {
            testDropIndex();
            original.export(false);
            configRegistry.switchConfiguration(original, false);
            String in = "CREATE INDEX idx_test_table_file_id ON casda.test_table USING btree (file_id)";
            clone.getTables().get("casda.test_table").getIndexDefs().put("idx_test_table_file_id", in);
            clone.wire();
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                fail("unauthorised change succeeded");
            }
            catch (ConfigurationException e)
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.REINDEX);
                String text = clone.export(false).toString();
                assertTrue(text.contains("idx_test_table_file_id:"));
                assertTrue(text.contains("casda.test_table:"));
            }
        }
    }

    @Test
    public void testDropConstraint() throws ConfigurationException
    {
        if (haveDB)
        {
            String text = clone.export(false).toString();
            assertTrue(text.contains("fkey_test_table_file_id:"));
            clone.getTables().get("casda.test_table").getConstraints().remove("fkey_test_table_file_id");
            clone.wire();
            clone.act(Configuration.Action.APPLY, Configuration.Change.DROP);
            text = clone.export(false).toString();
            assertFalse(text.contains("fkey_test_table_file_id:"));
            assertTrue(text.contains("casda.test_table:"));
        }
    }

    @Test
    public void testAddConstraint() throws ConfigurationException
    {
        if (haveDB)
        {
            testDropConstraint();
            original.export(false);
            configRegistry.switchConfiguration(original, false);
            String val = "FOREIGN KEY (file_id) REFERENCES casda.test_files (fid)";
            clone.getTables().get("casda.test_table").getConstraints().put("fkey_test_table_file_id", val);
            clone.wire();
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.NONE);
                fail("unauthorised change succeeded");
            }
            catch (ConfigurationException e)
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                String text = clone.export(false).toString();
                assertTrue(text.contains("fkey_test_table_file_id:"));
                assertTrue(text.contains("casda.test_table:"));
            }
        }
    }

    @Test
    public void testAddTable() throws ConfigurationException
    {
        if (haveDB)
        {
            TableConfig removedTable = clone.getTables().remove("casda.test_table");

            // now the table has been removed from the DB and configuration
            ConfigurationDAOImpl dao = (ConfigurationDAOImpl) original.gtDao();
            JdbcTemplate template = dao.getTemplate();
            String dropQuery = "DROP TABLE IF EXISTS casda.test_table";
            template.execute(dropQuery);

            // Let's add it back
            clone.getTables().put("casda.test_table", removedTable);
            int i = 1;
            for (ColumnConfig column : removedTable.getColumns().values())
            {
                column.put("column_order", String.valueOf(i++));
            }
            clone.getEndPoints().get("TAP").getTables().add("casda.test_table");
            original = original.export(false);
            configRegistry.switchConfiguration(original, false);
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                fail("expected exception");
            }
            catch (ConfigurationException e)
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.REINDEX);
                String text = clone.export(false).toString();
                assertTrue(text.contains("casda.test_table:"));
            }
        }
    }

    @Test
    public void testUpdateColumnType() throws ConfigurationException
    {
        if (haveDB)
        {
            clone.getTableConfig("casda.test_table").getColumns().get("file_id").setType("character varying(999)");
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.NONE);
                fail("unauthorised change succeeded");
            }
            catch (ConfigurationException e)
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                String text = clone.export(false).toString();
                assertTrue(text.contains("file_id: !Column"));
                assertTrue(text.contains("character varying(999)"));
            }
        }
    }

    @Test
    public void testChangeColumnType() throws ConfigurationException
    {
        if (haveDB)
        {
            clone.getTableConfig("casda.test_table").getColumns().get("file_id").setType("integer");
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
                fail("unauthorised change succeeded");
            }
            catch (Exception e)
            {
                assertEquals("Exception type", ConfigurationException.class, e.getClass());
                assertEquals("Exception message", "au.csiro.casda.votools.config.ConfigurationException: "
                        + "Type change is not supported, drop the column first", e.getMessage());
            }
        }
    }

    @Test
    public void testSaveColumnTapParams() throws ConfigurationException
    {
        String columnParams[] =
                { "description", "unit", "ucd", "utype", "principal", "indexed", "std", "scs_verbosity" };

        if (haveDB)
        {
            change(clone.getTableConfig("casda.test_table").getColumns().get("file_id"), columnParams, 200);
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.NONE);
                fail("unauthorised change succeeded");
            }
            catch (Exception e)
            {
                act();
                check(clone.getTableConfig("casda.test_table").getColumns().get("file_id"), columnParams, 200);
                // Now change again to make sure that the first changes were not there already
                change(clone.getTableConfig("casda.test_table").getColumns().get("file_id"), columnParams, 300);
                act();
                check(clone.getTableConfig("casda.test_table").getColumns().get("file_id"), columnParams, 300);
            }
        }

    }

    @Test
    public void testSaveTableTapParams() throws ConfigurationException
    {
        String tableParams[] = { "description", "description_long", "utype", "params" };

        if (haveDB)
        {
            change(clone.getTableConfig("casda.test_table"), tableParams, 200);
            clone.getTableConfig("casda.test_table").put("scs_enabled", "1");
            clone.getTableConfig("casda.test_table").put("release_required", "1");
            try
            {
                clone.act(Configuration.Action.APPLY, Configuration.Change.NONE);
                fail("unauthorised change succeeded");
            }
            catch (Exception e)
            {
                act();
                check(clone.getTableConfig("casda.test_table"), tableParams, 200);
                assertEquals("Failed parameter scs_enabled", "1",
                        clone.getTableConfig("casda.test_table").get("scs_enabled"));
                assertEquals("Failed parameter release_required", "1",
                        clone.getTableConfig("casda.test_table").get("release_required"));
                // Now change again to make sure that the first changes were not there already
                change(clone.getTableConfig("casda.test_table"), tableParams, 300);
                clone.getTableConfig("casda.test_table").put("scs_enabled", "0");
                clone.getTableConfig("casda.test_table").put("release_required", "0");
                act();
                check(clone.getTableConfig("casda.test_table"), tableParams, 300);
                assertEquals("Failed parameter scs_enabled", "0",
                        clone.getTableConfig("casda.test_table").get("scs_enabled"));
                assertEquals("Failed parameter release_required", "0",
                        clone.getTableConfig("casda.test_table").get("release_required"));
            }
        }

    }

    /**
     * Verify act will complete a CURRENT request when the database is not set
     * 
     * @throws ConfigurationException
     *             Not expected
     */
    @Test
    public void testActCurrentNoDb() throws ConfigurationException
    {
        Configuration config = new Configuration();

        config.put("connection.url", "");
        config.put("connection.username", "");
        config.put("connection.password", "");

        String result = config.act(Action.CURRENT, Change.NONE);
        assertThat(result, startsWith("!Configuration"));
        assertThat(result, containsString("connection.url"));
    }

    private void change(Options options, String[] params, int value)
    {
        for (String param : params)
        {
            options.put(param, String.valueOf(value++));
        }
    }

    private void act() throws ConfigurationException
    {
        clone.act(Configuration.Action.APPLY, Configuration.Change.UPDATE);
        configRegistry.switchConfiguration(original.export(false), false);
        clone.getTables().remove("casda.test_table"); // But leave it in EndPoints
        // re-read this table configuration, including TAP params, from DB
        clone = clone.export(false);
    }

    private void check(Options options, String[] params, int value)
    {
        for (String param : params)
        {
            assertEquals("Failed parameter " + param, String.valueOf(value++), options.get(param));
        }

    }

    /**
     * Creates a configuration base object for use in tests
     * 
     * @return a basic Configuration object
     * @throws ConfigurationException
     */
    public static Configuration getTestConfiguration()
    {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DataSource dataSource = Mockito.mock(DataSource.class);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        ConfigurationDAOImpl dao = new ConfigurationDAOImpl(jdbcTemplate);
        // Create a minimal configuration object
        ConfigurationRegistry registry = ConfigurationRegistry.getStaticRegistry();
        Configuration.setRegistry(registry);
        Configuration config = new Configuration();
        try
        {
            config.addDefaults();
        }
        catch (CannotGetJdbcConnectionException e) // this environment does not have a db
        {
            registry = null; // do nothing
        }
        config.setDao(dao);
        dao.setConfig(config);
        
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(String.class)))
            .thenReturn("result", "value1","value2");
        
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Integer.class))).thenReturn(800);
        return config;
    }

    // @formatter:off

    String[] sql = { "DROP TABLE IF EXISTS casda.test_table", "DROP TABLE IF EXISTS casda.test_files",
            "DROP TABLE IF EXISTS casda.test_columns",

            "CREATE TABLE casda.test_files" + "( " + "  fid character varying(255) NOT NULL PRIMARY KEY,"
                    + "  ttime character varying(255) UNIQUE" + ")",

            "CREATE TABLE casda.test_columns " + "(" + "  column_name character varying(64) NOT NULL,"
                    + "  table_name character varying(64) NOT NULL," + "  column_order integer NOT NULL DEFAULT 0,"
                    + "  description character varying(255)," + "  unit character varying(64),"
                    + "  ucd character varying(255)," + "  utype character varying(255),"
                    + "  datatype character varying(64)," + "  size integer," + "  principal integer,"
                    + "  indexed integer," + "  std integer," + "  scs_verbosity integer, "
                    + "  CONSTRAINT columns_pkey2 PRIMARY KEY (table_name, column_name) " + ")",

            "CREATE TABLE casda.test_table" + "( " + "  id bigserial NOT NULL PRIMARY KEY,"
                    + "  file_id character varying(255)," + "  column_name character varying(64) NOT NULL,"
                    + "  table_name character varying(64) NOT NULL,"
                    + "  CONSTRAINT fkey_test_table_file_id FOREIGN KEY (file_id) REFERENCES casda.test_files(fid),"
                    + "  FOREIGN KEY (table_name, column_name) REFERENCES casda.test_columns(table_name, column_name)"
                    + ")",

            "COMMENT ON TABLE casda.test_table "
                    + "IS 'Database store of processes started to download files from archive'",
            "COMMENT ON COLUMN casda.test_table.id IS 'The unique record id'",
            "COMMENT ON COLUMN casda.test_table.file_id IS 'The unique NGAS file id to use for downloading'",

            "DROP INDEX IF EXISTS casda.idx_test_table_file_id",
            "CREATE INDEX idx_test_table_file_id ON casda.test_table (file_id)",

            "INSERT INTO casda.test_columns values "
                    + "('id', 'casda.test_table', 1, 'desc1', 'unit1', 'ucd1', 'utype1', 'datatype1', 4, 1, 1, 1, 2), "
                    + "('file_id', 'casda.test_table', 2, 'desc2', 'unit2', 'ucd2', 'utype2', 'datatype2', 44, 0, 0, 0, 2), "
                    + "('fid', 'casda.test_files', 3, 'desc3', 'unit3', 'ucd3', 'utype3', 'datatype3', 55, 1, 1, 1, 2), "
                    + "('ttime', 'casda.test_files', 4, 'desc4', 'unit4', 'ucd4', 'utype4', 'datatype4', 4, 1, 0, 0, 2) " };
    // @formatter:on

    /**
     * Test-specific Configuration class
     */
    @PropertySource("classpath:/application.properties")
    @PropertySource("classpath:/unittest/application.properties")
    @ComponentScan(useDefaultFilters = false, basePackageClasses = { VoToolsApplication.class }, includeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ConfigurationRegistry.class) })
    public static class Config
    {
        /**
         * Required to configure the PropertySource(s) (see https://jira.spring.io/browse/SPR-8539)
         * 
         * @return a PropertySourcesPlaceholderConfigurer
         */
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
        {
            return new PropertySourcesPlaceholderConfigurer();
        }
        
        /**
         * @return A bean to hold the configuration locations.
         */
        @Bean
        public ConfigLocation getConfigLocation()
        {
            return new ConfigLocation(new HashSet<>(Arrays.asList(new String[] {"config"})));
        }

    }

}
