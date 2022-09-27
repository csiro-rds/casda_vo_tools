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
 * Verify the workings of SizeParamProcessor.
 * <p>
 * Copyright 2016, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class SizeParamProcessorTest
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
            return Arrays.asList(new Object[][] { { "0.01" }, { "1E-2" }, { "1.5E+1" }, { "1.5e1" },
                    { "0.05" }, { "7" }, { "" },
                    { new String[] { "5", "3" } } });
        }

        private String[] validParamValues;

        private SizeParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new SizeParamProcessor();

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
                    { "-inf,600" }, { "300,+inf" }, { "inf,600" }, { "300,inf" }, { "2.3-01" }, { "2.3e" }, { "2.3e-" },
                    { "2.3e 6" }, { "2.3 e-5" }, { "300 600 1300" } });
        }

        private SizeParamProcessor processor;

        private String invalidValue;

        public ValidateGenericInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new SizeParamProcessor();
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
                    { "30/60", "Ranges are not allowed in" }, 
                    { "10,2", "Only 1 entry allowed in" },
                    { "1,2,3", "Only 1 entry allowed in" },
                    { "1,2,3,4", "Only 1 entry allowed in" }, 
                    { "1;EXTRAGALACTC", "Unsupported qualifier in" }, 
                    { "1;source", "Unsupported qualifier in" }, 
                    { "1.5;Nonsense", "Unsupported qualifier in" }, 
                    { "0", "Value must be between 0 and 15 in" }, 
                    { "-1", "Value must be between 0 and 15 in" }, 
                    { "15.001", "Value must be between 0 and 15 in" }, 
                    { "1.6E1", "Value must be between 0 and 15 in" }});
             
        }

        private SizeParamProcessor processor;

        private String invalidValue;

        private String expectedErrorMessageFragment;


        public ValidateSpecificInvalidTest(String invalidValue, String expectedErrorMessageFragment) throws Exception
        {
            this.invalidValue = invalidValue;
            this.expectedErrorMessageFragment = expectedErrorMessageFragment;
            processor = new SizeParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: " + expectedErrorMessageFragment + " SIZE value " + invalidValue),
                    processor.validate("SIZE", new String[] { invalidValue }));
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
                    .asList(new Object[][] { { "10", "" }, { new String[] { "1.05e+1", "2.1" }, "" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private SizeParamProcessor processor;

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
            processor = new SizeParamProcessor();
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
                    .asList(new Object[][] { { "10", "" }, { new String[] { "1.05e+1", "2.1" }, "" } });
        }

        private String[] paramValues;
        private String expectedAdql;
        private SizeParamProcessor processor;

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
            processor = new SizeParamProcessor();
        }

        @Test
        public void testWithDoubleRange()
        {
            assertEquals("Incorrect result for size " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("s_fov", "s_fov", paramValues));
        }
    }

}
