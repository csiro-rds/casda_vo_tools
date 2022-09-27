package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
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
 * Validate the PositionParamProcessor class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class PositionParamProcessorTest
{

    /**
     * Check the validateDouble method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidatePositionValidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "CIRCLE 12.0 34.0 0.5" }, { "CIRCLE +182.5 -34.0 7.5" },
                    { "RANGE 12.0 12.5 34.0 36.0" }, { "RANGE 12.0 12.5 34.0 36.0" }, { "RANGE 0 360.0 -2.0 2.0" },
                    { "RANGE 0 360.0 89.0 +Inf" }, { "RANGE -Inf +Inf -Inf +Inf" },
                    { "POLYGON 12.0 34.0 14.0 35.0 14.0 36.0 12.0 35.0" },
                    { "POLYGON 112.0 34.0 118.0 36 118.0 -10.0 112.0 -10.0 89.0 0" },
                    { new String[] { "CIRCLE 276.7 -61.9 3.1667", "RANGE 297 305 -48 -55" } } });
        }

        private String[] validParamValues;

        private PositionParamProcessor processor;

        public ValidatePositionValidTest(Object validValue) throws Exception
        {
            processor = new PositionParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidatePosition()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("POS", validParamValues), is(empty()));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of values in the wrong format.
     */
    @RunWith(Parameterized.class)
    public static class ValidatePositionInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (only singles for this test)
            return Arrays.asList(new Object[][] { { "CIRCLE 12.0 34.0 0.5 7" }, { "CIRCLE +182.5 -34.0" },
                    { "CIRCLE -Inf 34.0 0.5" }, { "CIRCLE 34.0 +Inf 0.5" }, { "CIRCLE 34.0 0.5 NaN" },
                    { "RANGE 12.0 12.5 34.0" }, { "RANGE 12.0 12.5 34.0 36.0 8" }, { "RANE 0 360.0 -2.0 2.0" },
                    { "RANGE 0 360.0 89.0 Blah" }, { "RANGE NaN NaN NaN" }, { "RANGE 0 270 +Inf 40" },
                    { "RANGE 50.0/60.0 -24.0/-30.0" }, { "RANGE 320.9/321.9 55.54/55.56" },
                    { "POLYGON 12.0 34.0 14.0 35.0" }, { "POLYGON 112.0 34.0 118.0 36 118.0 -10.0 112.0 -10.0 89.0" },
                    { "POLYGON 12.0 34.0 14.0 35.0 NaN 17" }, { "POLYGON 12.0 34.0 14.0 35.0 17 NaN" } });
        }

        private String invalidParamValues;

        private PositionParamProcessor processor;

        public ValidatePositionInvalidTest(Object validValue) throws Exception
        {
            processor = new PositionParamProcessor();

            invalidParamValues = (String) validValue;
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidatePosition()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid POS value " + invalidParamValues),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid longitude (ra) values.
     */
    @RunWith(Parameterized.class)
    public static class ValidatePositionLongitudeTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (only singles for this test)
            return Arrays.asList(new Object[][] { { "CIRCLE -1.0 34.0 0.5" }, { "CIRCLE 360.001 -34.0 1" },
                    { "RANGE -199 12.5 34.0 35" }, { "RANGE 361.0 12.5 34.0 36.0" }, { "RANGE 0 1360.0 -2.0 2.0" },
                    { "POLYGON 412.0 34.0 14.0 35.0 5 5" }, { "POLYGON 112.0 34.0 -0.001 36 118.0 -10.0 112.0 -10.0" },
                    { "POLYGON 112.0 34.0 0.001 36 361.0 -10.0 112.0 -10.0" } });
        }

        private String invalidParamValues;

        private PositionParamProcessor processor;

        public ValidatePositionLongitudeTest(Object validValue) throws Exception
        {
            processor = new PositionParamProcessor();

            invalidParamValues = (String) validValue;
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateLongitude()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid longitude value. Valid range is [0,360]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid latitude (dec) values.
     */
    @RunWith(Parameterized.class)
    public static class ValidatePositionLatitudeTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (only singles for this test)
            return Arrays.asList(new Object[][] { { "CIRCLE 12.0 102.0 0.5" }, { "CIRCLE +182.5 -98.0 7.5" },
                    { "RANGE 12.0 12.5 134.0 36.0" }, { "RANGE 12.0 12.5 34.0 -95.0" }, { "RANGE 0 360.0 -2.0 432.0" },
                    { "POLYGON 12.0 134.0 14.0 35.0 14.0 36.0 12.0 35.0" },
                    { "POLYGON 112.0 34.0 118.0 -98 118.0 -10.0 112.0 -10.0 89.0 0" } });
        }

        private String invalidParamValues;

        private PositionParamProcessor processor;

        public ValidatePositionLatitudeTest(Object validValue) throws Exception
        {
            processor = new PositionParamProcessor();

            invalidParamValues = (String) validValue;
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateLatitude()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid latitude value. Valid range is [-90,90]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the PositionParamProcessor's handling of invalid circle radius values.
     */
    @RunWith(Parameterized.class)
    public static class ValidatePositionRadiusTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (only singles for this test)
            return Arrays.asList(new Object[][] { { "CIRCLE 12.0 34.0 -0.5" }, { "CIRCLE +182.5 -34.0 17.5" } });
        }

        private String invalidParamValues;

        private PositionParamProcessor processor;

        public ValidatePositionRadiusTest(Object validValue) throws Exception
        {
            processor = new PositionParamProcessor();

            invalidParamValues = (String) validValue;
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateRadius()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid radius value. Valid range is [0,10]"),
                    processor.validate("POS", new String[] { invalidParamValues }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] {
                    { "CIRCLE 12.0 34.0 0.5", "(INTERSECTS(CIRCLE('ICRS GEOCENTER', 12.0, 34.0, 0.5),s_region)=1)" },
                    { "RANGE 12.0 12.5 34.0 36.0",
                            "(INTERSECTS(BOX('ICRS GEOCENTER', 12.250000, 35.000000, 0.5, 2.0),s_region)=1)" },
                    { "RANGE -Inf +Inf -Inf +Inf",
                            "(INTERSECTS(BOX('ICRS GEOCENTER', 180.000000, 0.000000, 360, 180),s_region)=1)" },
                    {
                            "POLYGON 12.0 34.0 14.0 35.0 14.0 36.0 12.0 35.0",
                            "(INTERSECTS(POLYGON('ICRS GEOCENTER', 12.0, 34.0, 14.0, 35.0, 14.0, 36.0, 12.0, "
                                    + "35.0),s_region)=1)" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private PositionParamProcessor processor;

        public BuildQueryTest(Object value, String expectedWhereClause)
        {
            this.expectedAdql = expectedWhereClause;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
            processor = new PositionParamProcessor();
        }

        @Test
        public void testBuildQuery()
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

}
