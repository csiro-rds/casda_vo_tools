package au.csiro.casda.votools.config;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import au.csiro.casda.votools.config.Configuration.Change;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2020 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Verify the contents of the tap_configuration.yaml file.
 * 
 * Copyright 2020, CSIRO Australia
 * All rights reserved.
 */
public class TapConfigurationTest
{
    @Mock
    private JdbcTemplate template;

    private ConfigurationDAOImpl daoImpl;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        daoImpl = new ConfigurationDAOImpl(template);
    }
    
    @Test
    public void testTapConfiguration() throws Exception
    {
        String tapConfigText = daoImpl.readTapConfiguration();
        daoImpl.getConfig().setChangeLevel(Change.UPDATE);
        assertThat(tapConfigText, startsWith("!Configuration"));
        
        YamlParser parser = new YamlBeansParser();
        Configuration tapConfig = new Configuration(parser, tapConfigText);
        for (TableConfig tableConfig : tapConfig.getTables().values())
        {
            daoImpl.createTable(tableConfig);
            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(template, atLeast(1)).execute(argumentCaptor.capture());
            List<String> sqlCommands = argumentCaptor.getAllValues();
            assertThat(sqlCommands.get(0), startsWith("CREATE TABLE " + tableConfig.gtFullDbTableName()));
            for (int i = 1; i < sqlCommands.size(); i++)
            {
                assertThat(sqlCommands.get(i), startsWith("COMMENT ON "));
                assertThat(sqlCommands.get(i), containsString(tableConfig.gtFullDbTableName()));
            }
            reset(template);
        }
    }

}
