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

import au.csiro.AbstractMarshallingTest;
import au.csiro.casda.services.dto.Message.MessageCode;

/**
 * Tests the MessageDTO class
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class MessageDTOTest extends AbstractMarshallingTest
{
    @Override
    protected Serializable getTestObject()
    {
        MessageDTO messageDTO = new MessageDTO(MessageCode.SUCCESS, "some message");
        return messageDTO;
    }

}
