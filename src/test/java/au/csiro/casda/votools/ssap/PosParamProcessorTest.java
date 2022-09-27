package au.csiro.casda.votools.ssap;

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
 * Verify the workings of PosParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class PosParamProcessorTest
{
    /**
     * Check the validateDouble method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateValidTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "300,89" }, { "0,0" }, { "124,-13;FK5" }, { "+98,+27" },
                    { "+98,+27;ICRS" }, { "1.23E+2,6.33e+1" }, { "52,-27.8;Galactic" }, { "52,-27.8;GALACTIC" },
                    { "52,-27.8;GalacTIC-II" }, { "52,-27.8;GALACTIC-II" }, { "235,+01.05;GALACTIC" },
                    { "-25.65,-00.5;GALACTIC" }, { "+165,-27.8;GALACTIC" }, { "" },
                    { new String[] { "53,-27.8", "+98,+27;ICRS" } } });
        }

        private String[] validParamValues;

        private PosParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new PosParamProcessor();

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
                    processor.validate("POS", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of generically invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateGenericInvalidTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "a" }, { "300//" }, { "300/n" }, { "/600/" }, { "2." },
                    { "300,NaN" }, { "NaN,60" }, { "17," },
                    { "-inf 600" }, { "300 +inf" }, { "inf 600" }, { "300 inf" }, { "2.3-01" }, { "2.3e" }, { "2.3e-" },
                    { "2.3e 6" }, { "2.3 e-5" }, { "300 600 1300" } });
        }

        private PosParamProcessor processor;

        private String invalidValue;

        public ValidateGenericInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new PosParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid POS value " + invalidValue),
                    processor.validate("POS", new String[] { invalidValue }));
        }
    }

    /**
     * Check the validate method's handling of invalid values with specific error messages.
     */
    @RunWith(Parameterized.class)
    public static class ValidateSpecificInvalidTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected error message prefix
            return Arrays.asList(new Object[][] { { "300/600", "Ranges are not allowed in" },
                    { "30/60;ICRS", "Ranges are not allowed in" }, { "30/60,-23", "Ranges are not allowed in" },
                    { "100", "Must have exactly two coordinate values in" },
                    { "1,2,3", "Only 2 entries allowed in" },
                    { "1,2,3,4", "Only 2 entries allowed in" }, 
                    { "1,2;EXTRAGALACTC", "Unsupported coordinate system reference frame in" }, 
                    { "1,2;ECLIPTIC", "Unsupported coordinate system reference frame in" }, 
                    { "1,2;GALACTIC-I", "Unsupported coordinate system reference frame in" }, 
                    { "1,2;Nonsense", "Unsupported coordinate system reference frame in" }, 
                    { "1,2;FK4", "Unsupported coordinate system reference frame in" }, 
                    { "361,0;FK5", "Invalid right ascension in" }, 
                    { "-0.1,0;ICRS", "Invalid right ascension in" }, 
                    { "361,0", "Invalid right ascension in" }, 
                    { "15,91;FK5", "Invalid declination in" }, 
                    { "+15,-91;ICRS", "Invalid declination in" }, 
                    { "120,-120", "Invalid declination in" }, 
                    { "10,-91;GALACTIC", "Invalid latitude in" }, 
                    { "10,91;GALACTIC", "Invalid latitude in" }, 
                    { "361,0;GALACTIC", "Invalid longitude in" }, 
                    { "-181,0;GALACTIC", "Invalid longitude in" },});
             
        }

        private PosParamProcessor processor;

        private String invalidValue;

        private String expectedErrorMessageFragment;


        public ValidateSpecificInvalidTest(String invalidValue, String expectedErrorMessageFragment) throws Exception
        {
            this.invalidValue = invalidValue;
            this.expectedErrorMessageFragment = expectedErrorMessageFragment;
            processor = new PosParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " POS value " + invalidValue),
                    processor.validate("POS", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays
                    .asList(new Object[][] { { "300,-52", "" }, { new String[] { "5e+1,-1.25E+1", "120,-65" }, "" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private PosParamProcessor processor;

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
            processor = new PosParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("minCol", "maxCol", paramValues));
        }
    }

    /**
     * Check the BuildQuery method with a set of values for a single field parameter.
     */
    @RunWith(Parameterized.class)
    public static class BuildQuerySingleFieldTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays
                    .asList(new Object[][] { { "300,-52", "" }, { new String[] { "5e+1,-1.25E+1", "120,-65" }, "" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private PosParamProcessor processor;

        public BuildQuerySingleFieldTest(Object value, String expectedWhereClause)
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
            processor = new PosParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for pos " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

    /**
     * Check the getRaDec method.
     */
    @RunWith(Parameterized.class)
    public static class GetRaDecTest
    {

        @Parameters(name="{0}")
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays
                    .asList(new Object[][] { new Object[] { "300,-52", new double[] {300, -52} }, 
                        { "5e+1,-1.25E+1", new double[] {50, -12.5} }, 
                        { "5e+1,-1.25E+1;FK5", new double[] {50, -12.5} }, 
                        { "5e+1,-1.25E+1;ICRS", new double[] {50, -12.5} }, 
                        { "302.8084,-44.3277;GALACTIC", new double[] {13.15835, -72.80032} }, 
                        { "-57.1916,-44.3277;GALACTIC-II", new double[] {13.15835, -72.80032} }, 
                        { "0,0;GALACTIC", new double[] { 266.40496, -28.93618} } });
        }

        private String value;
        private double[] expectedRaDec;
        private PosParamProcessor processor;

        public GetRaDecTest(String value, double[] expectedRaDec)
        {
            this.value = value;
            this.expectedRaDec = expectedRaDec;
            processor = new PosParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            double[] raDec = processor.getRaDec(value);
            assertEquals("Incorrect Declination for " + value, expectedRaDec[1], raDec[1], 0.0001);
            assertEquals("Incorrect Right Ascension for " + value, expectedRaDec[0], raDec[0], 0.0001);
        }
    }

    
}
