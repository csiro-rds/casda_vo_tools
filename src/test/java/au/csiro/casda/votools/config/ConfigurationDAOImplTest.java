package au.csiro.casda.votools.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import au.csiro.BaseTest;

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
public class ConfigurationDAOImplTest
{

    /**
     * Verify the convertToTapType method.
     */
    public static class ConvertToTapTypeTest
    {

        private ConfigurationDAOImpl daoImpl;

        public static Stream<Arguments> types()
        {
            return Stream.of(Arguments.arguments("double precision", "DOUBLE"),
                    Arguments.arguments("character varying(64)", "VARCHAR"),
                    Arguments.arguments("character(10)", "CHAR"),
                    Arguments.arguments("text", "VARCHAR"),
                    Arguments.arguments("integer", "INTEGER"),
                    Arguments.arguments("bigint", "BIGINT"),
                    Arguments.arguments("spoly", "REGION"),
                    Arguments.arguments("geometry", "REGION"),
                    Arguments.arguments("timestamp with time zone", "TIMESTAMP"),
                    Arguments.arguments("TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP"));
        }

        @BeforeEach
        public void setup()
        {
            daoImpl = new ConfigurationDAOImpl();
        }

        @ParameterizedTest
        @MethodSource("types")
        public void testValidate(String dbType, String tapType)
        {
            assertThat("Incorrect type returned", daoImpl.convertToTapType(dbType), is(tapType));
        }
    }

    /**
     * 
     * Verify the configuration methods
     */
    public static class ConfigTest extends BaseTest
    {
        private ConfigurationDAOImpl configDaoImpl;
        
        @Mock
        private TableConfig config;
        
        @Mock
        private JdbcTemplate template;

        @BeforeEach
        public void setup()
        {
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
