package au.csiro.casda.votools.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2020 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Tests for the ColumnConfig class.
 * <p>
 * Copyright 2020, CSIRO Australia. All rights reserved.
 */
public class ColumnConfigTest
{

    @Test
    public void testEqualsXOptions()
    {
        ColumnConfig first = createColumnConfig("size", "VARCHAR", "true");
        ColumnConfig second = createColumnConfig("size", "VARCHAR", "true");
        ColumnConfig quoted = createColumnConfig("\"size\"", "VARCHAR", "true");

        assertThat(first.equalsXOptions(second), is(true));
        assertThat("Raw and quoted size equal", first.equalsXOptions(quoted), is(true));
    }

    private ColumnConfig createColumnConfig(String name, String type, String notNull)
    {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(name);
        columnConfig.setType(type);
        columnConfig.setNotnull(notNull);
        return columnConfig;
    }

}
