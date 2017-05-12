package au.csiro.casda.votools.ssap;

import static org.hamcrest.Matchers.contains;
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
 * Verify the workings of FormatParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class FormatParamProcessorTest
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
            return Arrays.asList(new Object[][] { { "METADATA" }, { "metadata" }, { "votable" }, { "all" },
                    { "compliant" }, { "native" }, { "NATIVE" }, { "NatiVE" }, { "Native" }, { "graphic" }, { "fits" },
                    { "xml" }, { "application/x-votable+xml" }, { "application/fits" }, { "application/xml" },
                    { "image/jpeg" }, { "image/png" }, { "image/PNG" }, { "fits,votable" },
                    { "image/jpg,image/gif,image/tiff" } });
        }

        private String[] validParamValues;

        private FormatParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new FormatParamProcessor();

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
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("FORMAT", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "application/fits;convention=STScI-STIS" }, { "unknown" },
                    { "text/csv" }, { "text/html" }, { "fits/a" }, { "image\\jpeg" }, { "" } });
        }

        private String[] invalidParamValues;

        private FormatParamProcessor processor;

        public ValidateInvalidTest(Object invalidValue) throws Exception
        {
            processor = new FormatParamProcessor();

            if (invalidValue instanceof String[])
            {
                invalidParamValues = (String[]) invalidValue;
            }
            else if (invalidValue instanceof String)
            {
                invalidParamValues = new String[] { (String) invalidValue };
            }
        }

        @Test
        public void testInValidate()
        {
            assertThat(processor.validate("FORMAT", invalidParamValues),
                    contains("UsageFault: FORMAT " + invalidParamValues[0] + " is not supported"));
        }
    }

    /**
     * Check the BuildQuery method with valid values.
     */
    @RunWith(Parameterized.class)
    public static class BuildQueryTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] { { "fits", "" }, { "FITS", "" }, { "image/png", "(1=0)" },
                    { "application/fits,graphic", "" }, { "native", "" }, { "all", "" }, { "compliant", "" },
                    { "image/fits", "" }, { "application/xml", "(1=0)" }, { "application/XML", "(1=0)" }  });
        }

        private String[] paramValues;
        private FormatParamProcessor processor;
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
            processor = new FormatParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery(null, null, paramValues));
        }
    }
}
