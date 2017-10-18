package au.csiro.casda.votools.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.utils.Utils;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Examples service that contains TAP Examples configuration.
 *
 * Copyright 2017, CSIRO Australia All rights reserved.
 */
@Service
public class TapExamplesService extends Configurable
{
    /**
     * Enum for examples nodes (both xml and yaml)
     */
    public enum ExampleKeys
    {
        /**
         * Name
         */
        NAME("name"),

        /**
         * Query
         */
        QUERY("query"),

        /**
         * Tables
         */
        TABLES("tables"),

        /**
         * Description
         */
        DESCRIPTION("description");

        private String key;

        public String getKey()
        {
            return key;
        }

        private ExampleKeys(String key)
        {
            this.key = key;
        }

    }

    private static Logger logger = LoggerFactory.getLogger(TapExamplesService.class);

    /** URL of a TAP Examples page. */
    private String examplesUrl;

    /** List of examples */
    private TapExamples tapExamples;

    private boolean ready;

    private Configuration config;

    /** Examples Page specific params */
    private String environment;

    private String css;

    private String logoUrl;

    private String buildNumber;

    /**
     * Constructor.
     * 
     * @param configRegistry
     *            The ConfigurationRegistry
     * @throws ConfigurationException
     *             The ConfigurationException
     */
    @Autowired
    public TapExamplesService(ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        configRegistry.register(this);
    }

    @Override
    public void setConfiguration(Configuration config)
    {
        ready = false;
        this.config = config;
    }

    @Override
    public boolean isReady() throws ConfigurationException
    {
        if (!ready && config != null)
        {
            EndPoint tap = config.getEndPoint("TAP");
            if (tap != null)
            {
                examplesUrl = tap.get(ConfigKeys.TAP_EXAMPLES_URL.getKey(), examplesUrl);

                if (StringUtils.isEmpty(examplesUrl))
                {
                    tapExamples = new TapExamples(tap.getExamples());
                    if (!this.hasExamples())
                    {
                        tapExamples.loadFromXmlConfig(
                                Configuration.getRegistry().getConfigurationTapExamplesFile());
                    }
                }
                // populate page specific params
                populatePageParams();
                ready = true;
            }
        }
        return ready;
    }

    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    public String getExamplesUrl()
    {
        return examplesUrl;
    }

    /**
     * Builds a ModelAndView response based on the type of configuration that is available.
     * 
     * @param response
     *            Http Response.
     * @return ModelAndView containing TAP Examples, if available.
     */
    public ModelAndView buildResponse(HttpServletResponse response)
    {
        ModelAndView model = new ModelAndView();
        model.addObject("serverName", environment);
        model.addObject("buildNumber", buildNumber);
        model.addObject("css", css);
        model.addObject("logo", logoUrl);

        // if we have a provided url for the examples endpoint, redirect to it.
        if (StringUtils.isNotEmpty(examplesUrl))
        {
            try
            {
                logger.debug("Redirecting to " + examplesUrl);
                response.setStatus(HttpServletResponse.SC_OK);
                response.sendRedirect(examplesUrl);
            }
            catch (Exception e)
            {
                logger.debug("Failed to redirect to examples url: " + examplesUrl);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        // otherwise, build the examples page from config.
        else if (this.tapExamples.hasExamples())
        {
            model.getModel().put("examples", this.tapExamples.getTapExamples());
        }
        else
        {
            // if neither method is available. Return 404. TAP Examples do not exist.
            model.addObject("error", "No examples configured in this environment.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return model;
    }

    private void populatePageParams()
    {
        if (config != null)
        {
            environment = config.get(ConfigValueKeys.ENVIRONMENT);
            css = config.get(ConfigValueKeys.CSS);
            logoUrl = config.get(ConfigValueKeys.LOGO_URL);
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
     * Static convenience method to get examples from a Configuration.
     * 
     * @param config
     *            The Configuration.
     * @return List of tap examples.
     */
    public static List<TapExample> getExamplesFromConfig(Configuration config)
    {
        EndPoint tap = config.getEndPoint("TAP");
        List<TapExample> examplesModels = new ArrayList<>();
        for (Map<String, String> m : tap.getExamples())
        {
            examplesModels.add(new TapExample(m));
        }
        return examplesModels;
    }

    /**
     * Check if there are examples loaded from config.
     *
     * @return Returns true if this service has examples loaded, otherwise, returns false.
     */
    public boolean hasExamples()
    {
        return this.tapExamples != null && this.tapExamples.hasExamples();
    }

    /**
     * Check if this examples service has configured TAP Examples.
     * 
     * @return True if TAP Examples configuration is active, otherwise, false.
     * @throws ConfigurationException
     *             The ConfigurationException.
     */
    public boolean configurationExists() throws ConfigurationException
    {
        if (this.isReady())
        {
            // if we have a provided url for the examples endpoint, redirect to it.
            if (examplesUrl != null)
            {
                return true;
            }

            // otherwise, samples have been provided via config.
            else if (tapExamples.hasExamples())
            {
                return true;
            }
        }

        return false;
    }

}
