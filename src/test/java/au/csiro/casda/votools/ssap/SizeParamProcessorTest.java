package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
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
 * Verify the workings of SizeParamProcessor.
 * <p>
 * Copyright 2016, CSIRO Australia All rights reserved.
 */
public class SizeParamProcessorTest
{
    /**
     * Check the validateDouble method's handling of valid values.
     */
    public static class ValidateValidTest
    {
        
        public static Stream<Arguments> validQueryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"0.01"}),
                    Arguments.arguments((Object) new String[]{"1E-2"}),
                    Arguments.arguments((Object) new String[]{"1.5E+1"}),
                    Arguments.arguments((Object) new String[]{"1.5e1"}),
                    Arguments.arguments((Object) new String[]{"0.05"}),
                    Arguments.arguments((Object) new String[]{"7"}),
                    Arguments.arguments((Object) new String[]{""}),
                    Arguments.arguments((Object) new String[]{ "5", "3" }));
        }

        @ParameterizedTest
        @MethodSource("validQueryParams")
        public void testValidate(String[] validParamValues)
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("POS", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of generically invalid values.
     */
    public static class ValidateGenericInvalidTest
    {
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("a"),
                    Arguments.arguments("300//"),
                    Arguments.arguments("300/n"),
                    Arguments.arguments("/600/"),
                    Arguments.arguments("2."),
                    Arguments.arguments("300,NaN"),
                    Arguments.arguments("NaN,60"),
                    Arguments.arguments("17,"),
                    Arguments.arguments("-inf,600"),
                    Arguments.arguments("300,+inf"),
                    Arguments.arguments("inf,600"),
                    Arguments.arguments("300,inf"),
                    Arguments.arguments("2.3-01"),
                    Arguments.arguments("2.3e"),
                    Arguments.arguments("2.3e-"),
                    Arguments.arguments("2.3e 6"),
                    Arguments.arguments("2.3 e-5"),
                    Arguments.arguments("300 600 1300"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue)
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid POS value " + invalidValue),
                    processor.validate("POS", new String[] { invalidValue }));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    public static class ValidateSpecificInvalidTest
    {
        
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("300/600", "Ranges are not allowed in"),
                    Arguments.arguments("30/60", "Ranges are not allowed in"),
                    Arguments.arguments("10,2", "Only 1 entry allowed in"),
                    Arguments.arguments("1,2,3", "Only 1 entry allowed in"),
                    Arguments.arguments("1,2,3,4", "Only 1 entry allowed in"),
                    Arguments.arguments("1;EXTRAGALACTC", "Unsupported qualifier in"),
                    Arguments.arguments("1;source", "Unsupported qualifier in"),
                    Arguments.arguments("1.5;Nonsense", "Unsupported qualifier in"),
                    Arguments.arguments("0", "Value must be between 0 and 15 in"),
                    Arguments.arguments("-1", "Value must be between 0 and 15 in"),
                    Arguments.arguments("15.001", "Value must be between 0 and 15 in"),
                    Arguments.arguments("1.6E1", "Value must be between 0 and 15 in"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue, String expectedErrorMessageFragment)
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " SIZE value " + invalidValue),
                    processor.validate("SIZE", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {
        
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"10"}, ""),
                    Arguments.arguments((Object) new String[]{"1.05e+1", "2.1"}, ""));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

    /**
     * Check the BuildQuery method with a set of values for a single field parameter.
     */
    public static class BuildQuerySingleFieldTest
    {
        
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"10"}, ""),
                    Arguments.arguments((Object) new String[]{"1.05e+1", "2.1"}, ""));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            assertEquals("Incorrect result for size " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

}
