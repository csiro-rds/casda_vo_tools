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


import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * 
 * Message builder for log events, eg unknown events, information about time taken and other debug.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaLogMessageBuilder implements CasdaMessageBuilder<CasdaLogMessageBuilder>
{
    private LogEvent logEvent;
    private String formatString;
    private long timeTaken;
    private StringBuilder stringBuilder = new StringBuilder();

    /**
     * Creates a CasdaLogMessageBuilder for the given event
     * @param logEvent the event type to build
     */
    public CasdaLogMessageBuilder(LogEvent logEvent)
    {
        this.logEvent = logEvent;
        this.formatString = logEvent.getFormatString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaLogMessageBuilder add(Date dateTime)
    {
        stringBuilder.append(CasdaFormatter.formatDateTimeForLog(dateTime));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaLogMessageBuilder add(List<Path> files)
    {
        stringBuilder.append(CasdaFormatter.formatFileListForLog(files));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaLogMessageBuilder addCustomMessage(String customMessage)
    {
        stringBuilder.append(customMessage);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaLogMessageBuilder addTimeTaken(long time)
    {
        timeTaken = time;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaLogMessageBuilder add(Object object)
    {
        stringBuilder.append(object);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public CasdaLogMessageBuilder addAll(List<Object> objects)
    {
        CasdaLogMessageBuilder builder = this;
        for (Object object : objects)
        {
            builder = this.add(object);
        }
        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if (logEvent == LogEvent.TIMED)
        {
            return String.format(formatString, this.timeTaken, stringBuilder.toString()).replaceAll("\n", "\\\\n");
        }
        else
        {
            return String.format(formatString, stringBuilder.toString()).replaceAll("\n", "\\\\n");
        }
    }

}
