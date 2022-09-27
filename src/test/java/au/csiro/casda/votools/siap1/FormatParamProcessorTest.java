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
 * Verify the FormatParamProcessor.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
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
            return Arrays.asList(new Object[][] { { "METADATA" }, { "metadata" }, { "all" }, { "graphic" },
                    { "graphic-ALL" }, { "image/fits" }, { "application/fits" }, { "metadata" }, { "text/html" },
                    { "image/png" }, { "image/PNG" }, { "image/fits,image/png" } });
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
                    { "text/csv" }, { "fits" }, { "fits/a" }, { "image\\png" }, { "image/jpeg" }, { "GRAPHIC-jpeg" },
                    { "image/jpg,image/gif,image/fits" } });
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
            assertThat("Expected '" + ArrayUtils.toString(invalidParamValues) + "' to be ignored.",
                    processor.validate("FORMAT", invalidParamValues), is(empty()));
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
            return Arrays.asList(new Object[][] { { "fits", "" }, { "FITS", "" }, { "image/png", "image/png" },
                    { "application/fits,graphic", "image/fits', 'image/png" }, { "native", "" },
                    { "graphic-ALL", "image/png" }, { "compliant", "" }, { "image/fits", "image/fits" },
                    { "application/xml", "" }, { "application/XML", "" } });
        }

        private String[] paramValues;
        private FormatParamProcessor processor;
        private String result;

        public BuildQueryTest(Object value, String resultFragment)
        {
            this.result = String.format("sia1_format.content_type IN ('%s')", resultFragment);
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
         * Test method for {@link au.csiro.casda.votools.siap2.FormatParamProcessor#buildQuery(java.lang.String)}.
         */
        @Test
        public void testWithValidParams()
        {
            assertEquals("Incorrect value for " + ArrayUtils.toString(paramValues), result,
                    processor.buildQuery(null, null, paramValues));
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
