package au.csiro.casda.votools.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.nullValue;

import java.sql.Date;
import java.sql.ResultSet;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import au.csiro.casda.votools.config.TapObjectCache.SchemaMapper;
import au.csiro.casda.votools.config.TapObjectCache.TableMapper;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;

/**
 * Test for the TapObjectCache class.
 * 
 * Copyright 2020, CSIRO Australia
 * All rights reserved.
 */
public class TapObjectCacheTest
{
    private TapObjectCache tapObjectCache;
    private DateTime futureDate = DateTime.now().plusHours(1);
    
    @Before
    public void setup() throws Exception
    {
        tapObjectCache = new TapObjectCache();
        
        // Set up a schema to put tables in.
        SchemaMapper schemaMapper = tapObjectCache.new SchemaMapper();
        ResultSet rs = mock(ResultSet.class);
        when (rs.getString(TapSchema.NAME)).thenReturn("test");
        schemaMapper.mapRow(rs, 1);

    }
    
    @Test
    public void testMapRowWithReleaseDate() throws Exception
    {
        TableMapper mapper = tapObjectCache.new TableMapper();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(TapTable.SCHEMA_NAME)).thenReturn("test");
        when(rs.getString(TapTable.NAME)).thenReturn("private_table");
        when(rs.getBoolean(TapTable.RELEASE_REQUIRED)).thenReturn(true);
        when(rs.getDate("release_date")).thenReturn(new Date(futureDate.getMillis()));

        TapTable tapTable = mapper.mapRow(rs , 1);
        assertThat(tapTable.getTableName(), is("private_table"));
        assertThat(tapTable.getReleaseDate(), is(futureDate));
        assertThat(tapTable.getReleaseRequired(), is(true));

        TapSchema tapSchema = tapTable.getSchema();
        assertThat(tapSchema.getSchemaName(), is("test"));
        assertThat(tapSchema.getTables(), contains(tapTable));
    }
    
    @Test
    public void testMapRowNoReleaseDate() throws Exception
    {
        TableMapper mapper = tapObjectCache.new TableMapper();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(TapTable.SCHEMA_NAME)).thenReturn("test");
        when(rs.getString(TapTable.NAME)).thenReturn("public_table");
        when(rs.getBoolean(TapTable.RELEASE_REQUIRED)).thenReturn(false);

        TapTable tapTable = mapper.mapRow(rs , 1);
        assertThat(tapTable.getTableName(), is("public_table"));
        assertThat(tapTable.getReleaseDate(), is(nullValue()));
        assertThat(tapTable.getReleaseRequired(), is(false));

        TapSchema tapSchema = tapTable.getSchema();
        assertThat(tapSchema.getSchemaName(), is("test"));
        assertThat(tapSchema.getTables(), contains(tapTable));
    }

}
