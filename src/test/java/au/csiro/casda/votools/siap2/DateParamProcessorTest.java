package au.csiro.casda.votools.siap2;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
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
 * Verify the workings of DateParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class DateParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDateValidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            //TODO once the siap2 specs are finilised in regards to date formats this code can 
            //either be reinstated or deleted
            /** removed due to change in specs but kept in case of future changes
            return Arrays.asList(
                    new Object[][] { { "2012-01-01" }, { "2012-01-01T22:22:22" }, { "2012-01-01T22:22:22.1234" },
                            { "2012-01-01 2014-01-10" }, { "2012-01-01T22:22:22 2015-01-01T22:22:22" },
                            { "2012-01-01T22:22:22.1234 2015-01-01T22:22:22.1234" } });
            **/
            return Arrays.asList(new Object[][] { { "55678.123456" }, { "55678.123456 55778.123456" } });
        }

        private String[] validParamValues;

        private DateParamProcessor processor;

        public ValidateDateValidTest(Object validValue) throws Exception
        {
            processor = new DateParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateDate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDateInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "2012-01-01.345" }, { "55678.123456a 55778.123456" },
                    { "55678.123456 55778.123456a" } });
        }

        private String[] invalidParamValues;

        private DateParamProcessor processor;

        public ValidateDateInvalidTest(Object invalidValue) throws Exception
        {
            processor = new DateParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testInValidateDate()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Your query contained an invalid date format. "
                            + "This query accepts the MJD format, e.g.\t55678.123456"),
                    processor.validate("TIME", invalidParamValues));
        }
    }

    /**
     * Check the validate method's handling of invalid date range.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDateRangeInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "65678.123456 55778.123456" } });
        }

        private String[] invalidParamValues;

        private DateParamProcessor processor;

        public ValidateDateRangeInvalidTest(Object invalidValue) throws Exception
        {
            processor = new DateParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testInValidateDateRange()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList(
                            "UsageFault: The first date in your query must be earlier (chronoligically) than the second"),
                    processor.validate("TIME", invalidParamValues));
        }
    }

    /**
     * Check the validate method's handling of invalid date range.
     */
    @RunWith(Parameterized.class)
    public static class ValidateDateRangeValidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "45678.123456 55778.123456" }, { "45678.123456 45678.123456" } });
        }

        private String[] validParamValues;

        private DateParamProcessor processor;

        public ValidateDateRangeValidTest(Object validValue) throws Exception
        {
            processor = new DateParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testInValidateDateRange()
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

        @Parameters
        public static Collection<Object[]> data()
        {
            /**
            return Arrays.asList(new Object[][] { 
                    { "2012-01-01", "(TIME >= 55927.0)" },
                    { "2012-01-10T23:59:59", "(TIME >= 55936.99998842592)" },
                    { "2012-01-10T23:59:59.999", "(TIME >= 55936.999999988424)" },
                    { " 2012-01-01", "(TIME <= 55927.999999988424)" }, 
                    { " 2012-01-10T23:59:59", "(TIME <= 55936.999999988424)" },
                    { " 2012-01-10T23:59:59.999", "(TIME <= 55936.999999988424)" },
                    { "2012-01-01 2014-01-01", "(TIME >= 55927.0 AND maxCol <= 56658.999999988424)" },
                    { "2012-01-10T23:59:59 2014-01-10T21:59:59",
                            "(TIME >= 55936.99998842592 AND maxCol <= 56667.916666655095)" },
                    { "2012-01-10T23:59:59.999 2015-01-10T23:59:23",
                            "(TIME >= 55936.999999988424 AND maxCol <= 57032.99958332176)" } });
                            
            */
            return Arrays.asList(new Object[][] { 
                { "55936.99998842592", "(TIME <= 55936.99998842592 AND maxCol >= 55936.99998842592)" },
                { " 55936.999999988424", "(TIME <= 55936.999999988424 AND maxCol >= 55936.999999988424)" },
                { "55927.0 56658.999999", "(TIME >= 55927.0 AND maxCol <= 56658.999999)" },
                { "  55927.0    56658.999999 ", "(TIME >= 55927.0 AND maxCol <= 56658.999999)" }});
        }

        private String[] paramValues;
        private DateParamProcessor processor;
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
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("TIME", "maxCol", paramValues));
        }
    }
}
