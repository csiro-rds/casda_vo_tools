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
 * Verify the workings of BandParamProcessor.
 * <p>
 * Copyright 2016, CSIRO Australia All rights reserved.
 */
public class BandParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "2.7E-7/0.13" }),
                    Arguments.arguments((Object) new String[] { "0.2078/0.2256" }),
                    Arguments.arguments((Object) new String[] { "1/2;source" }),
                    Arguments.arguments((Object) new String[] { "1/2;observer" }),
                    Arguments.arguments((Object) new String[] { "1/2;" }),
                    Arguments.arguments((Object) new String[] { "0.21;observer" }),
                    Arguments.arguments((Object) new String[] { "/0.18;observer" }),
                    Arguments.arguments((Object) new String[] { "0.19/;observer" }),
                    Arguments.arguments((Object) new String[] { "0.21" }),
                    Arguments.arguments((Object) new String[] { "/0.18" }),
                    Arguments.arguments((Object) new String[] { "0.19/" }),
                    Arguments.arguments((Object) new String[] { "/" }),
                    Arguments.arguments((Object) new String[] { "0.11/0.12,0.18/0.22" }),
                    Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "0.19/0.21", "+0.24/0.245" }));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String[] validParamValues)
        {
            BandParamProcessor processor = new BandParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("BAND", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    public static class ValidateSpecificInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("300/600;foo", "Unsupported spectral rest frame in"),
                    Arguments.arguments("J", "Invalid"), Arguments.arguments("HI;source", "Invalid"),
                    Arguments.arguments("0/12;", "Invalid wavelength in"),
                    Arguments.arguments("-3/12;", "Invalid wavelength in"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue, String expectedErrorMessageFragment)
        {
            BandParamProcessor processor = new BandParamProcessor();
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " BAND value " + invalidValue),
                    processor.validate("BAND", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300" }, "(em_min <= 300 AND em_max >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/600" }, "(em_min <= 600 AND em_max >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/" }, "(em_max >= 300)"),
                    Arguments.arguments((Object) new String[] { "/600" }, "(em_min <= 600)"),
                    Arguments.arguments((Object) new String[] { "" }, ""),
                    Arguments.arguments((Object) new String[] { "+1E-2" }, "(em_min <= +1E-2 AND em_max >= +1E-2)"),
                    Arguments.arguments((Object) new String[] { "/" }, "(em_min IS NOT NULL AND em_max IS NOT NULL)"),
                    Arguments.arguments((Object) new String[] { "5e+2", "0.21" },
                            "(em_min <= 5e+2 AND em_max >= 5e+2) OR (em_min <= 0.21 AND em_max >= 0.21)"));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            BandParamProcessor processor = new BandParamProcessor();
            assertEquals("Incorrect result for BAND " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("em_min", "em_max", paramValues));
        }
    }

}
