package au.csiro.casda.votools.tap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;

import au.csiro.casda.votools.config.ConfigurationRegistry;

/**
 * Check the functions of the UploadParamProcessor class.
 * <p>
 * Copyright 2018, CSIRO Australia. All rights reserved.
 */
@RunWith(Enclosed.class)
public class UploadParamProcessorTest
{

    @Mock
    static ConfigurationRegistry configRegistry;
    
    /**
     * Check the validate method's handling of invalid values.
     */
    @RunWith(Parameterized.class)
    public static class ValidateInvalidTest
    {

        @Parameters(name = "{0}")
        public static Collection<Object> data()
        {
            // Param values (may be multiple)
            return Arrays.asList(
                    new Object[] { "table3", ",foo", "table3,", "table3,param:", "table3,:t3", "table3,mailto:foo.com",
                            "image1,vos://example.authority!tempSapce/foo.fits", "table4,param:t4;table3:param:t3"});

        }

        private UploadParamProcessor processor;

        private String invalidValue;

        public ValidateInvalidTest(String invalidValue) throws Exception
        {
            this.invalidValue = invalidValue;
            processor = new UploadParamProcessor(configRegistry);
        }

        /**
         * Test method for
         * {@link au.csiro.casda.votools.siap2.Siapv2Service#validateDouble(java.lang.String, java.lang.String[])}.
         */
        @Test
        public void testValidate()
        {
            assertEquals("Expected '" + invalidValue + "' to be invalid.",
                    Arrays.asList("UsageFault: Invalid UPLOAD parameter format: " + invalidValue),
                    processor.validate(new String[] { invalidValue }));
        }
    }

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
            return Arrays.asList(new Object[][] { { "table3,param:t3" }, { "table4,param:t4;table3,param:t3" },
                    { "table1,http://example.com/t1.xml" }, { "tab1,http://example.com/t1" },
                    { "tab2,https://example.com/t2" },
                    { new String[] { "table3,param:t3", "tab2,https://example.com/t2" } } });
        }

        private String[] validParamValues;

        private UploadParamProcessor processor;

        public ValidateValidTest(Object validValue) throws Exception
        {
            processor = new UploadParamProcessor(configRegistry);

            if (validValue instanceof String[])
            {
                validParamValues = (String[]) validValue;
            }
            else if (validValue instanceof String)
            {
                validParamValues = new String[] { (String) validValue };
            }
        }

        @Test
        public void testValidate()
        {
            assertThat("Expected '" + ArrayUtils.toString(validParamValues) + "' to be valid.",
                    processor.validate(validParamValues), is(empty()));
        }
    }

    /**
     * Check the validate method's handling of invalid values.
     */
    public static class ParseParamsTest
    {
        private UploadParamProcessor processor = new UploadParamProcessor(configRegistry);

        @Test
        public void testParseSingleInlineParam()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap = processor.parseParams(new String[] { "table3,param:t3" }, errorList);
            UploadedTable table = tableMap.get("table3");
            assertThat(table.getName(), is("table3"));
            assertThat(table.getUri(), is("param:t3"));
            assertThat(tableMap.size(), is(1));
        }

        @Test
        public void testParseSingleReferenceParam()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap =
                    processor.parseParams(new String[] { "table1,http://example.com/t1.xml" }, errorList);
            UploadedTable table = tableMap.get("table1");
            assertThat(table.getName(), is("table1"));
            assertThat(table.getUri(), is("http://example.com/t1.xml"));
            assertThat(tableMap.size(), is(1));
        }

        @Test
        public void testParseSingleParamPadding()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap =
                    processor.parseParams(new String[] { "table1  , http://example.com/t1.xml " }, errorList);
            UploadedTable table = tableMap.get("table1");
            assertThat(table.getName(), is("table1"));
            assertThat(table.getUri(), is("http://example.com/t1.xml"));
            assertThat(tableMap.size(), is(1));
        }

        @Test
        public void testParseSingleParamComma()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap =
                    processor.parseParams(new String[] { "table1,http://example.com/t1.xml?foo=bar,baz" }, errorList);
            UploadedTable table = tableMap.get("table1");
            assertThat(table.getName(), is("table1"));
            assertThat(table.getUri(), is("http://example.com/t1.xml?foo=bar,baz"));
            assertThat(tableMap.size(), is(1));
        }

        @Test
        public void testParseMultipleTapParam()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap =
                    processor.parseParams(new String[] { "table4,param:t4;table3,param:t3" }, errorList);

            UploadedTable table = tableMap.get("table4");
            assertThat(table.getName(), is("table4"));
            assertThat(table.getUri(), is("param:t4"));

            table = tableMap.get("table3");
            assertThat(table.getName(), is("table3"));
            assertThat(table.getUri(), is("param:t3"));

            assertThat(tableMap.size(), is(2));
        }

        @Test
        public void testParseMultipleParam()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap =
                    processor.parseParams(new String[] { "table4,param:t4", "table3,param:t3" }, errorList);

            UploadedTable table = tableMap.get("table4");
            assertThat(table.getName(), is("table4"));
            assertThat(table.getUri(), is("param:t4"));

            table = tableMap.get("table3");
            assertThat(table.getName(), is("table3"));
            assertThat(table.getUri(), is("param:t3"));

            assertThat(tableMap.size(), is(2));
        }

        @Test
        public void testParseDuplicateParam()
        {
            List<String> errorList = new ArrayList<>();
            Map<String, UploadedTable> tableMap = processor.parseParams(
                    new String[] { "table4,param:t4", "table3,param:t3", "table4,param:final" }, errorList);

            UploadedTable table = tableMap.get("table4");
            assertThat(table.getName(), is("table4"));
            assertThat(table.getUri(), is("param:final"));

            table = tableMap.get("table3");
            assertThat(table.getName(), is("table3"));
            assertThat(table.getUri(), is("param:t3"));

            assertThat(tableMap.size(), is(2));
        }
    }

}
