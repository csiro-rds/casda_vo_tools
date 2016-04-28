package au.csiro.casda.services.dto;

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


import java.io.Serializable;

/**
 * Interface for messages that are to be displayed to a user.
 *
 */
public interface Message extends Serializable
{

    /**
     * Possible message codes
     *
     */
    public enum MessageCode
    {
        /**
         * Success message code
         */
        SUCCESS,
        /**
         * Failure message code
         */
        FAILURE,
        /**
         * User has permission to access a page or function
         */
        ACCESS, 
        /**
         * User does not have permission to access a page or function
         */
        NO_ACCESS
    }
    
    /**
     * Gets the message code
     * 
     * @return the message code
     */
    public MessageCode getMessageCode();
    
    /**
     * Gets the message
     * 
     * @return the message details
     */
    public String getMessage();
    
}
