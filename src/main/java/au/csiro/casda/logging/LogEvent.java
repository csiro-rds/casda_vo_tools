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
 * Log Event types that don't have an evento code.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public enum LogEvent implements CasdaEvent
{

    /** 
     * Unknown event - uses a default unknown event code Exxx.
     */
    UNKNOWN_EVENT("[Exxx] %s"), 
    /** 
     * Debug / info event - used for logging general debug and info messages, with no associated event code. 
     */
    DEBUG("%s"), 
    /**
     * Timed event - used for logging events with duration information, but no associated event code.
     */
    TIMED("[duration:%d] %s");

    private String formatString;

    private LogEvent(String formatString)
    {
        this.formatString = formatString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatString()
    {
        return this.formatString;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns null - unknown events do not have an event code.
     */
    @Override
    public String getCode()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the name of the event enum, ie UNKNOWN_EVENT, DEBUG and TIMED.
     */
    @Override
    public String getType()
    {
        return this.name();
    }
}