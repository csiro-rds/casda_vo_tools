package au.csiro.casda.votools.config;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;


/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Tests of the Configuration class which do not require any database updates
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class ConfigurationTestNoDb
{

    @Test
    public void testYamlPersistenceOfEndpointConfigKeys()
    {
        Configuration config = new Configuration();
        ConfigurationRegistry registry = new ConfigurationRegistry();
        Configuration.setRegistry(registry);
        config.addDefaults();
        String yaml = config.toString(false);
        // Make sure that tap.votable.xsl is in the TAP endpoint
        assertThat(yaml, stringContainsInOrder("endPoints:", "TAP:", "tap.votable.xsl:", "SCS:"));
        // Make sure that tap.votable.xsl is not in the general list
        assertThat(yaml, not(stringContainsInOrder("application.base.url:", "tap.votable.xsl:")));
    }
    
    
    @Test
    public void testYamlFileConfigurationLoading() throws ConfigurationException
    {
        ConfigurationRegistry registry = new ConfigurationRegistry();
        Configuration.setRegistry(registry);
        File testYamlFile = new File("src/test/resources/unittest/application.yaml");
        Configuration config = Configuration.newConfiguration(testYamlFile, registry);
        assertEquals("casda.image_cube, ^cube-[0-9]+$, application/fits", config.get("datalink.resource.image_cube"));
        assertEquals("test.catalogue, ^catalogue-[a-zA-Z0-9_]+$, test-content-type",
                config.get("datalink.resource.catalogue"));
    }
    
    @Test
    public void testBrokenYamlFileConfigurationLoading() throws ConfigurationException
    {
        ConfigurationRegistry registry = new ConfigurationRegistry();
        Configuration.setRegistry(registry);
        File testYamlFile = new File("src/test/resources/unittest/broken_application.yaml");
        ConfigurationException configException = assertThrows(ConfigurationException.class, () -> {
            Configuration.newConfiguration(testYamlFile, registry);
        });
        
        assertThat(configException.getMessage(), 
                containsString("Expected 3 values for config value: datalink.resource.moment_map but received: 2"));
    }
    
}
