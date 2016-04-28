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


/**
 * 
 * Factory for creating log message builders for given event types.
 * 
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 *
 */
public class CasdaLogMessageBuilderFactory
{
    /**
     * Create the appropriate log message builder for a given event.
     * 
     * @param eventType this could be a known event, or a {@link LogEvent}
     * @return a {@link CasdaEventLogMessageBuilder} for known events, and otherwise a {@link CasdaLogMessageBuilder}
     */
    public static CasdaMessageBuilder<?> getCasdaMessageBuilder(CasdaEvent eventType)
    {
        if (LogEvent.UNKNOWN_EVENT == eventType || LogEvent.TIMED == eventType || LogEvent.DEBUG == eventType)
        {
            return new CasdaLogMessageBuilder((LogEvent) eventType);
        }
        else
        {
            return new CasdaEventLogMessageBuilder(eventType);
        }
    }
}
