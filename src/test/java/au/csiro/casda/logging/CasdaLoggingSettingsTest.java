package au.csiro.casda.logging;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * Tests that the general MDC logging information is set as expected.
 *
 */
public class CasdaLoggingSettingsTest
{

    private static final String UUID_FORMAT = "^[0-9a-f\\-]{36}$";
    
    /**
     * Make sure the mdc is clear before starting
     */
    @Before
    public void setUp()
    {
        MDC.clear();
    }

    /**
     * Tests that the general logging settings are set in MDC when it is empty.
     */
    @Test
    public void testLoggingSettingsWhenEmpty()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        assertNull(MDC.get(CasdaLoggingSettings.COMPONENT_KEY));
        assertEquals(0, MDC.getCopyOfContextMap().keySet().size());
        loggingSettings.addGeneralLoggingSettings();
        assertEquals("test application", MDC.get(CasdaLoggingSettings.COMPONENT_KEY));
        assertEquals("default", MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));
        assertEquals(2, MDC.getCopyOfContextMap().keySet().size());
        MDC.clear();
    }
    
    /**
     * Tests that the general logging settings are not changed in MDC when it is not empty.
     */
    @Test
    public void testLoggingSettingsWhenNotEmpty()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        loggingSettings.addGeneralLoggingSettings();
        loggingSettings.updateLoggingInstanceId("other instance id");
        assertEquals("other instance id", MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));
        
        loggingSettings.addGeneralLoggingSettings();
        assertEquals("other instance id", MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));
        assertEquals(2, MDC.getCopyOfContextMap().keySet().size());
        MDC.clear();
    }

    /**
     * Tests that the instance id is added by the addLoggingInstanceId method when the current MDC value for instance
     * id is null.
     */
    @Test
    public void testAddLoggingInstanceIdWhenNull()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        assertNull(MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));
        loggingSettings.addLoggingInstanceId();
        assertTrue(Pattern.matches(UUID_FORMAT, MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY)));
        MDC.clear();
    }

    /**
     * Tests that the instance id is added by the addLoggingInstanceId method when the current MDC value for instance
     * id is the default value.
     */
    @Test
    public void testAddLoggingInstanceIdWhenDefault()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        loggingSettings.addGeneralLoggingSettings();
        assertEquals("default", MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));

        loggingSettings.addLoggingInstanceId();
        assertTrue(Pattern.matches(UUID_FORMAT, MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY)));
        MDC.clear();
    }

    /**
     * Tests that the instance id is NOT changed by the addLoggingInstanceId method when the current MDC value for instance
     * id is not null or the default value.
     */
    @Test
    public void testAddLoggingInstanceIdDoesntChangeWhenHasValue()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        loggingSettings.addLoggingInstanceId();

        String currentInstanceId = MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY);
        assertTrue(Pattern.matches(UUID_FORMAT, currentInstanceId));

        loggingSettings.addLoggingInstanceId();
        assertEquals(currentInstanceId, MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));

        MDC.clear();
    }

    /**
     * Tests that the instance id is set to the given value using the updateLoggingInstanceId method.
     */
    @Test
    public void testUpdateLoggingInstanceId()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        loggingSettings.addLoggingInstanceId();

        String currentInstanceId = MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY);
        
        assertTrue(Pattern.matches(UUID_FORMAT, currentInstanceId));

        loggingSettings.updateLoggingInstanceId("dummy instance id");
        assertEquals("dummy instance id", MDC.get(CasdaLoggingSettings.INSTANCE_ID_KEY));

        MDC.clear();
    }

    /**
     * Tests that clearLoggingSettings method clears the MDC.
     */
    @Test
    public void testClearLoggingSettings()
    {
        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("test application");
        loggingSettings.addGeneralLoggingSettings();
        assertNotNull(MDC.get(CasdaLoggingSettings.COMPONENT_KEY));
        assertTrue(MDC.getCopyOfContextMap().keySet().size() > 0);

        loggingSettings.clearLoggingSettings();
        assertTrue(MDC.getCopyOfContextMap().keySet().size() == 0);

    }

}
