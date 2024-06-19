package au.csiro.casda.votools.siap1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
/**
 * Tests for the SurveyParamProcessor class.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class SurveyParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {
        
        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"RACS-Low"}),
                    Arguments.arguments((Object) new String[]{"racs-low"}),
                    Arguments.arguments((Object) new String[]{"RACS-LOW"}),
                    Arguments.arguments((Object) new String[]{"survey2"}),
                    Arguments.arguments((Object) new String[]{"sUrVeY2"}),
                    Arguments.arguments((Object) new String[]{"SURVEY2"}),
                    Arguments.arguments((Object) new String[]{"VAST Epoch 1"}),
                    Arguments.arguments((Object) new String[]{"VAST EPOCH 1"}));
        }
        
        private SurveyParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SurveyParamProcessor();
            processor.setSurveys(Arrays.asList("RACS-Low", "Survey2", "VAST Epoch 1"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String [] validParamValues)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("SURVEY", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateInvalidTest
    {

        public static Stream<Arguments> invalidParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"unexpected"}),
                    Arguments.arguments((Object) new String[]{"unknown"}),
                    Arguments.arguments((Object) new String[]{"RACS-Low,Survey2"}),
                    Arguments.arguments((Object) new String[]{""}));
        }

        private SurveyParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SurveyParamProcessor();
            processor.setSurveys(Arrays.asList("RACS-Low", "Survey2", "VAST Epoch 1"));
        }

        @ParameterizedTest
        @MethodSource("invalidParams")
        public void testInValidate(String [] invalidParamValues)
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
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"RACS-Low"}, ""),
                    Arguments.arguments((Object) new String[]{"racs-low"}, ""),
                    Arguments.arguments((Object) new String[]{"RACS-LOW"}, ""),
                    Arguments.arguments((Object) new String[]{"survey2"}, ""),
                    Arguments.arguments((Object) new String[]{"sUrVeY2"}, ""),
                    Arguments.arguments((Object) new String[]{"SURVEY2"}, ""),
                    Arguments.arguments((Object) new String[]{"VAST Epoch 1"}, ""),
                    Arguments.arguments((Object) new String[]{"VAST EPOCH 1"}, ""));
        }

        private SurveyParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new SurveyParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#buildQuery(java.lang.String)}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithValidParams(String [] params, String result)
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(params), result,
                    processor.buildQuery(null, null, params));
        }
    }

}
