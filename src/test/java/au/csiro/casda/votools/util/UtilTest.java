package au.csiro.casda.votools.util;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.csiro.casda.votools.siap2.Siap2Param;
import au.csiro.casda.votools.utils.Utils;

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
 * Tests the VO Tools Util class functionality (mostly to do with logging in atm).
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class UtilTest
{

    @Before
    public void setUp() throws Exception
    {
        File file = new File(Utils.AUTH_FILE_NAME);
        if (file.exists())
        {
            file.delete();
        }
    }

    /**
     * tests password validation
     */
    @Test
    public void validatePasswordValidation()
    {
        // valid matching passwords
        assertTrue(Utils.validatePassword("PassW0rd.", "PassW0rd."));
        // valid but not matching passwords
        assertFalse(Utils.validatePassword("PassW0rd.", "PassW0rd.s"));
        // Invalid missing number
        assertFalse(Utils.validatePassword("PassWord.", "PassWord."));
        // Invalid missing uppercase char
        assertFalse(Utils.validatePassword("passw0rd.", "passw0rd."));
        // Invalid missing lower char
        assertFalse(Utils.validatePassword("PASSW0RD.", "PASSW0RD."));
        // Invalid missing Punct char
        assertFalse(Utils.validatePassword("PassW0rd", "PassW0rd"));
        // Invalid too short
        assertFalse(Utils.validatePassword("PassW0.", "PassW0."));
    }

    @Test
    public void testPasswordWritingAndetrieving() throws Exception
    {
        String username = "username";
        String password = "password";

        File file = new File(Utils.AUTH_FILE_NAME);

        assertFalse(file.exists());

        Utils.writeToFile(new String[] { username, password });

        assertTrue(file.exists());

        String[] returned = Utils.retrieveFromFile();

        assertEquals(username, returned[0]);
        assertEquals(password, returned[1]);
    }

    @Test
    public void testBuildParamsMap() throws Exception
    {
        String number = "123";
        String spaces = "";

        Map<String, String[]> parameters = new TreeMap<String, String[]>();
        Siap2Param[] types = Siap2Param.values();
        for (Siap2Param type : types)
        {
            parameters.put(spaces + type.name() + spaces, new String[] { number });
            spaces = " " + spaces;
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(parameters);

        Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
        for (String key : parameters.keySet())
        {
            assertTrue(paramsMap.containsKey(key.trim().toLowerCase()));
            assertEquals(number, paramsMap.get(key.trim().toLowerCase())[0]);
        }
    }

    @Test
    public void testBuildParamsMapMerge() throws Exception
    {
        Map<String, String[]> parameters = new TreeMap<String, String[]>();
        parameters.put("one", new String[]{"a", "b"});
        parameters.put("ONE", new String[]{"c", "d"});
        Map<String, String[]> paramsMap = Utils.buildParamsMap(parameters);
        assertEquals(1, paramsMap.keySet().size());
        assertEquals(4, paramsMap.get("one").length);
        assertThat(paramsMap.get("one"), arrayContainingInAnyOrder("a", "b", "c", "d"));
    }

    @After
    public void doAfter()
    {
        File file = new File(Utils.AUTH_FILE_NAME);
        if (file.exists())
        {
            file.delete();
        }
    }

}
