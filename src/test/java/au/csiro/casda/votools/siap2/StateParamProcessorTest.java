package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
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
 * Verify the workings of StateParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class StateParamProcessorTest
{
    /**
     * Check the validateState method's handling of valid values.
     */
    public static class ValidateStateValidTest
    {
        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "" }),
                    Arguments.arguments((Object) new String[] { "Q" }),
                    Arguments.arguments((Object) new String[] { "U" }),
                    Arguments.arguments((Object) new String[] { "       " }),
                    Arguments.arguments((Object) new String[] { "xX" }),
                    Arguments.arguments((Object) new String[] { "rR" }),
                    Arguments.arguments((Object) new String[] { "POLi" }),
                    Arguments.arguments((Object) new String[] { "I", "q" }),
                    Arguments.arguments((Object) new String[] { "I", "", "q" }));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateState(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidateState(String[] validParamValues)
        {
            StateParamProcessor processor = new StateParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("State", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validateState method's handling of invalid values.
     */
    public static class ValidateStateInvalidTest
    {
        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments("/"), Arguments.arguments("//"), Arguments.arguments("UU"),
                    Arguments.arguments("X X"), Arguments.arguments("POL i"));
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateState(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidateState(String invalidValue)
        {
            StateParamProcessor processor = new StateParamProcessor();
            assertEquals("Expected '" + ArrayUtils.toString(invalidValue) + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid STATE value " + invalidValue),
                    processor.validate("STATE", new String[] { invalidValue }));
        }
    }

    /**
     * Check the BuildQuery method with a set of values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "XX" }, "(pol_states LIKE '%/XX/%')"),
                    Arguments.arguments((Object) new String[] { "XX", "YY" },
                            "(pol_states LIKE '%/XX/%') OR (pol_states LIKE '%/YY/%')"),
                    Arguments.arguments((Object) new String[] { "XX", "YY", "I" },
                            "(pol_states LIKE '%/XX/%') OR (pol_states LIKE '%/YY/%') OR (pol_states LIKE '%/I/%')"),
                    Arguments.arguments((Object) new String[] { "" }, ""),
                    Arguments.arguments((Object) new String[] { "//" }, ""));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithValidParams(String[] paramValues, String expectedAdql)
        {
            StateParamProcessor processor = new StateParamProcessor();
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    processor.buildQuery("", "", paramValues));
        }
    }

}
