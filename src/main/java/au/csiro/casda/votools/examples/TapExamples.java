package au.csiro.casda.votools.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.input.XmlStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Container class for TAP Examples (TapExamples.java)
 * 
 * Copyright 2017, CSIRO Australia All rights reserved.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Examples")
public class TapExamples
{
    @XmlElement(name = "Example", type = TapExample.class)
    private List<TapExample> tapExamples;

    private static Logger logger = LoggerFactory.getLogger(TapExamples.class);

    /**
     * Default
     */
    public TapExamples()
    {
        this.tapExamples = new ArrayList<>();
    }

    /**
     * Create examples from Set of maps.
     * 
     * @param examples
     *            The examples.
     */
    public TapExamples(Set<Map<String, String>> examples)
    {
        this();
        if (examples != null)
        {
            for (Map<String, String> m : examples)
            {
                this.tapExamples.add(new TapExample(m));
            }
            generateIds();
        }
    }

    public List<TapExample> getTapExamples()
    {
        return tapExamples;
    }

    /**
     * Set examples.
     * 
     * @param tapExamples
     *            The Tap Examples to set.
     */
    public void setTapExamples(List<TapExample> tapExamples)
    {
        this.tapExamples = tapExamples;
        for (TapExample te : this.tapExamples)
        {
            Set<String> tables = new HashSet<>();
            for (String table : te.getTables())
            {
                for (String t : table.split(","))
                {
                    tables.add(t.trim());
                }
            }
            te.setTables(tables);
        }
        this.generateIds();
    }

    /**
     * Generate Id for the TAP Example. Required for wrapping div on examples page.
     */
    public void generateIds()
    {
        for (TapExample example : tapExamples)
        {
            String id = example.getName();
            if (id != null && !id.isEmpty())
            {
                // remove white space
                id = id.replaceAll("\\s+", "");
                example.setId(id + "_" + tapExamples.indexOf(example));
            }
        }
    }

    /**
     * Check if this Container for examples has examples.
     * 
     * @return True if examples exist, false, otherwise.
     */
    public boolean hasExamples()
    {
        if (this.tapExamples == null)
        {
            return false;
        }
        return this.tapExamples.size() > 0;
    }

    /**
     * Load TAP Examples from xml file.
     * 
     * @param file
     *            File location of the Tap Examples configuration xml
     * @return True if the operation succeeded, otherwise, false.
     */
    public boolean loadFromXmlConfig(File file)
    {
        JAXBContext context;

        if (!file.exists())
        {
            logger.warn("TAP Examples config example file does not exist:" + file);
            return false;
        }

        try
        {
            context = JAXBContext.newInstance(TapExamples.class);
            Unmarshaller um = context.createUnmarshaller();
            XmlStreamReader reader = new XmlStreamReader(file);
            TapExamples tapExamples = (TapExamples) um.unmarshal(reader);
            setTapExamples(tapExamples.getTapExamples());

            return true;
        }
        catch (Exception e)
        {
            logger.warn("Error reading TAP Examples from xml file: " + file);
        }
        return false;
    }

}
