package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
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
 * Verify the workings of TimeParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class TimeParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2012-01-01" }),
                    Arguments.arguments((Object) new String[] { "2012-01-01T22:22:22" }),
                    Arguments.arguments((Object) new String[] { "2012-01-01/2014-01-10" }),
                    Arguments.arguments((Object) new String[] { "2012/2013" }),
                    Arguments.arguments((Object) new String[] { "2012-01-01/" }),
                    Arguments.arguments((Object) new String[] { "/2014-01-10" }),
                    Arguments.arguments((Object) new String[] { "/" }),
                    Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "2012-01" }),
                    Arguments.arguments((Object) new String[] { "2012-02" }),
                    Arguments.arguments((Object) new String[] { "2012" }),
                    Arguments.arguments((Object) new String[] { "2999-12-31" }),
                    Arguments.arguments((Object) new String[] { "2012-01-01T22:22:22/2015-01-01T22:22:22" }));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String[] validParamValues)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("TIME", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateInvalidTest
    {
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2012-01-01.345" }),
                    Arguments.arguments((Object) new String[] { "55678.123456a 55778.123456" }),
                    Arguments.arguments((Object) new String[] { "55678.123456 55778.123456a" }),
                    Arguments.arguments((Object) new String[] { "2015/2014" }),
                    Arguments.arguments((Object) new String[] { "2014/2015/" }),
                    Arguments.arguments((Object) new String[] { "2015-01-10T31:21:21" }),
                    Arguments.arguments((Object) new String[] { "2015-01-10T01:61:21" }),
                    Arguments.arguments((Object) new String[] { "2015-01-10T01:21:60" }),
                    Arguments.arguments((Object) new String[] { "201" }));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidate(String[] invalidParamValues)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
            assertThat(processor.validate("TIME", invalidParamValues),
                    contains("UsageFault: Invalid TIME value " + invalidParamValues[0]));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    public static class ValidateSpecificInvalidTest
    {
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("2015-00-15", "Invalid month in"),
                    Arguments.arguments("2015-13-15", "Invalid month in"),
                    Arguments.arguments("2015-12-00", "Invalid day in"),
                    Arguments.arguments("2015-12-32", "Invalid day in"),
                    Arguments.arguments("2015-12-40", "Invalid day in"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue, String expectedErrorMessageFragment)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " TIME value " + invalidValue),
                    processor.validate("TIME", new String[] { invalidValue }));
        }
    }

    /**
     * Check the validate method's handling of invalid date range.
     */
    public static class ValidateRangeInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2016/2015" }));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidateRange(String[] invalidParamValues)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid TIME value " + invalidParamValues[0]),
                    processor.validate("TIME", invalidParamValues));
        }
    }

    /**
     * Check the validate method's handling of valid date range.
     */
    public static class ValidateRangeValidTest
    {
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2015/2016" }),
                    Arguments.arguments((Object) new String[] { "2015-03-05/2015-05-05" }),
                    Arguments.arguments((Object) new String[] { "2015-03-05/2015-03-05T18:00:00" }),
                    Arguments.arguments((Object) new String[] { "2015-03-05T18:00:00/2015-03-05" }));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testInValidateRange(String[] validParamValues)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
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
                    Arguments.arguments((Object) new String[] { "/" }, "(t_min IS NOT NULL AND t_max IS NOT NULL)"),
                    Arguments.arguments((Object) new String[] { "2012-01-01" },
                            "(t_min <= 55927.0 AND t_max >= 55927.0)"),
                    Arguments.arguments((Object) new String[] { "2012-01-01/" }, "(t_max >= 55927.0)"),
                    Arguments.arguments((Object) new String[] { "/2012-01" }, "(t_min <= 55957.99998842592)"),
                    Arguments.arguments((Object) new String[] { "2012-01-10T23:59:59" },
                            "(t_min <= 55936.99998842592 AND t_max >= 55936.99998842592)"),
                    Arguments.arguments((Object) new String[] { " 2012-01-01" },
                            "(t_min <= 55927.0 AND t_max >= 55927.0)"),
                    Arguments.arguments((Object) new String[] { "2012-01" }, "(t_min <= 55927.0 AND t_max >= 55927.0)"),
                    Arguments.arguments((Object) new String[] { " 2012-01-10T23:59:59" },
                            "(t_min <= 55936.99998842592 AND t_max >= 55936.99998842592)"),
                    Arguments.arguments((Object) new String[] { "2012-01-01/2014-01-01" },
                            "(t_min >= 55927.0 AND t_max <= 56658.99998842592)"),
                    Arguments.arguments((Object) new String[] { "2012-01-10T23:59:59/2014-01-10T21:59:59" },
                            "(t_min >= 55936.99998842592 AND t_max <= 56667.916655092595)"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TimeParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithValidParams(String[] paramValues, String result)
        {
            TimeParamProcessor processor = new TimeParamProcessor();
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("t_min", "t_max", paramValues));
        }
    }
}
