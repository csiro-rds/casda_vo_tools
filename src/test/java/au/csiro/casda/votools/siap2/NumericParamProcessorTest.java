package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Verify the workings of NumericParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class NumericParamProcessorTest
{
    /**
     * Check the validateDouble method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDoubleValidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "300" }, { "300 600" }, { "300 +Inf" }, { "-Inf 600" }, { "-Inf +Inf" },
                    { "0.21" }, { "3.5e-1" }, { "3.5e1" }, { "3.5e+1" }, { "3.5e-1 4e-1" }, { "3.5e-1   4e-1" },
                    { " 0.21" }, { "0.21 " }, { " 0.21 " }, { "" }, { new String[] { "500", "0.21" } },
                    { new String[] { "5e-9", "2.1e-01", "2.1e-01", "-Inf 5e-9", "5e-9 +Inf", "5e-9 2.05e+2" } } });
        }

        private String[] validParamValues;

        private NumericParamProcessor processor;

        public ValidateDoubleValidTest(Object validValue) throws Exception
        {
            processor = new NumericParamProcessor();

            if (validValue instanceof String[])
            {
                validParamValues = (String[]) validValue;
            }
            else if (validValue instanceof String)
            {
                validParamValues = new String[] { (String) validValue };
            }
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateDouble()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("BAND", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validateDouble method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDoubleInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "a" }, { "300\\600" }, { "300//" }, { "/600/" }, { "2." },
                    { "300 NaN" }, { "NaN 600" }, { "NaN NaN" }, { "+Inf 600" }, { "300 -Inf" }, { "+Inf -Inf" },
                    { "-inf 600" }, { "300 +inf" }, { "inf 600" }, { "300 inf" }, { "2.3-01" }, { "2.3e" }, { "2.3e-" },
                    { "2.3e 6" }, { "2.3 e-5" }, { "300 600 1300" } });
        }

        private NumericParamProcessor processor;

        private String invalidValue;

        public ValidateDoubleInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new NumericParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateDouble()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid BAND value " + invalidValue),
                    processor.validate("BAND", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] { { "300", "(minCol <= 300 AND maxCol >= 300)" },
                    { "300 600", "(minCol <= 600 AND maxCol >= 300)" }, { "300 +Inf", "(maxCol >= 300)" },
                    { "-Inf 600", "(minCol <= 600)" }, { "300   600", "(minCol <= 600 AND maxCol >= 300)" },
                    { "300   +Inf", "(maxCol >= 300)" }, { "-Inf  600", "(minCol <= 600)" }, { "", "" },
                    { "-Inf +Inf", "(minCol IS NOT NULL AND maxCol IS NOT NULL)" },
                    { "-Inf     +Inf", "(minCol IS NOT NULL AND maxCol IS NOT NULL)" }, { new String[] { "5e+2", "0.21" },
                            "(minCol <= 5e+2 AND maxCol >= 5e+2) OR (minCol <= 0.21 AND maxCol >= 0.21)" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private NumericParamProcessor processor;

        public BuildQueryTest(Object value, String expectedWhereClause)
        {
            this.expectedAdql = expectedWhereClause;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
            processor = new NumericParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

    /**
     * Check the BuildQuery method with a set of values for a single field parameter.
     */
    @RunWith(Parameterized.class)
    public static class BuildQuerySingleFieldTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] { { "300", "(s_fov <= 300 AND s_fov >= 300)" },
                    { "300 600", "(s_fov <= 600 AND s_fov >= 300)" }, { "300 +Inf", "(s_fov >= 300)" },
                    { "-Inf 600", "(s_fov <= 600)" }, { "300   600", "(s_fov <= 600 AND s_fov >= 300)" },
                    { "300   +Inf", "(s_fov >= 300)" }, { "-Inf  600", "(s_fov <= 600)" }, { "", "" },
                    { "-Inf +Inf", "(s_fov IS NOT NULL AND s_fov IS NOT NULL)" },
                    { "-Inf     +Inf", "(s_fov IS NOT NULL AND s_fov IS NOT NULL)" }, { new String[] { "5e+2", "0.21" },
                            "(s_fov <= 5e+2 AND s_fov >= 5e+2) OR (s_fov <= 0.21 AND s_fov >= 0.21)" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private NumericParamProcessor processor;

        public BuildQuerySingleFieldTest(Object value, String expectedWhereClause)
        {
            this.expectedAdql = expectedWhereClause;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
            processor = new NumericParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

}
