package au.csiro.casda.votools.ssap;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
 * Verify the workings of BandParamProcessor.
 * <p>
 * Copyright 2016, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class BandParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateValidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "2.7E-7/0.13" }, { "0.2078/0.2256" }, { "1/2;source" },
                    { "1/2;observer" }, { "1/2;" }, { "0.21;observer" }, { "/0.18;observer" }, { "0.19/;observer" },
                    { "0.21" }, { "/0.18" }, { "0.19/" }, { "/" }, { "0.11/0.12,0.18/0.22" }, { "" },
                    { new String[] { "0.19/0.21", "+0.24/0.245" } } });
        }

        private String[] validParamValues;

        private BandParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new BandParamProcessor();

            if (validValue instanceof String[])
            {
                validParamValues = (String[]) validValue;
            }
            else if (validValue instanceof String)
            {
                validParamValues = new String[] { (String) validValue };
            }
        }

        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("BAND", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    @RunWith(Parameterized.class)
    public static class ValidateSpecificInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected error message prefix
            return Arrays.asList(new Object[][] { { "300/600;foo", "Unsupported spectral rest frame in" },
                    { "J", "Invalid" }, { "HI;source", "Invalid" }, { "0/12;", "Invalid wavelength in" },
                    { "-3/12;", "Invalid wavelength in" } });

        }

        private BandParamProcessor processor;

        private String invalidValue;

        private String expectedErrorMessageFragment;

        public ValidateSpecificInvalidTest(String invalidValue, String expectedErrorMessageFragment) throws Exception
        {
            this.invalidValue = invalidValue;
            this.expectedErrorMessageFragment = expectedErrorMessageFragment;
            processor = new BandParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " BAND value " + invalidValue),
                    processor.validate("BAND", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] { { "300", "(em_min <= 300 AND em_max >= 300)" },
                    { "300/600", "(em_min <= 600 AND em_max >= 300)" }, { "300/", "(em_max >= 300)" },
                    { "/600", "(em_min <= 600)" }, { "", "" }, { "+1E-2", "(em_min <= +1E-2 AND em_max >= +1E-2)" },
                    { "/", "(em_min IS NOT NULL AND em_max IS NOT NULL)" }, { new String[] { "5e+2", "0.21" },
                            "(em_min <= 5e+2 AND em_max >= 5e+2) OR (em_min <= 0.21 AND em_max >= 0.21)" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private BandParamProcessor processor;

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
            processor = new BandParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for BAND " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("em_min", "em_max", paramValues));
        }
    }

}
