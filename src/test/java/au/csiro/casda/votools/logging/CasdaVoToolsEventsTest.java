package au.csiro.casda.votools.logging;

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


import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests related to Events.
 * 
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 */
public class CasdaVoToolsEventsTest
{

    /**
     * Tests whether all of the event codes have been defined in the properties file.
     */
    @Test
    public void testAllEvents()
    {
        Arrays.asList(CasdaVoToolsEvents.values()).forEach(event -> {
            assertNotNull(event.name() + " has no code defined in event.properties", event.getCode());
            assertNotNull(event.name() + " has no description defined in event.properties", event.getFormatString());
            assertNotNull(event.name() + " has no title defined in event.properties", event.getType());
        });
    }  


}
