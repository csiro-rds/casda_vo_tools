package au.csiro.casda.votools.siap2;

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
 * Check the AdqlQueryBuilder class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(Enclosed.class)
public class AdqlQueryBuilderTest
{

    /**
     * Check the withDoubleRange method with a set of values.
     */
    @RunWith(Parameterized.class)
    public static class WithDoubleRangeTest
    {

        @Parameters
        public static Collection<Object[]> data()
        {
            // Pairs of param values (may be multiple) and the expected where clause
            return Arrays.asList(new Object[][] {
                    { "300", " WHERE ((minCol <= 300 AND maxCol >= 300))" },
                    { "300/600", " WHERE ((minCol <= 600 AND maxCol >= 300))" },
                    { "300/", " WHERE ((maxCol >= 300))" },
                    { "/600", " WHERE ((minCol <= 600))" },
                    { "", "" },
                    { "/", " WHERE ((minCol IS NOT NULL AND maxCol IS NOT NULL))" },
                    { new String[] { "500", "0.21" },
                            " WHERE ((minCol <= 500 AND maxCol >= 500) OR (minCol <= 0.21 AND maxCol >= 0.21))" } });
        }

        private String[] paramValues;
        private String expectedAdql;

        public WithDoubleRangeTest(Object value, String expectedWhereClause)
        {
            this.expectedAdql = "SELECT * FROM ivoa.obscore" + expectedWhereClause;
            if (value instanceof String[])
            {
                paramValues = (String[]) value;
            }
            else if (value instanceof String)
            {
                paramValues = new String[] { (String) value };
            }
        }

        @Test
        public void testWithDoubleRange()
        {
            AdqlQueryBuilder builder = new AdqlQueryBuilder("ivoa.obscore");
            assertEquals(builder, builder.withDoubleRange("minCol", "maxCol", paramValues));
            assertEquals("Incorrect result for range " + ArrayUtils.toString(paramValues), expectedAdql,
                    builder.toString());
        }
    }

    /**
     * Check the withSpecificClause method.
     */
    public static class WithSpecificClauseTest
    {

        @Test
        public void testSingleCriteria()
        {
            AdqlQueryBuilder builder = new AdqlQueryBuilder("ivoa.obscore");
            assertEquals(builder, builder.withSpecificClause("obs_collection = 'BETA Science Observations'"));
            assertEquals("Incorrect query",
                    "SELECT * FROM ivoa.obscore WHERE (obs_collection = 'BETA Science Observations')",
                    builder.toString());
        }

        @Test
        public void testMultipleCriteria()
        {
            AdqlQueryBuilder builder = new AdqlQueryBuilder("ivoa.obscore");
            assertEquals(builder, builder.withSpecificClause("obs_collection = 'BETA Science Observations'"));
            assertEquals(builder, builder.withSpecificClause("dataproduct_type IN ('cube')"));
            assertEquals("Incorrect query",
                    "SELECT * FROM ivoa.obscore WHERE (obs_collection = 'BETA Science Observations') "
                            + "AND (dataproduct_type IN ('cube'))", builder.toString());
        }
    }

}
