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
 * Verify the workings of StateParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class StateParamProcessorTest
{
    /**
     * Check the validateState method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateStateValidTest
    {
        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "" }, { "Q" }, { " U " }, { "       " }, { "xX" }, { "rR" }, {"POLi"},
                {new String[] { "I", "q" }}, {new String[] { "I", "", "q" }}
            });
            
        }
        
        private String[] validParamValues;

        private StateParamProcessor processor;

        public ValidateStateValidTest(Object validValue) throws Exception
        {
            processor = new StateParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateState(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateState()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("State", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validateState method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateStateInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(new Object[][] { { "/" }, { "//" }, { "UU" }, { "X X" }, {"POL i"}});
        }

        private StateParamProcessor processor;

        private String invalidValue;

        public ValidateStateInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new StateParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateState(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateState()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid STATE value " + invalidValue),
                    processor.validate("STATE", new String[] { invalidValue }));
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
            // @formatter:off
            return Arrays.asList(new Object[][] { 
                { "XX", "(pol_states LIKE '%/XX/%')" },
                { new String[] { "XX", "YY" }, "(pol_states LIKE '%/XX/%') OR (pol_states LIKE '%/YY/%')" }, 
                { new String[] { "XX", "YY" , "I"}, "(pol_states LIKE '%/XX/%') OR (pol_states LIKE '%/YY/%')"
                        + " OR (pol_states LIKE '%/I/%')" }, 
                { "", "" },
                { "//", "" }
            });
            // @formatter:on
        }

        private String[] paramValues;
        private String expectedAdql;
        private StateParamProcessor processor;

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
            processor = new StateParamProcessor();
        }

        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("", "", paramValues));
        }
    }

}
