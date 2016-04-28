package au.csiro.casda.votools.scs;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import au.csiro.casda.votools.TestUtils;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.scs.ConeSearchTable.Verbosity;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Validate the functions of ConeSearchTable.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class ConeSearchTableTest
{
    private TapSchema casdaSchema;

    @Before
    public void setup()
    {
        casdaSchema = new TapSchema();
        casdaSchema.setSchemaName("casda");
        casdaSchema.setTables(new ArrayList<TapTable>());
    }

    /**
     * Test method for
     * {@link au.csiro.casda.votools.scs.ConeSearchTable# 
     * addColumn(au.csiro.casda.votools.scs.ConeSearchTable.Verbosity, java.lang.String)}
     * .
     */
    @Test
    public void testAddColumn()
    {
        TapTable table =
                TestUtils.createTapTable("casda", "continuum_catalogue", casdaSchema, "ObsCore", true, false);
        ConeSearchTable csTable = new ConeSearchTable(table);

        csTable.addColumn(Verbosity.LEVEL_2, "peak_flux", 17);
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_1), is(""));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_2), is("peak_flux"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_3), is("peak_flux"));

        csTable.addColumn(Verbosity.LEVEL_3, "peak_flux_err", 18);
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_1), is(""));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_2), is("peak_flux"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_3), is("peak_flux,peak_flux_err"));

        csTable.addColumn(Verbosity.LEVEL_1, "sbid", 1);
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_1), is("sbid"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_2), is("sbid,peak_flux"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_3), is("sbid,peak_flux,peak_flux_err"));

        csTable.addColumn(Verbosity.LEVEL_3, "freq", 2);
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_1), is("sbid"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_2), is("sbid,peak_flux"));
        assertThat(csTable.getSelectFields(Verbosity.LEVEL_3), is("sbid,freq,peak_flux,peak_flux_err"));
    }

    /**
     * Test method for
     * {@link au.csiro.casda.votools.scs.ConeSearchTable#putVoTableColumnDef(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testPutVoTableColumnDef()
    {
        TapTable table =
                TestUtils.createTapTable("casda", "continuum_catalogue", casdaSchema, "ObsCore", true, false);
        ConeSearchTable csTable = new ConeSearchTable(table);
        assertThat(csTable.getVotableFieldMap().isEmpty(), is(true));

        final String colKey = "table|col";
        csTable.putVoTableColumnDef(colKey, "fieldDef");
        Map<String, String> votableFieldMap = csTable.getVotableFieldMap();
        assertThat(votableFieldMap.keySet(), contains(colKey));
        assertThat(votableFieldMap.get(colKey), is("fieldDef"));
    }

    @Test
    public void testFindLevelForKey()
    {
        assertThat(Verbosity.findLevelForKey("1"), is(Verbosity.LEVEL_1));
        assertThat(Verbosity.findLevelForKey("2"), is(Verbosity.LEVEL_2));
        assertThat(Verbosity.findLevelForKey("3"), is(Verbosity.LEVEL_3));
        assertThat(Verbosity.findLevelForKey("foo"), is(Verbosity.LEVEL_2));
        assertThat(Verbosity.findLevelForKey(""), is(Verbosity.LEVEL_2));
        assertThat(Verbosity.findLevelForKey(null), is(Verbosity.LEVEL_2));
    }

}
