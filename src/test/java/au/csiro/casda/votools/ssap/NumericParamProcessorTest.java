package au.csiro.casda.votools.ssap;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
    public static class ValidateValidTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "300" }, { "300/600" }, { "300/" }, { "/600" }, { "/" }, { "0.21" },
                    { "3.5e-1" }, { "3.5e1" }, { "3.5e+1" }, { "3.5e-1/4e-1" }, { "3.5E-1" }, { "3.5E1" }, { "3.5E+1" },
                    { "3.5E-1/4E-1" }, { "3.5e-1/4E-1" }, { "0.21,0.28" }, { "3.5e-1,0.12" },
                    { "3.5e-1,0.12,999,1.001" }, { "0.21/0.22,0.13/0.135" }, { "52,-27.8;Galactic" }, 
                    { "+0.21/+0.215;Source" }, { " 0.21" }, { "0.21 " }, { " 0.21 " },
                    { "" }, { new String[] { "500", "0.21" } },
                    { new String[] { "5e-9", "2.1e-01", "2.1E-01", "/5e-9", "5e-9/", "5e-9/2.05e+2" } } });
        }

        private String[] validParamValues;

        private NumericParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new NumericParamProcessor(1000, true, Arrays.asList(new String[] { "GALACTIC", "SOURCE" }),
                    "qualifier");

            if (validValue instanceof String[])
            {
                validParamValues = (String[]) validValue;
            }
            else if (validValue instanceof String)
            {
                validParamValues = new String[] { (String) validValue };
            }
        }

        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("BAND", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validateDouble method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateInvalidTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "a" }, { "300\\600" }, { "300//" }, { "300/n" }, { "/600/" }, { "2." },
                    { "300 NaN" }, { "NaN 600" }, { "NaN NaN" }, { "+Inf 600" }, { "300 -Inf" }, { "+Inf -Inf" },
                    { "-inf 600" }, { "300 +inf" }, { "inf 600" }, { "300 inf" }, { "2.3-01" }, { "2.3e" }, { "2.3e-" },
                    { "2.3e 6" }, { "2.3 e-5" }, { "300 600 1300" }, { "600/300" } });
        }

        private NumericParamProcessor processor;

        private String invalidValue;

        public ValidateInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
        }

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

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] { { "300", "(minCol <= 300 AND maxCol >= 300)" },
                    { "300/600", "(minCol <= 600 AND maxCol >= 300)" }, { "300/", "(maxCol >= 300)" },
                    { "/600", "(minCol <= 600)" }, { "", "" }, { "+1E-2", "(minCol <= +1E-2 AND maxCol >= +1E-2)" },
                    { "/", "(minCol IS NOT NULL AND maxCol IS NOT NULL)" },
                    { new String[] { "5e+2", "0.21" },
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
            processor = new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
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

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] { { "300", "(s_fov <= 300 AND s_fov >= 300)" },
                    { "300/600", "(s_fov <= 600 AND s_fov >= 300)" }, { "300/", "(s_fov >= 300)" },
                    { "/600", "(s_fov <= 600)" },
                    { "", "" },
                    { "/", "(s_fov IS NOT NULL AND s_fov IS NOT NULL)" },
                    { new String[] { "5e+2", "0.21" },
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
            processor = new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

}
