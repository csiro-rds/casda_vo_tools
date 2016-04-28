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


import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * Casda Message Builder interface, for constructing log and email messages.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 * 
 * @param <T>
 *            the CasdaMessageBuilder subtype
 */
public interface CasdaMessageBuilder<T extends CasdaMessageBuilder<?>>
{
    /**
     * Add a Date object as an argument to the message format string.
     * 
     * @param dateTime
     *            date time
     * @return the builder object
     */
    public T add(Date dateTime);

    /**
     * Add a list of files as an argument to the message format string.
     * 
     * @param files
     *            list
     * @return the builder object
     */
    public T add(List<Path> files);

    /**
     * Add a custom string to the end of the formatted message.
     * 
     * @param customMessage
     *            extra information, not standard message content
     * @return the builder object
     */
    public T addCustomMessage(String customMessage);

    /**
     * Add the time taken to the message.
     * 
     * @param timeTaken
     *            the amount of time an operation took in milliseconds
     * @return the builder object
     */
    public T addTimeTaken(long timeTaken);

    /**
     * Add an object as an argument to the message format
     * 
     * @param object
     *            this could be an integer, String, etc
     * @return the builder object
     */
    public T add(Object object);

    /**
     * Adds each of the objects in the list as arguments to the message format
     * 
     * @param objects
     *            the list of objects
     * @return the builder object
     */
    public T addAll(List<Object> objects);

    /**
     * 
     * @return the message as a string
     */
    public String toString();

}
