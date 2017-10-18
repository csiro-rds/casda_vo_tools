package au.csiro.casda.votools.examples;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Example model used to encapsulate a single TAP example. Consumed by examples.jsp
 * 
 * Copyright 2017, CSIRO Australia All rights reserved.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Example")
public class TapExample
{
    private String id;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Query")
    private String query;

    @XmlElement(name = "Tables")
    private Set<String> tables;

    @XmlElement(name = "Description")
    private String description;

    /**
     * Default constructor.
     */
    public TapExample()
    {
        this.tables = new HashSet<>();
    }

    /**
     * Build Tap examples from map.
     * 
     * @param example
     *            The Map of example properties and values.
     */
    public TapExample(Map<String, String> example)
    {
        this();
        if (example != null)
        {
            this.name = example.get(TapExamplesService.ExampleKeys.NAME.getKey());
            this.query = example.get(TapExamplesService.ExampleKeys.QUERY.getKey());
            String tbls = example.get(TapExamplesService.ExampleKeys.TABLES.getKey());
            this.description = example.get(TapExamplesService.ExampleKeys.DESCRIPTION.getKey());
            if (tbls != null)
            {
                for (String table : tbls.split(","))
                {
                    tables.add(table.trim());
                }
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public Set<String> getTables()
    {
        return tables;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setTables(Set<String> tables)
    {
        this.tables = tables;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
