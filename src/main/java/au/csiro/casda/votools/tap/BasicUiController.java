package au.csiro.casda.votools.tap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.Utils;

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
 * Basic HTML UI Controller
 * 
 * Provides a simple HTML UI to the various TAP functions.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Controller
public class BasicUiController extends Configurable
{
    private Configuration config;
    
    private String buildNumber = "Unknown";
    
    private String environment = "Unknown";
    
    private String css;
    
    private String logoUrl;
    
    private static Logger logger = LoggerFactory.getLogger(BasicUiController.class);
    
    /**
     * Constructor
     * @param configRegistry the configuration registry 
     */
    @Autowired
    public BasicUiController(ConfigurationRegistry configRegistry)
    {
        try
        {
            configRegistry.register(this);
        }
        catch (ConfigurationException e)
        {
            logger.warn("Controller could not be registered with the configuration registry", e);
        }
        
        if(config != null)
        {
            environment = config.get(ConfigValueKeys.ENVIRONMENT);
            css = config.get(ConfigValueKeys.CSS);
            logoUrl =  config.get(ConfigValueKeys.LOGO_URL);
        }

        Properties prop;
        try
        {
            prop = Utils.loadProperties("version.properties");
            buildNumber = prop.getProperty(ConfigValueKeys.BUILD_NUMBER);
        }
        catch (IOException e)
        {
            logger.warn("Version properties could not be loaded", e);
        }
        
    }
    
    /**
     * Implementation of GET /tap -> text/html
     * 
     * @return the model-and-view
     * @throws ConfigurationException 
     */
    @RequestMapping(value = "/tap", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView show() throws ConfigurationException
    {
        ModelAndView result = new ModelAndView("tap/show");

        List<String> outputFormats = Arrays.asList(OutputFormat.values()).stream()
                .flatMap(outputFormat -> outputFormat.getIdentifiers().stream()).collect(Collectors.toList());
        result.addObject("outputFormats", outputFormats);
        result.addObject("serverName", environment);
        result.addObject("buildNumber", buildNumber);
        result.addObject("css", css);
        result.addObject("logo", logoUrl);
        return result;
    }
    
    /**
     * Implementation of GET /datalink -> text/html
     * 
     * @return the model-and-view
     * @throws ConfigurationException 
     */
    @RequestMapping(value = "/datalink", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView showdl() throws ConfigurationException
    {
        ModelAndView result = new ModelAndView("datalink/showdl");
        result.addObject("serverName", environment);
        result.addObject("buildNumber", buildNumber);
        result.addObject("css", css);
        result.addObject("logo", logoUrl);
        return result;
    }

    @Override
    public void setConfiguration(Configuration config)
    {
        this.config = config;
    }

    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        return true;
    }


    @Override
    public void invalidate()
    {

    }

}
