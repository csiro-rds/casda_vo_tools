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
 * 
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public interface YamlParser
{

    /**
     * Serialises an object
     * 
     * @param object
     *            the object to serialise
     * @return configuration text in YAML format
     * @throws ConfigurationException if could not serialise 
     */
    public String serialise(Object object) throws ConfigurationException;

    /**
     * Parses a string into an object
     * 
     * @param string
     *            the string to parse
     * @return parsed configuration object
     * @throws ConfigurationException in case of parsing problems
     */
    public Object parse(String string) throws ConfigurationException;

}
