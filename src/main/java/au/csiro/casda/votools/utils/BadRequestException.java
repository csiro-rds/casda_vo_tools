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
 * An exception for bad service requests (which nevertheless map to a controller method). Instead of returning a success
 * response (2xx code) and forwarding to an error page you can throw this exception and return a 400 response code.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException
{
    private static final long serialVersionUID = -1l;

    /**
     * Constructor with message
     * 
     * @param message
     *            error message
     */
    public BadRequestException(String message)
    {
        super(message);
    }

    /**
     * Constructor with throwable
     * 
     * @param t
     *            exception to wrap
     */
    public BadRequestException(Throwable t)
    {
        super(t);
    }
}