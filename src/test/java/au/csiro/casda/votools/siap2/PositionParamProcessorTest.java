package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
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
 * Validate the PositionParamProcessor class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class PositionParamProcessorTest
{

    /**
     * Check the validateDouble method's handling of valid values.
     */
    public static class ValidatePositionValidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "CIRCLE 12.0 34.0 0.5" }),
                    Arguments.arguments((Object) new String[] { "CIRCLE +182.5 -34.0 7.5" }),
                    Arguments.arguments((Object) new String[] { "RANGE 12.0 12.5 34.0 36.0" }),
                    Arguments.arguments((Object) new String[] { "RANGE 12.0 12.5 34.0 36.0" }),
                    Arguments.arguments((Object) new String[] { "RANGE 0 360.0 -2.0 2.0" }),
                    Arguments.arguments((Object) new String[] { "RANGE 0 360.0 89.0 +Inf" }),
                    Arguments.arguments((Object) new String[] { "RANGE -Inf +Inf -Inf +Inf" }),
                    Arguments.arguments((Object) new String[] { "POLYGON 12.0 34.0 14.0 35.0 14.0 36.0 12.0 35.0" }),
                    Arguments.arguments(
                            (Object) new String[] { "POLYGON 112.0 34.0 118.0 36 118.0 -10.0 112.0 -10.0 89.0 0" }),
                    Arguments
                            .arguments((Object) new String[] { "CIRCLE 276.7 -61.9 3.1667", "RANGE 297 305 -48 -55" }));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidatePosition(String[] validParamValues)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("POS", validParamValues), is(empty()));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of values in the wrong format.
     */
    public static class ValidatePositionInvalidTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("CIRCLE 12.0 34.0 0.5 7"), Arguments.arguments("CIRCLE +182.5 -34.0"),
                    Arguments.arguments("CIRCLE -Inf 34.0 0.5"), Arguments.arguments("CIRCLE 34.0 +Inf 0.5"),
                    Arguments.arguments("CIRCLE 34.0 0.5 NaN"), Arguments.arguments("RANGE 12.0 12.5 34.0"),
                    Arguments.arguments("RANGE 12.0 12.5 34.0 36.0 8"), Arguments.arguments("RANE 0 360.0 -2.0 2.0"),
                    Arguments.arguments("RANGE 0 360.0 89.0 Blah"), Arguments.arguments("RANGE NaN NaN NaN"),
                    Arguments.arguments("RANGE 0 270 +Inf 40"), Arguments.arguments("RANGE 50.0/60.0 -24.0/-30.0"),
                    Arguments.arguments("RANGE 320.9/321.9 55.54/55.56"),
                    Arguments.arguments("POLYGON 12.0 34.0 14.0 35.0"),
                    Arguments.arguments("POLYGON 112.0 34.0 118.0 36 118.0 -10.0 112.0 -10.0 89.0"),
                    Arguments.arguments("POLYGON 12.0 34.0 14.0 35.0 NaN 17"),
                    Arguments.arguments("POLYGON 12.0 34.0 14.0 35.0 17 NaN"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidatePosition(String invalidParamValues)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid POS value " + invalidParamValues),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid longitude (ra) values.
     */
    public static class ValidatePositionLongitudeTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("CIRCLE -1.0 34.0 0.5"), Arguments.arguments("CIRCLE 360.001 -34.0 1"),
                    Arguments.arguments("RANGE -199 12.5 34.0 35"), Arguments.arguments("RANGE 361.0 12.5 34.0 36.0"),
                    Arguments.arguments("RANGE 0 1360.0 -2.0 2.0"),
                    Arguments.arguments("POLYGON 412.0 34.0 14.0 35.0 5 5"),
                    Arguments.arguments("POLYGON 112.0 34.0 -0.001 36 118.0 -10.0 112.0 -10.0"),
                    Arguments.arguments("POLYGON 112.0 34.0 0.001 36 361.0 -10.0 112.0 -10.0"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidateLongitude(String invalidParamValues)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid longitude value. Valid range is [0,360]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid latitude (dec) values.
     */
    public static class ValidatePositionLatitudeTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("CIRCLE 12.0 102.0 0.5"),
                    Arguments.arguments("CIRCLE +182.5 -98.0 7.5"), Arguments.arguments("RANGE 12.0 12.5 134.0 36.0"),
                    Arguments.arguments("RANGE 12.0 12.5 34.0 -95.0"), Arguments.arguments("RANGE 0 360.0 -2.0 432.0"),
                    Arguments.arguments("POLYGON 12.0 134.0 14.0 35.0 14.0 36.0 12.0 35.0"),
                    Arguments.arguments("POLYGON 112.0 34.0 118.0 -98 118.0 -10.0 112.0 -10.0 89.0 0"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidateLatitude(String invalidParamValues)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid latitude value. Valid range is [-90,90]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid circle radius values.
     */
    public static class ValidatePositionRadiusTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments("CIRCLE 12.0 34.0 -0.5"),
                    Arguments.arguments("CIRCLE +182.5 -34.0 17.5"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidateRadius(String invalidParamValues)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid radius value. Valid range is [0,10]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(
                    Arguments.arguments((Object) new String[] { "CIRCLE 12.0 34.0 0.5" },
                            "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 12.0, 34.0, 0.5),s_region)=1)"),
                    Arguments.arguments((Object) new String[] { "RANGE 12.0 12.5 34.0 36.0" },
                            "(INTERSECTS(BOX('ICRS GEOCENTER', 12.250000, 35.000000, 0.5, 2.0),s_region)=1)"),
                    Arguments.arguments((Object) new String[] { "RANGE -Inf +Inf -Inf +Inf" },
                            "(INTERSECTS(BOX('ICRS GEOCENTER', 180.000000, 0.000000, 360, 180),s_region)=1)"),
                    Arguments.arguments((Object) new String[] { "POLYGON 12.0 34.0 14.0 35.0 14.0 36.0 12.0 35.0" },
                            "(INTERSECTS(POLYGON('ICRS GEOCENTER', "
                                    + "12.0, 34.0, 14.0, 35.0, 14.0, 36.0, 12.0, 35.0),s_region)=1)"));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testBuildQuery(String[] paramValues, String expectedAdql)
        {
            PositionParamProcessor processor = new PositionParamProcessor();
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

    /**
     * Tests for the buildDistanceFunction method.
     */
    public static class BuildDistanceFunctionTest
    {
        private PositionParamProcessor processor = new PositionParamProcessor();

        @Test
        public void testNotApplicableCriteria()
        {
            assertEquals("", processor.buildDistanceFunction(null));
            assertEquals("", processor.buildDistanceFunction(new String[] {}));
            assertEquals("", processor.buildDistanceFunction(new String[] { "RANGE 1 2 3 4" }));
            assertEquals("", processor.buildDistanceFunction(new String[] { "foo" }));
            assertEquals("", processor.buildDistanceFunction(new String[] { "CIRCLE 1 -2 0.1", "CIRCLE 5 -3 0.2" }));
        }

        @Test
        public void testCirclePosition()
        {
            assertEquals("DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),POINT('ICRS GEOCENTER',1, -2))",
                    processor.buildDistanceFunction(new String[] { "CIRCLE 1 -2 0.1" }));
        }
    }
}
