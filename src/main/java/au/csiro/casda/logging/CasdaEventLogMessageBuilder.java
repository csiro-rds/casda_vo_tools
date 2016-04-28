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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * Message builder for known events, see https://wiki.csiro.au/display/CASDA/APPENDIX+F%3A+Events+and+Notifications
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaEventLogMessageBuilder implements CasdaMessageBuilder<CasdaEventLogMessageBuilder>
{

    /**
     * Format string for the log message.
     */
    static final String EVENT_MESSAGE_FORMAT = "[%s] [%s] [%s] %s";
    /**
     * Format string for duration information in log message.
     */
    static final String TIMED_EVENT_MESSAGE_FORMAT = "[duration:%d]";
    
    private CasdaEvent event;
    private String formatString;
    /**
     * List of arguments passed to the event format string.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected List<Object> args = new ArrayList<>();
    private String customMessage = "";
    /**
     * Flag indicating this event has duration information
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected boolean timedEvent = false;
    /**
     * Duration of the event.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected long timeTaken = -1;

    /**
     * Creates a CasdaEventLogMessageBuilder for the given event
     * @param event the event type to build
     */
    public CasdaEventLogMessageBuilder(CasdaEvent event)
    {
        this.event = event;
        this.formatString = event.getFormatString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder add(Date dateTime)
    {
        args.add(CasdaFormatter.formatDateTimeForLog(dateTime));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder add(List<Path> files)
    {
        args.add(CasdaFormatter.formatFileListForLog(files));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder addCustomMessage(String customMessage)
    {
        this.customMessage = customMessage;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder addTimeTaken(long timeInMillis)
    {
        this.timeTaken = timeInMillis;
        this.timedEvent = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder add(Object object)
    {
        args.add(object);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CasdaEventLogMessageBuilder addAll(List<Object> objects)
    {
        CasdaEventLogMessageBuilder builder = this;
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
        StringBuilder message = new StringBuilder();
        if (timedEvent)
        {
            message.append(String.format(TIMED_EVENT_MESSAGE_FORMAT, timeTaken));
            message.append(" ");
        }
        if (event != null)
        {
            message.append(String.format(
                    EVENT_MESSAGE_FORMAT, 
                    event.getCode(), 
                    event.getType(),
                    String.format(formatString, args.toArray()).replaceAll("\n", "\\\\n"), 
                    customMessage));
        }
        else
        {
            message.append(String.format(formatString, args.toArray()).replaceAll("\n", "\\\\n"));
        }
        return message.toString();
    }

}
