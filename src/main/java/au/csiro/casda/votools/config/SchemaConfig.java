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


import java.util.Objects;

/**
 * Schema configuration
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class SchemaConfig extends Options
{
    /** Description key */
    public static final String DESCRIPTION = "description";

    /** Utype key */
    public static final String UTYPE = "utype";

    /** Schema name */
    private String name;

    private Configuration config;

    /**
     * Parameterless constructor
     */
    public SchemaConfig()
    {
        super();
    }

    /* (non-Javadoc)
     * @see au.csiro.casda.votools.config.Options#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof SchemaConfig))
        {
            return false;
        }
        SchemaConfig other = (SchemaConfig) object;
        return Objects.equals(name, other.name) && super.equals(other);
    }

    /* (non-Javadoc)
     * @see au.csiro.casda.votools.config.Options#hashCode()
     */
    @Override
    public int hashCode()
    {
        int code = name == null ? 0 : name.hashCode() ; 
        return code;
    }

    /**
     * A constructor
     * 
     * @param schemaName
     *            schema name
     */
    public SchemaConfig(String schemaName)
    {
        this();
        name = schemaName;
    }

    /**
     * Adds placeholders of needed
     */
    public void export()
    {
        addPlaceholder(DESCRIPTION);
        addPlaceholder(UTYPE);
    }

    /**
     * Get schema name
     * 
     * @return schema name
     */
    public String gtName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Replaces getter to hide it from YAML parser
     * 
     * @return config
     */
    public Configuration gtConfig()
    {
        return config;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

}
