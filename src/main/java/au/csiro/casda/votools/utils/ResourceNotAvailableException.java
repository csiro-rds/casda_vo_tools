package au.csiro.casda.votools.utils;

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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception for when a resource is not currently available to be downloaded (e.g. requires retrieval from tape)
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotAvailableException extends RuntimeException
{
    private static final long serialVersionUID = -9151423532958177468L;

    /**
     * Constructor with message
     * 
     * @param message
     *            error message
     */
    public ResourceNotAvailableException(String message)
    {
        super(message);
    }

    /**
     * Constructor with throwable
     * 
     * @param t
     *            exception to wrap
     */
    public ResourceNotAvailableException(Throwable t)
    {
        super(t);
    }
}