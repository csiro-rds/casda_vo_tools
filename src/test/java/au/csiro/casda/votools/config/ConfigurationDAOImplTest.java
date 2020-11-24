package au.csiro.casda.votools.config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Unit tests for the ConfigurationDAOImpl test.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
@RunWith(Enclosed.class)
public class ConfigurationDAOImplTest
{

    /**
     * Verify the convertToTapType method.
     */
    @RunWith(Parameterized.class)
    public static class ConvertToTapTypeTest
    {

        private ConfigurationDAOImpl daoImpl;
        private String dbType;
        private String tapType;

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of dbtype and taptype
            return Arrays.asList(new Object[][] { { "double precision", "DOUBLE" },
                    { "character varying(64)", "VARCHAR" }, { "character(10)", "CHAR" }, { "text", "VARCHAR" },
                    { "integer", "INTEGER" }, { "bigint", "BIGINT" }, { "spoly", "REGION" }, { "geometry", "REGION" },
                    {"timestamp with time zone", "TIMESTAMP"}, {"TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP"}});
        }

        public ConvertToTapTypeTest(String dbType, String tapType) throws Exception
        {
            this.dbType = dbType;
            this.tapType = tapType;
            daoImpl = new ConfigurationDAOImpl();
        }

        @Test
        public void testValidate()
        {
            assertThat("Incorrect type returned", daoImpl.convertToTapType(dbType), is(tapType));
        }
    }

    /**
     * 
     * Verify the configuration methods
     */
    public static class ConfigTest
    {
        private ConfigurationDAOImpl configDaoImpl;
        
        @Mock
        private TableConfig config;
        
        @Mock
        private JdbcTemplate template;

        @Before
        public void setup()
        {
            MockitoAnnotations.initMocks(this);
            configDaoImpl = new ConfigurationDAOImpl(template);
        }
        
        @SuppressWarnings({ "unchecked" })
        @Test
        public void testUpdateTableFromTap()
        {
            boolean result = configDaoImpl.updateTableFromTap("test.the_table", config);
            assertThat(result, is(true));
            
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
            verify(template).query(sqlCaptor.capture(), argsCaptor.capture(), any(RowMapper.class));
            
            String actualSql = sqlCaptor.getAllValues().get(0);
            assertThat(actualSql, containsString("SELECT description, description_long, "));
            assertThat(actualSql, containsString("release_required, release_date"));
            assertThat(actualSql, containsString("FROM"));
            assertThat(actualSql, containsString("tap_tables"));
            assertThat(actualSql, containsString("WHERE db_table_name = ? AND db_schema_name = ?"));
            assertThat(argsCaptor.getAllValues().get(0), is(new Object[]{"the_table", "test"}));
        }
    }
}
