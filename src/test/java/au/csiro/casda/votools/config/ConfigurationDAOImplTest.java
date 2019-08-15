package au.csiro.casda.votools.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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

}
