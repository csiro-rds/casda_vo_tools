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


import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 
 * Tests to ensure the factory creates the correct message builders.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class CasdaLogMessageBuilderFactoryTest
{

    /**
     * Test that all the known events create a CasdaEventLogMessageBuilder.
     */
    @Test
    public void testFactoryEventLogMessageBuilder()
    {
        for (CasdaCommonEvents event : CasdaCommonEvents.values())
        {
            assertTrue("The factory should create a CasdaEventLogMessageBuilder for known events",
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event) instanceof CasdaEventLogMessageBuilder);

        }
    }

    /**
     * Test that all the log events create a CasdaLogMessageBuilder.
     */
    @Test
    public void testFactoryOtherEventLogMessageBuilder()
    {
        for (LogEvent event : LogEvent.values())
        {
            assertTrue("The factory shoudl create a CasdaLogMessageBuilder for unknown and other log events",
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event) instanceof CasdaLogMessageBuilder);
        }
    }
}
