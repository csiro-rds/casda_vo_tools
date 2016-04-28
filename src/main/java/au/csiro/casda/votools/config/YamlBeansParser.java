package au.csiro.casda.votools.config;

import java.io.StringReader;
import java.io.StringWriter;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

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
public class YamlBeansParser implements YamlParser
{

    /**
     * Parameterless constructor
     */
    public YamlBeansParser()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.YamlParser#serialise(java.lang.Object)
     */
    @Override
    public String serialise(Object object) throws ConfigurationException
    {
        StringWriter strWriter = new StringWriter();
        YamlWriter writer = new YamlWriter(strWriter);
        init(writer.getConfig());
        try
        {
            writer.write(object);
            writer.close();
        }
        catch (YamlException e)
        {
            throw new ConfigurationException(e);
        }
        return strWriter.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.YamlParser#parse(java.lang.String)
     */
    @Override
    public Object parse(String string) throws ConfigurationException
    {

        YamlReader reader = new YamlReader(new StringReader(string));
        init(reader.getConfig());
        try
        {
            return reader.read();
        }
        catch (YamlException e)
        {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Sets parsing shortcuts and properties
     * 
     * @param config
     *            YamlBeans parser configuration object
     */
    void init(YamlConfig config)
    {
        config.setPropertyElementType(Configuration.class, "tables", TableConfig.class);
        config.setPropertyElementType(Configuration.class, "endPoints", EndPoint.class);
        config.setClassTag("Configuration", Configuration.class);
        config.setClassTag("Map", java.util.LinkedHashMap.class);
        config.setClassTag("List", java.util.LinkedList.class);
        config.setClassTag("Column", ColumnConfig.class);
    }

}
