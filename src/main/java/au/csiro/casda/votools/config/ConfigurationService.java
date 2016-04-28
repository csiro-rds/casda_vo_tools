package au.csiro.casda.votools.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.config.Configuration.Action;
import au.csiro.casda.votools.config.Configuration.Change;

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
 * A service to perform configuration tasks.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Service
@Scope("singleton")
public class ConfigurationService
{
    private static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private ConfigurationRegistry configRegistry;

    /**
     * Processes a request: parses submitted configuration and executes requested action. Then exports the result as a
     * YAML string. If an exception happens, returns its message as a result.
     * 
     * @param request
     *            HTTP servlet request object
     * @param response
     *            HTTP servlet responce object
     * @param paramsMap
     *            parameters map
     * @return either resulting configuration text or exception message
     * @throws Exception
     *             if there were DB or configuration problems
     */
    String process(HttpServletRequest request, HttpServletResponse response, Map<String, String> paramsMap)
            throws Exception
    {
        Configuration current = null;
        try
        {
            current = configRegistry.getCurrent();
            String config = paramsMap.get("config");
            YamlParser parser = new YamlBeansParser();
            String action = paramsMap.get("submit");
            if ("CURRENT".equals(action))
            {
                return current.act(Action.CURRENT, Change.NONE);
            }
            String changeLevel = paramsMap.get("changelevel");
            Configuration configuration = new Configuration(parser, config);
            configuration.setChangeLevel(Configuration.Change.valueOf(changeLevel));
            if (!StringUtils.equals(current.get(ConfigValueKeys.CONNECTION_URL),
                    configuration.get(ConfigValueKeys.CONNECTION_URL)))
            {
                configRegistry.switchConfiguration(configuration, true);
            }
            else
            {
                current = null;
            }
            String newConfiguration = configuration.act(Action.valueOf(action), Change.valueOf(changeLevel));
            return newConfiguration;
        }
        catch (ClassCastException e) // can be caused by submitting invalid configuration text
        {
            if ((e.getMessage().contains("cannot be cast to au.csiro.casda.votools.config.Configuration")))
            {
                throw new ConfigurationException(
                        "The submitted text is not recognised as a valid configuration in YAML format.");
            }
            throw e;
        }
        catch (Exception e)
        { 
            if (current != null && current != configRegistry.getCurrent())
            {
                try
                {
                    configRegistry.switchConfiguration(current, false);
                }
                catch (Exception e1)
                {
                    logger.info("Ignoring error switching back to previous config - it is most likely invalid: "
                            + e1.getMessage());
                }
            }
            throw e;
        }

    }
}
