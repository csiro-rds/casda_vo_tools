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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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
public class VoToolsApplication extends SpringBootServletInitializer implements ResourceLoaderAware 
{
    private static Logger logger = LoggerFactory.getLogger(VoToolsApplication.class);

    /**
     * Application name - used in log messages.
     */
    public static final String APPLICATION_NAME = "CasdaVoTools";

    private static final String LOG_PROPERTIES_FILE_SUFFIX = "-log4j2.xml";

    private static final String CONFIG_FOLDER = "config";

    private ConfigLocation configLocation;
    
    /**
     * @return A bean to hold the configuration locations.
     */
    @Bean
    public ConfigLocation getConfigLocation()
    {
        return configLocation;
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        String logPropertiesLocation = getLogPropertiesLocation();
        File logPropertiesFile = new File(logPropertiesLocation);
        CasdaLoggingSettings loggingSettings;
        boolean useLogPropertiesFile = logPropertiesFile.exists() && logPropertiesFile.canRead();
        if (useLogPropertiesFile)
        {
            loggingSettings = new CasdaLoggingSettings(APPLICATION_NAME, logPropertiesLocation);
        }
        else
        {
            loggingSettings = new CasdaLoggingSettings(APPLICATION_NAME);
        }

        loggingSettings.addGeneralLoggingSettings();

        if (useLogPropertiesFile)
        {
            logger.info("Log properties location: {}", logPropertiesFile.getAbsolutePath());
        }
        else
        {
            logger.info("Using default log properties. Custom log properties can be set at : {}",
                    logPropertiesFile.getAbsolutePath());
        }

        SpringApplicationBuilder app = application.sources(VoToolsApplication.class);
        app.profiles("casda_vo_tools");
        
        return app;
    }

    private Set<String> checkResourceLocations(ResourceLoader resourceLoader, String keyName)
    {
        Set<String> foundLocations = new HashSet<>();
        String[] locations =  System.getProperty(keyName).split(",");
        for (String loc : locations)
        {
            Resource res = resourceLoader.getResource(loc);
            try
            {
                File file = res.getFile();
                if (!file.exists())
                {
                    logger.warn("Unable to find folder specified in '" + keyName + "' of " + file.getCanonicalPath());
                }
                else if (!file.canRead())
                {
                    logger.warn("Unable to read folder specified in '" + keyName + "' of " + file.getCanonicalPath());
                }
                else
                {
                    foundLocations.add(file.getCanonicalPath());
                }
            }
            catch (IOException e)
            {
                logger.warn("Unable to find folder specified in '" + keyName + "' of " + loc);
            }
        }
        return foundLocations;
    }

    private Set<String> checkFileLocations(File[] locations)
    {
        Set<String> foundLocations = new HashSet<>();
        for (File file : locations)
        {
            try
            {
                if (!file.exists())
                {
                    logger.warn("Unable to find folder " + file.getCanonicalPath());
                }
                else if (!file.canRead())
                {
                    logger.warn("Unable to read folder " + file.getCanonicalPath());
                }
                else
                {
                    foundLocations.add(file.getCanonicalPath());
                }
            }
            catch (IOException e)
            {
                logger.warn("Unable to find folder " + file.getPath());
            }
        }
        return foundLocations;
    }

    /**
     * Gets the location of the log properties file.
     * 
     * @return String application's log file in the config folder.
     */
    public static String getLogPropertiesLocation()
    {
        return CONFIG_FOLDER + File.separator + APPLICATION_NAME + LOG_PROPERTIES_FILE_SUFFIX;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        // Allow:
        // 1. Direct user specification of config via casda.votools.configdir (ignored if missing or blank)
        Set<String> foundLocations = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(System.getProperty("spring.config.location")))
        {
            foundLocations.addAll(checkResourceLocations(resourceLoader, "spring.config.location"));
                
            if (foundLocations.isEmpty())
            {
                logger.error(
                        "Unable to find any of the locations in " + System.getProperty("spring.config.location"));
            }
        }
        else
        {
            if (StringUtils.isNotBlank(System.getProperty("spring.config.additional-location")))
            {
                foundLocations.addAll(checkResourceLocations(resourceLoader, "spring.config.additional-location"));
            }
            else
            {
                logger.info("Custom config locations can be specified using the -Dspring.config.location= and "
                        + "-Dspring.config.additional-location command line parameters.");
            }

            File userDir = new File(System.getProperty("user.dir"));
            File configDir = new File(userDir, CONFIG_FOLDER);
            foundLocations.addAll(checkFileLocations(new File[] {userDir, configDir}));
        }

        configLocation = new ConfigLocation(foundLocations);

        if (!foundLocations.isEmpty())
        {
            logger.info("Config being read from " + StringUtils.join(foundLocations, " , "));
        }
    }

    /**
     * A container for a list of configuration locations for the application. 
     */
    public static class ConfigLocation
    {
     
        private Set<String> configLocations;
        
        /**
         * Create a new ConfigLocation instance.
         * @param configLocations The locations at which to search for config. 
         */
        public ConfigLocation(Set<String> configLocations)
        {
            this.configLocations = configLocations;
        }

        public Set<String> getConfigLocations()
        {
            return configLocations;
        }
        
        /**
         * Retrieve the current location of the named config file, or its default location if it does not yet exist.
         * 
         * @param configFileName The filename of the config file.
         * @param logMissing Should a log entry be made if the file is not present?
         * @return The file if found, or a file object for its default location if not found. 
         */
        public File getConfigFile(String configFileName, boolean logMissing)
        {
            List<String> missingFiles = new ArrayList<>();
            File configFile = null;
            for (String location : configLocations)
            {
                configFile = new File(location, configFileName);
                if (configFile.exists())
                {
                    break;
                }
                
                missingFiles.add(configFile.getAbsolutePath());
                configFile = null;
            }
            
            if (configFile == null)
            {
            	if (logMissing)
            	{
					logger.info("Unable to find configuration file " + configFileName + " - tried "
							+ StringUtils.join(missingFiles, " , "));
            	}
                String defaultLocation = configLocations.isEmpty() ? "config" : configLocations.iterator().next();
                configFile = new File(defaultLocation, configFileName);
            	if (logMissing)
            	{
            		logger.info("Defaulting to " + configFile.getAbsolutePath());
            	}
            }
            return configFile;
        }

    }
}
