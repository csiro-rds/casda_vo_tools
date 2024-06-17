package au.csiro.casda.votools.siap1;

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
 * Verify the workings of PosParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class PosParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> validQueryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300,89" }),
                    Arguments.arguments((Object) new String[] { "0,0" }),
                    Arguments.arguments((Object) new String[] { "124,-13" }),
                    Arguments.arguments((Object) new String[] { "+98,+27" }),
                    Arguments.arguments((Object) new String[] { "1.23E+2,6.33e+1" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8" }),
                    Arguments.arguments((Object) new String[] { "235,+01.05" }),
                    Arguments.arguments((Object) new String[] { "25.65,-00.5" }),
                    Arguments.arguments((Object) new String[] { "+165,-27.8" }),
                    Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "53,-27.8", "+98,+27" }));
        }

        private PosParamProcessor processor;

        @BeforeEach
        private void setup()
        {
            processor = new PosParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("validQueryParams")
        public void testValidate(String[] params)
        {
            assertThat("Expected '" + ArrayUtils.toString(params) + "' to be valid.", processor.validate("POS", params),
                    is(empty()));
        }
    }

    /**
     * Check the validate method's handling of generically invalid values.
     */
    public static class ValidateGenericInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("a"), Arguments.arguments("300//"), Arguments.arguments("300/n"),
                    Arguments.arguments("/600/"), Arguments.arguments("2."), Arguments.arguments("300,NaN"),
                    Arguments.arguments("NaN,60"), Arguments.arguments("17,"), Arguments.arguments("-inf 600"),
                    Arguments.arguments("300 +inf"), Arguments.arguments("inf 600"), Arguments.arguments("300 inf"),
                    Arguments.arguments("2.3-01"), Arguments.arguments("2.3e"), Arguments.arguments("2.3e-"),
                    Arguments.arguments("2.3e 6"), Arguments.arguments("2.3 e-5"), Arguments.arguments("300 600 1300"),
                    Arguments.arguments("300/600"), Arguments.arguments("30/60,-23"), Arguments.arguments("1,2;FK5"));
        }

        private PosParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new PosParamProcessor();
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
            return Stream.of(Arguments.arguments("100", "Must have exactly two coordinate values in"),
                    Arguments.arguments("1,2,3", "Only 2 entries allowed in"),
                    Arguments.arguments("1,2,3,4", "Only 2 entries allowed in"),
                    Arguments.arguments("361,0", "Invalid right ascension in"),
                    Arguments.arguments("-0.1,0", "Invalid right ascension in"),
                    Arguments.arguments("15,91", "Invalid declination in"),
                    Arguments.arguments("+15,-91", "Invalid declination in"),
                    Arguments.arguments("120,-120", "Invalid declination in"));
        }

        private PosParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new PosParamProcessor();
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
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " POS value " + invalidValue),
                    processor.validate("POS", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300,-52" }, ""),
                    Arguments.arguments((Object) new String[] { "5e+1,-1.25E+1", "120,-65" }, ""));
        }

        private PosParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new PosParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithMinMaxFields(String[] params, String expectedAdql)
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(params), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", params));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithSingleField(String[] params, String expectedAdql)
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(params), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", params));
        }
    }

    /**
     * Check the getRaDec method.
     */
    public static class GetRaDecTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("300,-52", new double[] { 300, -52 }),
                    Arguments.arguments("5e+1,-1.25E+1", new double[] { 50, -12.5 }),
                    Arguments.arguments("302.8084,-44.3277", new double[] { 302.8084, -44.3277 }));
        }

        private PosParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new PosParamProcessor();
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String value, double[] expectedRaDec)
        {
            double[] raDec = processor.getRaDec(value);
            assertEquals("Incorrect Declination for " + value, expectedRaDec[1], raDec[1], 0.0001);
            assertEquals("Incorrect Right Ascension for " + value, expectedRaDec[0], raDec[0], 0.0001);
        }
    }

}
