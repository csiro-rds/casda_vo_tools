package au.csiro.casda.votools.siap1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Verify the workings of SizeParamProcessor.
 * <p>
 * Copyright 2022, CSIRO Australia All rights reserved.
 */
public class SizeParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {   
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{ "0.01" }),
                    Arguments.arguments((Object) new String[]{ "1E-2" }),
                    Arguments.arguments((Object) new String[]{ "0.2E+1" }),
                    Arguments.arguments((Object) new String[]{ "0.2e1" }),
                    Arguments.arguments((Object) new String[]{ "0.05" }),
                    Arguments.arguments((Object) new String[]{ "1" }),
                    Arguments.arguments((Object) new String[]{ "0" }),
                    Arguments.arguments((Object) new String[]{ "1.5,1.3" }));
        }

        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String [] validParamValues)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("SIZE", validParamValues), is(empty()));
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
                    Arguments.arguments("300 600 1300"),
                    Arguments.arguments("300/600"),
                    Arguments.arguments("30/60"),
                    Arguments.arguments("1;ICRS"));
        }
       
        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue)
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid SIZE value " + invalidValue),
                    processor.validate("SIZE", new String[] { invalidValue }));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    public static class ValidateSpecificInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("1,2,3", "Only 2 entries allowed in"),
                    Arguments.arguments("1,2,3,4", "Only 2 entries allowed in"),
                    Arguments.arguments("-1", "Value must be between 0 and 2.0 in"),
                    Arguments.arguments("15.001", "Value must be between 0 and 2.0 in"),
                    Arguments.arguments("1.6E1", "Value must be between 0 and 2.0 in"),
                    Arguments.arguments("", "Either one or two numbers must be provided in"));
        }

        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue, String expectedErrorMessageFragment)
        {
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
            return Stream.of(Arguments.arguments((Object) new String[]{ "10" }, ""),
                    Arguments.arguments((Object) new String[]{ "1.05e+1", "2.1" }, ""));
        }

        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String [] value, String expectedAdql)
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(value), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", value));
        }
    }

    /**
     * Check the BuildQuery method with a set of values for a single field parameter.
     */
    public static class BuildQuerySingleFieldTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{ "10" }, ""),
                    Arguments.arguments((Object) new String[]{ "1.05e+1", "2.1" }, ""));
        }

        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String [] paramValues, String expectedAdql)
        {
            assertEquals("Incorrect result for size " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

    /**
     * Verify validation rejects multiple SIZE parameters.
     */
    public static class ValidateMultipleInvalidTest
    {
        @Test
        public void testWithDoubleRange()
        {
            SizeParamProcessor processor = new SizeParamProcessor();
            String[] invalidValue = new String[] { "1.5", "1.3" };
            assertEquals("Expected '" + Arrays.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Only one set of SIZE values may be provided."),
                    processor.validate("SIZE", invalidValue));
        }

    }

    /**
     * Check the getSizeDegrees method's handling of valid values.
     */
    public static class GetSizeDegreesTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"0.01"}, new double[] { 0.01, 0.01 }),
                    Arguments.arguments((Object) new String[]{"1E-2"},  new double[] { 0.01, 0.01 }),
                    Arguments.arguments((Object) new String[]{"0.2E+1"}, new double[] { 2.0, 2.0 }),
                    Arguments.arguments((Object) new String[]{"0.2e1"}, new double[] { 2.0, 2.0 }),
                    Arguments.arguments((Object) new String[]{"0.05"}, new double[] { 0.05, 0.05 }),
                    Arguments.arguments((Object) new String[]{"1"}, new double[] { 1.0, 1.0 }),
                    Arguments.arguments((Object) new String[]{"0"}, new double[] { 0.2, 0.2 }),
                    Arguments.arguments((Object) new String[]{"1.5,1.3"}, new double[] { 1.5, 1.3 }));
        }

        private SizeParamProcessor processor;

        @BeforeEach
        public void setup() throws Exception
        {
            processor = new SizeParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testGetSizeDegrees(String [] validParamValues, double[] expectedValue)
        {

            double[] sizeDegrees = processor.getSizeDegrees(validParamValues);
            assertThat("Unexpected width.", sizeDegrees[0], is(closeTo(expectedValue[0], 1e-5)));
            assertThat("Unexpected height.", sizeDegrees[1], is(closeTo(expectedValue[1], 1e-5)));
        }
    }

    /**
     * Check the getSizeDegrees method's handling of valid values.
     */
    public static class GetSearchRadiusTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"0.01"}, 0.005d),
                    Arguments.arguments((Object) new String[]{"1E-2"},  0.005d),
                    Arguments.arguments((Object) new String[]{"0.2E+1"}, 1.0d),
                    Arguments.arguments((Object) new String[]{"0.2e1"}, 1.0d),
                    Arguments.arguments((Object) new String[]{"0.05"}, 0.025d),
                    Arguments.arguments((Object) new String[]{"1"}, 0.5d),
                    Arguments.arguments((Object) new String[]{"0"}, 0.1d),
                    Arguments.arguments((Object) new String[]{"1.5,1.3"}, 0.75d));
        }
        
        private SizeParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SizeParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testGetSizeDegrees(String [] validValue, double expectedValue)
        {

            double radius = processor.getSearchRadius(validValue);
            assertThat("Unexpected radius.", radius, is(closeTo(expectedValue, 1e-5)));
        }
    }
}
