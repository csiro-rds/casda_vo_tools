package au.csiro.casda.votools.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationDAO;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;

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
 * This health indicator runs a query against the database to ensure that our target database is configured.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Component
public class DbHealth extends Configurable implements HealthIndicator
{
    private static Logger logger = LoggerFactory.getLogger(DbHealth.class);
    
    private JdbcTemplate jdbcTemplate;

    private Configuration config;

    private boolean ready;

    /**
     * Constructor
     * 
     * @param configRegistry
     *            The configuration registry
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @Autowired
    public DbHealth(ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        // Register for callbacks when configuration changes.
        configRegistry.register(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.actuate.health.HealthIndicator#health()
     */
    @Override
    public Health health()
    {
        try
        {
            if (!isReady())
            {
                throw new ConfigurationException("DBHealth is not configured.");
            }
            // Run a query that needs pgsphere, it will fail if pgsphere is not active.
            jdbcTemplate.queryForObject("SELECT 1;", Object.class);
            return new Health.Builder().up()
                    .withDetail(ConfigValueKeys.CONNECTION_URL, config.get(ConfigValueKeys.CONNECTION_URL)).build();
        }
        catch (Exception e)
        {
            logger.error("Unable to query database with " + ConfigValueKeys.CONNECTION_URL + ":"
                    + config.get(ConfigValueKeys.CONNECTION_URL));
            return new Health.Builder().down(e)
                    .withDetail(ConfigValueKeys.CONNECTION_URL, config.get(ConfigValueKeys.CONNECTION_URL)).build();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        this.config = config;
        ready = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public boolean isReady() throws ConfigurationException
    {

        if (!ready && config != null)
        {
            ConfigurationDAO dao = config.initDao();
            if (dao != null)
            {
                jdbcTemplate = dao.getTemplate();
                ready = jdbcTemplate != null;
            }

        }
        return ready;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        this.config = null;
        ready = false;

    }
}
