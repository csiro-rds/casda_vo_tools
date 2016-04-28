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


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import au.csiro.casda.logging.CasdaEvent;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.CasdaMessageBuilder;

/**
 * The known events for the CASDA VO Tools. For more information see
 * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public enum CasdaVoToolsEvents implements CasdaEvent
{
    /**
     * VO query failed
     */
    E060,

    /**
     * Invalid VO query
     */
    E061,

    /**
     * Successful VO query
     */
    E062,

    /**
     * Unexpected exception with VO query
     */
    E098,
    /** 
     * Couldn't execute VO SCS query 
     */
    E142,
    /**
     * Invalid SCS query
     */
    E143,
    /**
     * Successful SCS query
     */
    E144,

    /**
     * Invalid SIA2 query
     */
    E148,
    
    /**
     * Failed to build DataLink links
     */
    E150;


    private static Properties eventProperties = new Properties();

    static
    {
        InputStream propertiesStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("event.properties");
        try
        {
            eventProperties.load(propertiesStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load event properties");
        }

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
        return eventProperties.getProperty(this.getCode() + ".description");
    }

    /**
     * Get the type of event. For known events, this is the Event Title, see:
     * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return event title
     */
    public String getType()
    {
        return eventProperties.getProperty(this.getCode() + ".title");
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
     * @return
     *      a message builder that can be used to build a message of this type
     */
    public CasdaMessageBuilder<?> messageBuilder()
    {
        return CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(this);
    }

}
