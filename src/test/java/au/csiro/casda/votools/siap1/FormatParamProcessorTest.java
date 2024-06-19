package au.csiro.casda.votools.siap1;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verify the FormatParamProcessor.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class FormatParamProcessorTest
{

    /**
     * Check the validate method's handling of valid values.
     */
    public static class ValidateValidTest
    {
        
        public static Stream<Arguments> validQueryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"METADATA"}),
                    Arguments.arguments((Object) new String[]{"metadata"}),
                    Arguments.arguments((Object) new String[]{"all"}),
                    Arguments.arguments((Object) new String[]{"graphic"}),
                    Arguments.arguments((Object) new String[]{"graphic-ALL"}),
                    Arguments.arguments((Object) new String[]{"image/fits"}),
                    Arguments.arguments((Object) new String[]{"application/fits"}),
                    Arguments.arguments((Object) new String[]{"text/html"}),
                    Arguments.arguments((Object) new String[]{"image/png"}),
                    Arguments.arguments((Object) new String[]{"image/PNG"}),
                    Arguments.arguments((Object) new String[]{"image/fits,image/png"}));
        }

        @BeforeEach
        private void setup()
        {
            processor = new FormatParamProcessor();
        }
        
        private FormatParamProcessor processor;

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.FormatParamProcessor#validate(java.lang.String, java.lang.String[])}.
         */
        @ParameterizedTest
        @MethodSource("validQueryParams")
        public void testValidate(String [] validParams)
        {
            assertThat("Expected '" + ArrayUtils.toString(validParams) + "' to be valid.",
                    processor.validate("FORMAT", validParams), empty());
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ValidateInvalidTest
    {

        public static Stream<Arguments> invalidQueryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"application/fits;convention=STScI-STIS"}),
                    Arguments.arguments((Object) new String[]{"unknown"}),
                    Arguments.arguments((Object) new String[]{"text/csv"}),
                    Arguments.arguments((Object) new String[]{"fits"}),
                    Arguments.arguments((Object) new String[]{"fits/a"}),
                    Arguments.arguments((Object) new String[]{"image\\png"}),
                    Arguments.arguments((Object) new String[]{"image/jpeg"}),
                    Arguments.arguments((Object) new String[]{"GRAPHIC-jpeg"}),
                    Arguments.arguments((Object) new String[]{"image/jpg,image/gif,image/fits"}));
        }

        @BeforeEach
        private void setup()
        {
            processor = new FormatParamProcessor();
        }
        
        private FormatParamProcessor processor;

        @ParameterizedTest
        @MethodSource("invalidQueryParams")
        public void testInValidate(String [] param)
        {
            assertThat("Expected '" + ArrayUtils.toString(param) + "' to be ignored.",
                    processor.validate("FORMAT", param), is(empty()));
        }
    }

    /**
     * Check the BuildQuery method with valid values.
     */
    public static class BuildQueryTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(Arguments.arguments((Object) new String[]{"fits"}, ""),
                    Arguments.arguments((Object) new String[]{"FITS"},  ""),
                    Arguments.arguments((Object) new String[]{"image/png"}, "image/png"),
                    Arguments.arguments((Object) new String[]{"application/fits,graphic"}, "image/fits', 'image/png"),
                    Arguments.arguments((Object) new String[]{"native"}, ""),
                    Arguments.arguments((Object) new String[]{"graphic-ALL"}, "image/png"),
                    Arguments.arguments((Object) new String[]{"compliant"}, ""),
                    Arguments.arguments((Object) new String[]{"image/fits"}, "image/fits"),
                    Arguments.arguments((Object) new String[]{"application/xml"}, ""),
                    Arguments.arguments((Object) new String[]{"application/XML"}, ""));
        }

        @BeforeEach
        private void setup()
        {
            processor = new FormatParamProcessor();
        }
        
        private FormatParamProcessor processor;

        /**
         * Test method for {@link au.csiro.casda.votools.siap2.FormatParamProcessor#buildQuery(java.lang.String)}.
         */
        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithValidParams(String[] param, String resultFragment)
        {
            String result = String.format("sia1_format.content_type IN ('%s')", resultFragment);
            assertEquals("Incorrect value for " + ArrayUtils.toString(param), result,
                    processor.buildQuery(null, null, param));
        }
    }

    /**
     * Check the getSelectedFormats method.
     */
    public static class GetSelectedFormatsTest
    {

        private FormatParamProcessor processor;

        public GetSelectedFormatsTest() throws Exception
        {
            processor = new FormatParamProcessor();

        }

        @Test
        public void testValidSingle()
        {
            assertThat(processor.getSelectedFormats(new String[] {}), contains("image/fits", "image/png"));
            assertThat(processor.getSelectedFormats(null), contains("image/fits", "image/png"));
            assertThat(processor.getSelectedFormats(new String[] { "all" }),
                    contains("image/fits", "image/png"));

            assertThat(processor.getSelectedFormats(new String[] { "graphiC" }), contains("image/png"));
            assertThat(processor.getSelectedFormats(new String[] { "GRAPHIC-all" }),
                    contains("image/png"));

            assertThat(processor.getSelectedFormats(new String[] { "image/fits" }), contains("image/fits"));
            assertThat(processor.getSelectedFormats(new String[] { "image/PNG" }), contains("image/png"));
        }

        @Test
        public void testValidMultiple()
        {
            assertThat(processor.getSelectedFormats(new String[] { "APPLICATION/fits", "image/png" }),
                    contains("image/fits", "image/png"));
            assertThat(processor.getSelectedFormats(new String[] { "all", "GRAPHIC", "image/png" }),
                    contains("image/fits", "image/png"));
        }

        @Test
        public void testInvalidIgnored()
        {
            assertThat(processor.getSelectedFormats(new String[] { "something" }), empty());
            assertThat(processor.getSelectedFormats(new String[] { "text/html" }), empty());
            assertThat(processor.getSelectedFormats(new String[] { "image/tiff", "image/BMP" }), empty());
        }

        @Test
        public void testMixedMultiple()
        {
            assertThat(processor.getSelectedFormats(new String[] { "APPLICATION/fits", "image/BMP" }),
                    contains("image/fits"));
            assertThat(processor.getSelectedFormats(new String[] { "image/fits,GRAPHIC-jpeg,png,gif" }),
                    contains("image/fits"));
        }
    }

    /**
     * Check the getPixFlags method.
     */
    public static class GetPixFlagTest
    {

        private FormatParamProcessor processor;

        public GetPixFlagTest() throws Exception
        {
            processor = new FormatParamProcessor();

        }

        @Test
        public void testGetPixFlag()
        {
            assertEquals("C", processor.getPixFlags("image/fits"));
            assertEquals("V", processor.getPixFlags("image/png"));
        }
    }
}
