package au.csiro.casda.votools.result;

import org.springframework.dao.DataAccessException;

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
 * This exception represents a failure to process a TAP query for other than a database or SQL problem.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@SuppressWarnings("serial")
public class ProcessingException extends DataAccessException
{

    /**
     * Creates a new instance of TapProcessingException.
     * 
     * @param msg
     *            The description of the failure.
     */
    public ProcessingException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new instance of TapProcessingException.
     * 
     * @param msg
     *            The description of the failure.
     * @param cause
     *            The exception that is the root cause.
     */
    public ProcessingException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
