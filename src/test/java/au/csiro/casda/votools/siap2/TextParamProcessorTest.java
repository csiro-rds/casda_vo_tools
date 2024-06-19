package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
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
 * Verify the workings of TextParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class TextParamProcessorTest
{
    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateTextValidTest
    {
        public static Stream<Arguments> queryParams()
        {
            // atm only the DPType values are validated
            return Stream.of(Arguments.arguments((Object) new String[]{"image"}),
                    Arguments.arguments((Object) new String[]{"cube"}));
        }

        private TextParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new TextParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidateText(String[] validParamValues)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("DPTYPE", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateTextInvalidTest
    {
        public static Stream<Arguments> queryParams()
        {
            // atm only the DPType values are validated
            return Stream.of(Arguments.arguments("image-1"),
                    Arguments.arguments("cutout"));
        }

        private TextParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new TextParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testValidate(String invalidValue)
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
    public static class BuildQueryTest
    {
        
        public static Stream<Arguments> queryParams()
        {
            // atm only the DPType values are validated
            return Stream.of(Arguments.arguments((Object) new String[]{"cube"}, "(lower(DPTYPE) = 'cube')"),
                    Arguments.arguments((Object) new String[]{"image"}, "(lower(DPTYPE) = 'image')"));
        }

        private TextParamProcessor processor;

        @BeforeEach
        public void setup()
        {
            processor = new TextParamProcessor();
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.TextParamProcessor#buildQuery(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithValidParams(String[] paramValues, String result)
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery("DPTYPE", "maxCol", paramValues));
        }
    }
}
