package au.csiro.casda.votools.siap1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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

/**
 * Tests for the SurveyParamProcessor class.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
@RunWith(Enclosed.class)
public class SurveyParamProcessorTest
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
            return Arrays.asList(new Object[][] { { "RACS-Low" }, { "racs-low" }, { "RACS-LOW" }, { "survey2" },
                    { "sUrVeY2" }, { "SURVEY2" }, { "VAST Epoch 1" }, { "VAST EPOCH 1" } });
        }

        private String[] validParamValues;

        private SurveyParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new SurveyParamProcessor();
            processor.setSurveys(Arrays.asList("RACS-Low", "Survey2", "VAST Epoch 1"));

            validParamValues = new String[] { (String) validValue };
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("SURVEY", validParamValues), is(empty()));
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
            return Arrays.asList(new Object[][] { { "unexpected" }, { "unknown" }, { "RACS-Low,Survey2" }, { "" } });
        }

        private String[] invalidParamValues;

        private SurveyParamProcessor processor;

        public ValidateInvalidTest(Object invalidValue) throws Exception
        {
            processor = new SurveyParamProcessor();
            processor.setSurveys(Arrays.asList("RACS-Low", "Survey2", "VAST Epoch 1"));

            invalidParamValues = new String[] { (String) invalidValue };
        }

        @Test
        public void testInValidate()
        {
            assertThat(processor.validate("SURVEY", invalidParamValues),
                    contains("UsageFault: SURVEY " + invalidParamValues[0] + " is not supported"));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateSingleOnlyTest
    {

        private SurveyParamProcessor processor;

        public ValidateSingleOnlyTest()
        {
            processor = new SurveyParamProcessor();
            processor.setSurveys(Arrays.asList("RACS-Low", "Survey2", "VAST Epoch 1"));
        }

        @Test
        public void testValidateMultipeValues()
        {

            String[] paramValues = new String[] { "RACS-Low", "Survey2" };
            assertThat(processor.validate("SURVEY", paramValues),
                    contains("UsageFault: Query can only contain a single SURVEY value"));
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
            return Arrays.asList(
                    new Object[][] { { "RACS-Low", "" }, { "racs-low", "" }, { "RACS-LOW", "" }, { "survey2", "" },
                            { "sUrVeY2", "" }, { "SURVEY2", "" }, { "VAST Epoch 1", "" }, { "VAST EPOCH 1", "" } });
        }

        private String[] paramValues;
        private SurveyParamProcessor processor;
        private String result;

        public BuildQueryTest(Object value, String result)
        {
            this.result = result;
            paramValues = new String[] { (String) value };
            processor = new SurveyParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#buildQuery(java.lang.String)}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery(null, null, paramValues));
        }
    }

}
