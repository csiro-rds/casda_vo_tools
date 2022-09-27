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
 * Verify the workings of TextParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class TextParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateTextValidTest
    {
        // atm only the DPType values are validated
        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "image" }, { "cube" } });
        }

        private String[] validParamValues;

        private TextParamProcessor processor;

        public ValidateTextValidTest(Object validValue) throws Exception
        {
            processor = new TextParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidateText()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("DPTYPE", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateTextInvalidTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "image-1" }, { "cutout" } });
        }

        private TextParamProcessor processor;

        private String invalidValue;

        public ValidateTextInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new TextParamProcessor();
        }


        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: The value '" + invalidValue + "' is not valid for the DPTYPE. The value "
                            + "must be one of the following: image or cube"),
                    processor.validate("DPTYPE", new String[] { invalidValue }));
        }
    }
    
    /**
     * Check the BuildQuery method with valid values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] {
                    { "cube", "(lower(DPTYPE) = 'cube')" },
                    { "image", "(lower(DPTYPE) = 'image')" } });
        }

        private String[] paramValues;
        private TextParamProcessor processor;
        private String result;

        public BuildQueryTest(Object value, String result)
        {
            this.result = result;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
            processor = new TextParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("DPTYPE", "maxCol", paramValues));
        }
    }
}
