package au.csiro.casda.votools.config;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
 * 
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class OptionsTest
{

    Options options, options2;

    @Before
    public void setUp() throws Exception
    {
        options = new Options();
        options.put("long", "1");
        options.put("int", "2");
        options.put("float", "3.0f");
        options.put("double", "4.0");
        options.put("boolean1", "1");
        options.put("booleanTrue", "true");
        options.put("booleanOn", "on");
        options.put("booleanYes", "yes");
        options2 = new Options();
        options2.put("long", "1");
        options2.put("int", "2");
        options2.put("float", "3.0f");
        options2.put("double", "4.0");
        options2.put("boolean1", "1");
        options2.put("booleanTrue", "true");
        options2.put("booleanOn", "on");
        options2.put("booleanYes", "yes");
    }

    @Test
    @Ignore
    public void testEquivalence()
    {
        if (!options.equals(options2))
        {
            fail("Equivalence is not recognised.");
        }
        options.put("Not the same now", "true");
        if (options.equals(options2))
        {
            fail("Difference is not found.");
        }
        options2.put("Almost same now", "true");
        if (options.equals(options2))
        {
            fail("Difference is not found.");
        }
    }

    @Test
    public void testLong()
    {
        assertEquals(options.getLong("long"), 1L);
    }

    @Test
    public void testInt()
    {
        assertEquals(options.getInt("int"), 2);
    }

    @Test
    public void testFloat()
    {
        assertTrue(options.getFloat("float") == 3.0f);
    }

    @Test
    public void testDouble()
    {
        assertTrue(options.getDouble("double") == 4.0);
    }

    @Test
    public void testBoolean1() throws ConfigurationException
    {
        assertTrue(options.getBoolean("boolean1"));
        options.put("boolean1", "0");
        assertFalse(options.getBoolean("boolean1"));

    }

    @Test
    public void testbooleanTrue() throws ConfigurationException
    {
        assertTrue(options.getBoolean("booleanTrue"));
        options.put("booleanTrue", "false");
        assertFalse(options.getBoolean("booleanTrue"));
    }

    @Test
    public void testbooleanOn() throws ConfigurationException
    {
        assertTrue(options.getBoolean("booleanOn"));
        options.put("booleanOn", "Off");
        assertFalse(options.getBoolean("booleanOn"));
    }

    @Test
    public void testbooleanYes() throws ConfigurationException
    {
        assertTrue(options.getBoolean("booleanYes"));
        options.put("booleanYes", "No");
        assertFalse(options.getBoolean("booleanYes"));
    }

}
