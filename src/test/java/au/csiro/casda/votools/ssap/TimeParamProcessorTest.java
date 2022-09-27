package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
 * Verify the workings of TimeParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class TimeParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateValidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(
                    new Object[][] { { "2012-01-01" }, { "2012-01-01T22:22:22" },
                            { "2012-01-01/2014-01-10" }, { "2012/2013" },
                            { "2012-01-01/" }, { "/2014-01-10" }, { "/" }, { "" }, 
                            { "2012-01" }, { "2012-02" }, { "2012" }, { "2999-12-31" }, 
                            { "2012-01-01T22:22:22/2015-01-01T22:22:22" } });
        }

        private String[] validParamValues;

        private TimeParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new TimeParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "2012-01-01.345" }, { "55678.123456a 55778.123456" },
                    { "55678.123456 55778.123456a" }, { "2015/2014" }, { "2014/2015/" }, { "2015-01-10T31:21:21" },
                    { "2015-01-10T01:61:21" }, { "2015-01-10T01:21:60" }, { "201" } });
        }

        private String[] invalidParamValues;

        private TimeParamProcessor processor;

        public ValidateInvalidTest(Object invalidValue) throws Exception
        {
            processor = new TimeParamProcessor();

            if (invalidValue instanceof String[])
            {
                invalidParamValues = (String[]) invalidValue;
            }
            else if (invalidValue instanceof String)
            {
                invalidParamValues = new String[] { (String) invalidValue };
            }
        }

        @Test
        public void testInValidate()
        {
            assertThat(processor.validate("TIME", invalidParamValues),
                    contains("UsageFault: Invalid TIME value " + invalidParamValues[0]));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    @RunWith(Parameterized.class)
    public static class ValidateSpecificInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected error message prefix
            return Arrays.asList(new Object[][] { { "2015-00-15", "Invalid month in" },
                    { "2015-13-15", "Invalid month in" }, { "2015-12-00", "Invalid day in" },
                    { "2015-12-32", "Invalid day in" }, { "2015-12-40", "Invalid day in" } });

        }

        private TimeParamProcessor processor;

        private String invalidValue;

        private String expectedErrorMessageFragment;

        public ValidateSpecificInvalidTest(String invalidValue, String expectedErrorMessageFragment) throws Exception
        {
            this.invalidValue = invalidValue;
            this.expectedErrorMessageFragment = expectedErrorMessageFragment;
            processor = new TimeParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " TIME value " + invalidValue),
                    processor.validate("TIME", new String[] { invalidValue }));
        }
    }

    /**
     * Check the validate method's handling of invalid date range.
     */
    @RunWith(Parameterized.class)
    public static class ValidateRangeInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "2016/2015" } });
        }

        private String[] invalidParamValues;

        private TimeParamProcessor processor;

        public ValidateRangeInvalidTest(Object invalidValue) throws Exception
        {
            processor = new TimeParamProcessor();

            if (invalidValue instanceof String[])
            {
                invalidParamValues = (String[]) invalidValue;
            }
            else if (invalidValue instanceof String)
            {
                invalidParamValues = new String[] { (String) invalidValue };
            }
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testInValidateRange()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList(
                            "UsageFault: Invalid TIME value " + invalidParamValues[0]),
                    processor.validate("TIME", invalidParamValues));
        }
    }

    /**
     * Check the validate method's handling of valid date range.
     */
    @RunWith(Parameterized.class)
    public static class ValidateRangeValidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "2015/2016" }, { "2015-03-05/2015-05-05" },
                    { "2015-03-05/2015-03-05T18:00:00" }, { "2015-03-05T18:00:00/2015-03-05" } });
        }

        private String[] validParamValues;

        private TimeParamProcessor processor;

        public ValidateRangeValidTest(Object validValue) throws Exception
        {
            processor = new TimeParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testInValidateRange()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the BuildQuery method with valid values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { 
                { "/", "(t_min IS NOT NULL AND t_max IS NOT NULL)" },
                    { "2012-01-01", "(t_min <= 55927.0 AND t_max >= 55927.0)" },
                    { "2012-01-01/", "(t_max >= 55927.0)" },
                    { "/2012-01-01", "(t_min <= 55927.99998842592)" },
                    { "/2012-01", "(t_min <= 55957.99998842592)" },
                    { "2012-01-10T23:59:59", "(t_min <= 55936.99998842592 AND t_max >= 55936.99998842592)" },
                    { " 2012-01-01", "(t_min <= 55927.0 AND t_max >= 55927.0)" }, 
                    { "2012-01", "(t_min <= 55927.0 AND t_max >= 55927.0)" }, 
                    { " 2012-01-10T23:59:59", "(t_min <= 55936.99998842592 AND t_max >= 55936.99998842592)" },
                    { "2012-01-01/2014-01-01", "(t_min >= 55927.0 AND t_max <= 56658.99998842592)" },
                    { "2012-01-10T23:59:59/2014-01-10T21:59:59",
                            "(t_min >= 55936.99998842592 AND t_max <= 56667.916655092595)" } });
        }

        private String[] paramValues;
        private TimeParamProcessor processor;
        private String result;

        public BuildQueryTest(Object value, String result)
        {
            this.result = result;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
            processor = new TimeParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("t_min", "t_max", paramValues));
        }
    }
}
