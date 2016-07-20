package au.csiro.casda.votools;

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


import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import au.csiro.casda.logging.CasdaLoggingSettings;

/**
 * Initialises spring boot application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 *
 */
@Configuration
// disables spring's automatic datasource configuration because we configure it manually
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class })
@EnableAspectJAutoProxy
@ComponentScan
public class VoToolsApplication extends SpringBootServletInitializer
{
    private static Logger logger = LoggerFactory.getLogger(VoToolsApplication.class);

    /**
     * Application name - used in log messages.
     */
    public static final String APPLICATION_NAME = "CasdaVoTools";

    private static final String CONFIG_FOLDER = "config";

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        File userDir = new File(System.getProperty("user.dir"));
        File configDir = new File(userDir, CONFIG_FOLDER);

        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings(APPLICATION_NAME); 
        loggingSettings.addGeneralLoggingSettings();

        logger.info("Config being read from {} and {}", configDir.getAbsolutePath(), userDir.getAbsolutePath());

        SpringApplicationBuilder app = application.sources(VoToolsApplication.class);
        app.profiles("casda_vo_tools");
        
        return app;
    }

}