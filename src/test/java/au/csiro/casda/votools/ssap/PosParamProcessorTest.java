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
 * Verify the workings of PosParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class PosParamProcessorTest
{
    /**
     * Check the validateDouble method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300,89" }),
                    Arguments.arguments((Object) new String[] { "0,0" }),
                    Arguments.arguments((Object) new String[] { "124,-13;FK5" }),
                    Arguments.arguments((Object) new String[] { "+98,+27" }),
                    Arguments.arguments((Object) new String[] { "+98,+27;ICRS" }),
                    Arguments.arguments((Object) new String[] { "1.23E+2,6.33e+1" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8;Galactic" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8;GALACTIC" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8;GalacTIC-II" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8;GALACTIC-II" }),
                    Arguments.arguments((Object) new String[] { "235,+01.05;GALACTIC" }),
                    Arguments.arguments((Object) new String[] { "-25.65,-00.5;GALACTIC" }),
                    Arguments.arguments((Object) new String[] { "+165,-27.8;GALACTIC" }),
                    Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "53,-27.8", "+98,+27;ICRS" }));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidate(String[] validParamValues)
        {
            PosParamProcessor processor = new PosParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("POS", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of generically invalid values.
     */
    public static class ValidateGenericInvalidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments("a"), Arguments.arguments("300//"), Arguments.arguments("300/n"),
                    Arguments.arguments("/600/"), Arguments.arguments("2."), Arguments.arguments("300,NaN"),
                    Arguments.arguments("NaN,60"), Arguments.arguments("17,"), Arguments.arguments("-inf 600"),
                    Arguments.arguments("300 +inf"), Arguments.arguments("inf 600"), Arguments.arguments("300 inf"),
                    Arguments.arguments("2.3-01"), Arguments.arguments("2.3e"), Arguments.arguments("2.3e-"),
                    Arguments.arguments("2.3e 6"), Arguments.arguments("2.3 e-5"), Arguments.arguments("300 600 1300"));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidate(String invalidValue)
        {
            PosParamProcessor processor = new PosParamProcessor();
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

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments("300/600", "Ranges are not allowed in"),
                    Arguments.arguments("30/60;ICRS", "Ranges are not allowed in"),
                    Arguments.arguments("30/60,-23", "Ranges are not allowed in"),
                    Arguments.arguments("100", "Must have exactly two coordinate values in"),
                    Arguments.arguments("1,2,3", "Only 2 entries allowed in"),
                    Arguments.arguments("1,2,3,4", "Only 2 entries allowed in"),
                    Arguments.arguments("1,2;EXTRAGALACTC", "Unsupported coordinate system reference frame in"),
                    Arguments.arguments("1,2;ECLIPTIC", "Unsupported coordinate system reference frame in"),
                    Arguments.arguments("1,2;GALACTIC-I", "Unsupported coordinate system reference frame in"),
                    Arguments.arguments("1,2;Nonsense", "Unsupported coordinate system reference frame in"),
                    Arguments.arguments("1,2;FK4", "Unsupported coordinate system reference frame in"),
                    Arguments.arguments("361,0;FK5", "Invalid right ascension in"),
                    Arguments.arguments("-0.1,0;ICRS", "Invalid right ascension in"),
                    Arguments.arguments("361,0", "Invalid right ascension in"),
                    Arguments.arguments("15,91;FK5", "Invalid declination in"),
                    Arguments.arguments("+15,-91;ICRS", "Invalid declination in"),
                    Arguments.arguments("120,-120", "Invalid declination in"),
                    Arguments.arguments("10,-91;GALACTIC", "Invalid latitude in"),
                    Arguments.arguments("10,91;GALACTIC", "Invalid latitude in"),
                    Arguments.arguments("361,0;GALACTIC", "Invalid longitude in"),
                    Arguments.arguments("-181,0;GALACTIC", "Invalid longitude in"));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidate(String invalidValue, String expectedErrorMessageFragment)
        {
            PosParamProcessor processor = new PosParamProcessor();
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

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300,-52" }, ""),
                    Arguments.arguments((Object) new String[] { "5e+1,-1.25E+1", "120,-65" }, ""));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            PosParamProcessor processor = new PosParamProcessor();
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

    /**
     * Check the BuildQuery method with a set of values for a single field parameter.
     */
    public static class BuildQuerySingleFieldTest
    {
        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300,-52" }, ""),
                    Arguments.arguments((Object) new String[] { "5e+1,-1.25E+1" }, ""));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            PosParamProcessor processor = new PosParamProcessor();
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

    /**
     * Check the getRaDec method.
     */
    public static class GetRaDecTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments("300,-52", new double[] { 300, -52 }),
                    Arguments.arguments("5e+1,-1.25E+1", new double[] { 50, -12.5 }),
                    Arguments.arguments("5e+1,-1.25E+1;FK5", new double[] { 50, -12.5 }),
                    Arguments.arguments("5e+1,-1.25E+1;ICRS", new double[] { 50, -12.5 }),
                    Arguments.arguments("302.8084,-44.3277;GALACTIC", new double[] { 13.15835, -72.80032 }),
                    Arguments.arguments("-57.1916,-44.3277;GALACTIC-II", new double[] { 13.15835, -72.80032 }),
                    Arguments.arguments("0,0;GALACTIC", new double[] { 266.40496, -28.93618 }));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithDoubleRange(String value, double[] expectedRaDec)
        {
            PosParamProcessor processor = new PosParamProcessor();
            double[] raDec = processor.getRaDec(value);
            assertEquals("Incorrect Declination for " + value, expectedRaDec[1], raDec[1], 0.0001);
            assertEquals("Incorrect Right Ascension for " + value, expectedRaDec[0], raDec[0], 0.0001);
        }
    }

}
