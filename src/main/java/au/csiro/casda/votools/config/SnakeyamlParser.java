package au.csiro.casda.votools.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

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
 * Implements YAML serialisation and deserialisation of Configuration objects.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class SnakeyamlParser implements YamlParser
{

    /** YAML parser */
    private Yaml yaml;

    /** Max line width in YAML dumps */
    private static final int LINE_WIDTH = 120;

    /** Standard ident in YAML dumps */
    private static final int INDENT = 4;
    
    /**
     * Parameterless constructor
     */
    public SnakeyamlParser()
    {
        Representer representer = new SkipNullRepresenter();
        DumperOptions options = new DumperOptions();
        options.setWidth(LINE_WIDTH);
        options.setIndent(INDENT);

        yaml = new Yaml(representer, options);        
    }

    /* (non-Javadoc)
     * @see au.csiro.casda.votools.config.YamlParser#serialise(java.lang.Object)
     */
    @Override
    public String serialise(Object object)
    {
        return yaml.dump(object);
    }

    /* (non-Javadoc)
     * @see au.csiro.casda.votools.config.YamlParser#parse(java.lang.String)
     */
    @Override
    public Object parse(String string)
    {
        return yaml.load(string);
    }


    /**
     * Makes Snakeyaml skip null values
     * 
     */
    private static class SkipNullRepresenter extends Representer
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.yaml.snakeyaml.representer.Representer#representJavaBeanProperty(java.lang.Object,
         * org.yaml.snakeyaml.introspector.Property, java.lang.Object, org.yaml.snakeyaml.nodes.Tag)
         */
        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                Tag customTag)
        {
            if (propertyValue == null || property.getName().startsWith("i_"))
            {
                return null;
            }
            else
            {
                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        }
    }

}
