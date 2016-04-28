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
 * Interface for Casda Events, for logging and errors.
 * 
 * Implements default methods for formatting log event messages.
 * 
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 *
 */
public interface CasdaEvent
{

    /**
     * Log levels
     *
     */
    enum Level
    {
        /**
         * Debug log level
         */
        DEBUG, 
        /**
         * Info log level
         */
        INFO, 
        /**
         * Warning log level
         */
        WARN, 
        /**
         * Error log level
         */
        ERROR
    }

    /**
     * Get the format string for the event, using standard java formatting strings.
     * 
     * http://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
     * 
     * @return format string for the event
     */
    String getFormatString();

    /**
     * Get the event code.
     * 
     * @return the event code name.
     */
    String getCode();

    /**
     * Get the event type.
     * 
     * @return the type of event.
     */
    String getType();


}
