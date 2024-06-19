package au.csiro.casda.votools.siap2;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
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
 * Check the AdqlQueryBuilder class.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class AdqlQueryBuilderTest
{

    /**
     * Check the withDoubleRange method with a set of values.
     */
    public static class WithDoubleRangeTest
    {

        public static Stream<Arguments> queryParams()
        {
            return Stream.of(
                    Arguments.arguments((Object) new String[] { "300" }, " WHERE ((minCol <= 300 AND maxCol >= 300))"),
                    Arguments.arguments((Object) new String[] { "300/600" },
                            " WHERE ((minCol <= 600 AND maxCol >= 300))"),
                    Arguments.arguments((Object) new String[] { "300/" }, " WHERE ((maxCol >= 300))"),
                    Arguments.arguments((Object) new String[] { "/600" }, " WHERE ((minCol <= 600))"),
                    Arguments.arguments((Object) new String[] { "" }, ""),
                    Arguments.arguments((Object) new String[] { "/" },
                            " WHERE ((minCol IS NOT NULL AND maxCol IS NOT NULL))"),
                    Arguments.arguments((Object) new String[] { "500", "0.21" },
                            " WHERE ((minCol <= 500 AND maxCol >= 500) OR (minCol <= 0.21 AND maxCol >= 0.21))"));
        }

        @ParameterizedTest
        @MethodSource("queryParams")
        public void testWithDoubleRange(String[] paramValues, String expectedWhereClause)
        {
            String expectedAdql = "SELECT * FROM ivoa.obscore" + expectedWhereClause;
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
                            + "AND (dataproduct_type IN ('cube'))",
                    builder.toString());
        }
    }

}
