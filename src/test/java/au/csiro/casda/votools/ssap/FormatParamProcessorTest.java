package au.csiro.casda.votools.ssap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

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
 * Verify the workings of FormatParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class FormatParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "METADATA" }),
                    Arguments.arguments((Object) new String[] { "metadata" }),
                    Arguments.arguments((Object) new String[] { "votable" }),
                    Arguments.arguments((Object) new String[] { "all" }),
                    Arguments.arguments((Object) new String[] { "compliant" }),
                    Arguments.arguments((Object) new String[] { "native" }),
                    Arguments.arguments((Object) new String[] { "NATIVE" }),
                    Arguments.arguments((Object) new String[] { "NatiVE" }),
                    Arguments.arguments((Object) new String[] { "Native" }),
                    Arguments.arguments((Object) new String[] { "graphic" }),
                    Arguments.arguments((Object) new String[] { "fits" }),
                    Arguments.arguments((Object) new String[] { "xml" }),
                    Arguments.arguments((Object) new String[] { "application/x-votable+xml" }),
                    Arguments.arguments((Object) new String[] { "application/fits" }),
                    Arguments.arguments((Object) new String[] { "application/xml" }),
                    Arguments.arguments((Object) new String[] { "image/jpeg" }),
                    Arguments.arguments((Object) new String[] { "image/png" }),
                    Arguments.arguments((Object) new String[] { "image/PNG" }),
                    Arguments.arguments((Object) new String[] { "fits,votable" }),
                    Arguments.arguments((Object) new String[] { "image/jpg,image/gif,image/tiff" }));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testValidate(String[] validParamValues)
        {
            FormatParamProcessor processor = new FormatParamProcessor();
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate("FORMAT", validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateInvalidTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "application/fits;convention=STScI-STIS" }),
                    Arguments.arguments((Object) new String[] { "unknown" }),
                    Arguments.arguments((Object) new String[] { "text/csv" }),
                    Arguments.arguments((Object) new String[] { "text/html" }),
                    Arguments.arguments((Object) new String[] { "fits/a" }),
                    Arguments.arguments((Object) new String[] { "image\\jpeg" }),
                    Arguments.arguments((Object) new String[] { "" }));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testInValidate(String[] invalidParamValues)
        {
            FormatParamProcessor processor = new FormatParamProcessor();
            assertThat(processor.validate("FORMAT", invalidParamValues),
                    contains("UsageFault: FORMAT " + invalidParamValues[0] + " is not supported"));
        }
    }

    /**
     * Check the BuildQuery method with valid values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> testParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[] { "fits" }, ""),
                    Arguments.arguments((Object) new String[] { "FITS" }, ""),
                    Arguments.arguments((Object) new String[] { "image/png" }, "(1=0)"),
                    Arguments.arguments((Object) new String[] { "application/fits,graphic" }, ""),
                    Arguments.arguments((Object) new String[] { "native" }, ""),
                    Arguments.arguments((Object) new String[] { "all" }, ""),
                    Arguments.arguments((Object) new String[] { "compliant" }, ""),
                    Arguments.arguments((Object) new String[] { "image/fits" }, ""),
                    Arguments.arguments((Object) new String[] { "application/xml" }, "(1=0)"),
                    Arguments.arguments((Object) new String[] { "application/XML" }, "(1=0)"));
        }

        @ParameterizedTest
        @MethodSource("testParams")
        public void testWithValidParams(String[] paramValues, String result)
        {
            FormatParamProcessor processor = new FormatParamProcessor();
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery(null, null, paramValues));
        }
    }
}
