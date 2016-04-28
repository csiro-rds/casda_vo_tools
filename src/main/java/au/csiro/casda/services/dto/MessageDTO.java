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


/**
 * Message data transfer object
 * 
 */
public class MessageDTO implements Message
{

    private static final long serialVersionUID = 6253072784993133681L;
    
    private MessageCode messageCode;
    private String message;

    /**
     * Empty constructor, required for json serialisation
     */
    public MessageDTO()
    {
    }

    /**
     * @param messageCode
     *            success or failure
     * @param message
     *            that is the message
     */
    public MessageDTO(MessageCode messageCode, String message)
    {
        this.messageCode = messageCode;
        this.message = message;
    }

    /**
     * Gets the message details.
     * 
     * @return the message details
     */
    @Override
    public String getMessage()
    {
        return message;
    }

    /**
     * Gets the message code
     * 
     * @return the message code
     */
    @Override
    public MessageCode getMessageCode()
    {
        return messageCode;
    }

}
