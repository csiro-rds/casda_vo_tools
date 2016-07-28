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


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.MDC;

/**
 * Logging settings for CASDA - these are stored in ThreadLocal using MDC.
 * 
 * MDC: Mapped Diagnostic Context - this is a map of contextual information used by the logger, so in our case will
 * contain information about the component, instance id, etc and then can be referred to in the logging pattern.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class CasdaLoggingSettings
{
    /**
     * MDC Key for instance id.
     */
    protected static final String INSTANCE_ID_KEY = "instanceid";
    /**
     * MDC key for component.
     */
    protected static final String COMPONENT_KEY = "component";
    /**
     * Default value for instance id.
     */
    protected static final String DEFAULT_INSTANCE_ID = "default";
    

    private String applicationName;

    /**
     * Set up logging. This gets the log4j2.xml properties from the default location.
     * 
     * @param applicationName
     *            the name of the application for the log messages
     * 
     */
    public CasdaLoggingSettings(String applicationName)
    {
        this(applicationName, null);
    }

    /**
     * Set up logging.
     * 
     * @param applicationName
     *            the name of the application for the log messages
     * @param logfilePath
     *            the path to the log config file, preferable to use a path relative to the user.dir, eg
     *            config/Application-log4j2.xml
     */
    public CasdaLoggingSettings(String applicationName, String logfilePath)
    {
        this.applicationName = applicationName;
        if (StringUtils.isNotBlank(logfilePath))
        {
            Configurator.initialize(applicationName, logfilePath);
        }
    }

    /**
     * Add a logging instance id - this is for traceability of a request
     */
    public void addLoggingInstanceId()
    {
        if (MDC.get(INSTANCE_ID_KEY) == null || DEFAULT_INSTANCE_ID.equals(MDC.get(INSTANCE_ID_KEY)))
        {
            String newInstanceId = nextInstanceId();
            MDC.put(INSTANCE_ID_KEY, newInstanceId);
        }
    }

    /**
     * If the instance id is being passed from another application, this method allows you to set it.
     * 
     * @param instanceId
     *            the instance id for logging
     */
    public void updateLoggingInstanceId(String instanceId)
    {
        MDC.put(INSTANCE_ID_KEY, instanceId);
    }

    /**
     * Update the default information for the logging MDC, unless there is already information in there.
     */
    public void addGeneralLoggingSettings()
    {
        if (MDC.get(INSTANCE_ID_KEY) == null)
        {
            MDC.put(COMPONENT_KEY, applicationName);
            MDC.put(INSTANCE_ID_KEY, DEFAULT_INSTANCE_ID);
        }
    }

    /**
     * Clears the logging settings from the thread.
     */
    public void clearLoggingSettings()
    {
        MDC.clear();
    }

    /**
     * Generate the next unique instance id.
     * 
     * @return String an instance id
     */
    private static String nextInstanceId()
    {
        return UUID.randomUUID().toString();
    }

}
