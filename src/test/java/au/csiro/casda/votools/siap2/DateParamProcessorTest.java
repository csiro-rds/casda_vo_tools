package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
    public static class ValidateDateValidTest
    {

        // Param values (may be multiple)
        // TODO once the siap2 specs are finilised in regards to date formats this code can
        // either be reinstated or deleted
        /**
         * removed due to change in specs but kept in case of future changes return Arrays.asList( new Object[][] { {
         * "2012-01-01" }, { "2012-01-01T22:22:22" }, { "2012-01-01T22:22:22.1234" }, { "2012-01-01 2014-01-10" }, {
         * "2012-01-01T22:22:22 2015-01-01T22:22:22" }, { "2012-01-01T22:22:22.1234 2015-01-01T22:22:22.1234" } });
         **/
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "55678.123456" }),
                    Arguments.arguments((Object) new String[] { "55678.123456 55778.123456" }));
        }

        private DateParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidateDate(String[] validParamValues)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateDateInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2012-01-01.345" }),
                    Arguments.arguments((Object) new String[] { "55678.123456a 55778.123456" }),
                    Arguments.arguments((Object) new String[] { "55678.123456 55778.123456a" }));
        }

        private DateParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidateDate(String[] invalidParamValues)
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
    public static class ValidateDateRangeInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "65678.123456 55778.123456" }));
        }

        private DateParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidateDateRange(String[] invalidParamValues)
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.", Arrays.asList(
                    "UsageFault: The first date in your query must be earlier (chronoligically) than the second"),
                    processor.validate("TIME", invalidParamValues));
        }
    }

    /**
     * Check the validate method's handling of invalid date range.
     */
    public static class ValidateDateRangeValidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "45678.123456 55778.123456" }),
                    Arguments.arguments((Object) new String[] { "45678.123456 45678.123456" }));
        }

        private DateParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidateDateRange(String[] validParamValues)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the BuildQuery method with valid values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(
                    Arguments.arguments((Object) new String[] { "55936.99998842592" },
                            "(TIME <= 55936.99998842592 AND maxCol >= 55936.99998842592)"),
                    Arguments.arguments((Object) new String[] { " 55936.999999988424" },
                            "(TIME <= 55936.999999988424 AND maxCol >= 55936.999999988424)"),
                    Arguments.arguments((Object) new String[] { "55927.0 56658.999999" },
                            "(TIME >= 55927.0 AND maxCol <= 56658.999999)"),
                    Arguments.arguments((Object) new String[] { "  55927.0    56658.999999 " },
                            "(TIME >= 55927.0 AND maxCol <= 56658.999999)"));
        }

        private DateParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new DateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.DateParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithValidParams(String[] paramValues, String result)
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("TIME", "maxCol", paramValues));
        }
    }
}
