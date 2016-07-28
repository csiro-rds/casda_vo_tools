package au.csiro.casda.votools;

import java.io.IOException;

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


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.scs.ScsService;
import au.csiro.casda.votools.tap.TapService;
import au.csiro.casda.votools.utils.Utils;

/**
 * UI Controller for the VO Tools application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class VoToolsUiController extends Configurable
{
    private static final Logger logger = LoggerFactory.getLogger(VoToolsUiController.class);

    private TapService tapService;

    private ScsService scsService;
    
    private Configuration config;
    
    private String buildNumber = "Unknown";
    
    private String environment = "Unknown";
    
    private String css;
    
    private String logoUrl;

    /**
     * Constructor
     * 
     * @param tapService
     *            the TAP service
     * @param scsService
     *            the SCS service
     * @param configRegistry the configuration registry 
     */
    @Autowired
    public VoToolsUiController(TapService tapService, ScsService scsService, ConfigurationRegistry configRegistry)
    {
        try
        {
            configRegistry.register(this);
        }
        catch (ConfigurationException e)
        {
            logger.warn("Controller could not be registered with the configuration registry", e);
        }
        
        this.scsService = scsService;
        this.tapService = tapService;
        
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
     * Sample demonstrating a home page.
     * 
     * @param model
     *            the webapp model
     * @return view name
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model)
    {
        String timezone = tapService.getConfig().get("log.timezone");
        String message = tapService.getConfig().get("application.message");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
        model.addAttribute("serverTime", now.toString());
        model.addAttribute("message", message);
        logger.debug("Welcome home! Message is {}. The client timezone is {}.", message, timezone);

        return "home";
    }

    /**
     * Reload all VO service metadata. This will regenerate tables and capabilities documents and utilise the new config
     * for all future VO queries.
     * 
     * @return A simple result message.
     * @throws ConfigurationException  if there were configuration problems
     */
    @RequestMapping(value = "/refreshconfig", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public MessageDTO refreshConfig() throws ConfigurationException
    {
        logger.info("Refreshing the TAP config");
        // Refresh config for services
        tapService.refresh();
        scsService.refresh();
        checkReady();

        return new MessageDTO(MessageCode.SUCCESS, "Refreshed TAP and SCS config");
    }

    /**
     * Checks is this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    void checkReady()
    {
        try
        {
            if (tapService == null || !tapService.isReady() || scsService == null || !scsService.isReady())
            {
                throw new ConfigurationException("UiController is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }
    
    /**
     * Login page
     * 
     * @return login 
     *            String view name of login
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView login()
    {
        ModelAndView result = new ModelAndView("login");
        result.addObject("serverName", environment);
        result.addObject("buildNumber", buildNumber);
        result.addObject("css", css);
        result.addObject("logo", logoUrl);
        
        return result;
    }
    
    /**
     * Logout page
     * 
     * @return logout
     *            String view name of logout
     */
    @RequestMapping(value = "/logoutMessage", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView logout()
    {
        ModelAndView result = new ModelAndView("logout");
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
