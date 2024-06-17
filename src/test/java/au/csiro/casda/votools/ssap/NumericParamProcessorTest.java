package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
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
 * Verify the workings of NumericParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class NumericParamProcessorTest
{
    /**
     * Check the validateDouble method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300" }),
                    Arguments.arguments((Object) new String[] { "300/600" }),
                    Arguments.arguments((Object) new String[] { "300/" }),
                    Arguments.arguments((Object) new String[] { "/600" }),
                    Arguments.arguments((Object) new String[] { "/" }),
                    Arguments.arguments((Object) new String[] { "0.21" }),
                    Arguments.arguments((Object) new String[] { "3.5e-1" }),
                    Arguments.arguments((Object) new String[] { "3.5e1" }),
                    Arguments.arguments((Object) new String[] { "3.5e+1" }),
                    Arguments.arguments((Object) new String[] { "3.5e-1/4e-1" }),
                    Arguments.arguments((Object) new String[] { "3.5E-1" }),
                    Arguments.arguments((Object) new String[] { "3.5E1" }),
                    Arguments.arguments((Object) new String[] { "3.5E+1" }),
                    Arguments.arguments((Object) new String[] { "3.5E-1/4E-1" }),
                    Arguments.arguments((Object) new String[] { "3.5e-1/4E-1" }),
                    Arguments.arguments((Object) new String[] { "0.21,0.28" }),
                    Arguments.arguments((Object) new String[] { "3.5e-1,0.12" }),
                    Arguments.arguments((Object) new String[] { "3.5e-1,0.12,999,1.001" }),
                    Arguments.arguments((Object) new String[] { "0.21/0.22,0.13/0.135" }),
                    Arguments.arguments((Object) new String[] { "52,-27.8;Galactic" }),
                    Arguments.arguments((Object) new String[] { "+0.21/+0.215;Source" }),
                    Arguments.arguments((Object) new String[] { " 0.21" }),
                    Arguments.arguments((Object) new String[] { "0.21 " }),
                    Arguments.arguments((Object) new String[] { " 0.21 " }),
                    Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "500", "0.21" }), Arguments.arguments(
                            (Object) new String[] { "5e-9", "2.1e-01", "2.1E-01", "/5e-9", "5e-9/", "5e-9/2.05e+2" }));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidate(String[] validParamValues)
        {
            NumericParamProcessor processor = new NumericParamProcessor(1000, true,
                    Arrays.asList(new String[] { "GALACTIC", "SOURCE" }), "qualifier");
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("BAND", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validateDouble method's handling of invalid values.
     */
    public static class ValidateInvalidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments("a"), Arguments.arguments("300\\600"), Arguments.arguments("300//"),
                    Arguments.arguments("300/n"), Arguments.arguments("/600/"), Arguments.arguments("2."),
                    Arguments.arguments("300 NaN"), Arguments.arguments("NaN 600"), Arguments.arguments("NaN NaN"),
                    Arguments.arguments("+Inf 600"), Arguments.arguments("300 -Inf"), Arguments.arguments("+Inf -Inf"),
                    Arguments.arguments("-inf 600"), Arguments.arguments("300 +inf"), Arguments.arguments("inf 600"),
                    Arguments.arguments("300 inf"), Arguments.arguments("2.3-01"), Arguments.arguments("2.3e"),
                    Arguments.arguments("2.3e-"), Arguments.arguments("2.3e 6"), Arguments.arguments("2.3 e-5"),
                    Arguments.arguments("300 600 1300"), Arguments.arguments("600/300"));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidateDouble(String invalidValue)
        {
            NumericParamProcessor processor =
                    new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid BAND value " + invalidValue),
                    processor.validate("BAND", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "300" }, "(minCol <= 300 AND maxCol >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/600" }, "(minCol <= 600 AND maxCol >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/" }, "(maxCol >= 300)"),
                    Arguments.arguments((Object) new String[] { "/600" }, "(minCol <= 600)"),
                    Arguments.arguments((Object) new String[] { "" }, ""),
                    Arguments.arguments((Object) new String[] { "+1E-2" }, "(minCol <= +1E-2 AND maxCol >= +1E-2)"),
                    Arguments.arguments((Object) new String[] { "/" }, "(minCol IS NOT NULL AND maxCol IS NOT NULL)"),
                    Arguments.arguments((Object) new String[] { "5e+2", "0.21" },
                            "(minCol <= 5e+2 AND maxCol >= 5e+2) OR (minCol <= 0.21 AND maxCol >= 0.21)"));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            NumericParamProcessor processor = new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
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
            return Stream.of(Arguments.arguments((Object) new String[] { "300" }, "(s_fov <= 300 AND s_fov >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/600" }, "(s_fov <= 600 AND s_fov >= 300)"),
                    Arguments.arguments((Object) new String[] { "300/" }, "(s_fov >= 300)"),
                    Arguments.arguments((Object) new String[] { "/600" }, "(s_fov <= 600)"),
                    Arguments.arguments((Object) new String[] { "" }, ""),
                    Arguments.arguments((Object) new String[] { "/" }, "(s_fov IS NOT NULL AND s_fov IS NOT NULL)"),
                    Arguments.arguments((Object) new String[] { "5e+2", "0.21" },
                            "(s_fov <= 5e+2 AND s_fov >= 5e+2) OR (s_fov <= 0.21 AND s_fov >= 0.21)"));
        }
        
        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithDoubleRange(String[] paramValues, String expectedAdql)
        {
            NumericParamProcessor processor = new NumericParamProcessor(1000, true, Collections.emptyList(), "qualifier");
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

}
