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


import java.util.Date;

/**
 * 
 * Common CASDA Event types.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public enum CasdaCommonEvents implements CasdaEvent
{

    /**
     * Notification sent event
     */
    E011(CasdaEvent.Level.INFO, "Notification sent",
    // first %s is recipient list, second is event time, third is notification channel
            "Notification sent to %s regarding E011 at %s by %s", new Class<?>[] { String.class, Date.class,
                    String.class }),
    /**
     * Method started event
     */
    E063(CasdaEvent.Level.INFO, "Method Start", "Started method: %s", new Class[] { String.class }),
    /**
     * Method completed event
     */
    E064(CasdaEvent.Level.INFO, "Method Finish", "Finished method: %s", new Class[] { String.class })
    ;

    private String type;
    private String formatString;
    private final Class<?>[] requiredArgs;
    private CasdaEvent.Level level;

    private CasdaCommonEvents()
    {
        this.requiredArgs = null;
    }

    private CasdaCommonEvents(CasdaEvent.Level level, String type, String formatString, Class<?>[] requiredArgs)
    {
        this.level = level;
        this.type = type;
        this.formatString = formatString;
        this.requiredArgs = requiredArgs;
    }

    /**
     * Get the format string. For known events, this is the Standard Content, see:
     * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return standard java format string representing the standard content for this event see
     *         (http://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html)
     */
    public String getFormatString()
    {
        return this.formatString;
    }

    /**
     * Get the type of event. For known events, this is the Event Title, see:
     * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return event title
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Get the Event Code, see: https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return event code, eg E001
     */
    public String getCode()
    {
        return this.name();
    }

    /**
     * Get the event level - used in the sample LogGenerator to choose the appropriate log level method.
     * 
     * @return event level.
     */
    protected CasdaEvent.Level getLevel()
    {
        return this.level;
    }

    /**
     * Get the list of class types that the format string takes as arguments. This is used in the sample
     * LogGenerator to generate sample content.
     * 
     * @return list of class types required for the format string
     */
    protected Class<?>[] getRequiredArgs()
    {
        return requiredArgs;
    }

}
