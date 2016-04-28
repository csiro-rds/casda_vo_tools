package au.csiro.casda.votools.config;

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
 * A general exception for unrecoverable exceptions related to configiration
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class ConfigurationException extends Exception
{

    private static final long serialVersionUID = 1L;

    /**
     * Create a new ConfigurationException without information.
     */
    public ConfigurationException()
    {
        super();
    }

    /**
     * Create a new ConfigurationException with a message and a cause.
     * 
     * @param message
     *            The description of the cause of the exception.
     * @param cause
     *            The Exception or Error that caused the problem.
     */
    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create a new ConfigurationException with a plain message
     * 
     * @param message
     *            The description of the cause of the exception.
     */
    public ConfigurationException(String message)
    {
        super(message);
    }

    /**
     * Create a new ConfigurationException with a cause.
     * 
     * @param cause
     *            The Exception or Error that caused the problem.
     */
    public ConfigurationException(Throwable cause)
    {
        super(cause);
    }

}
