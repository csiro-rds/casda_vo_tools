package au.csiro.casda.votools.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;

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
    
}
