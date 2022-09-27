package au.csiro.casda.votools.surveys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Surveys service that contains SIAP1 Surveys configuration.
 *
 * Copyright 2022, CSIRO Australia All rights reserved.
 */
@Service
public class SiapSurveysService extends Configurable
{
    /**
     * Enum for survey nodes (both xml and yaml)
     */
    public enum SurveyKeys
    {
        /**
         * A unique code for the survey
         */
        CODE("code"),

        /**
         * The human readable short name for the survey
         */
        NAME("name"),

        /**
         * The where clause fragment to be used in querying obscore and restricting the results to just this survey.
         */
        WHERE_CLAUSE("whereClause"),

        /**
         * A longer description of the survey
         */
        DESCRIPTION("description"), 
        
        /**
         * The group the survey belongs to (for gathering multiple related surveys)
         */
        GROUP("group");

        private String key;

        public String getKey()
        {
            return key;
        }

        private SurveyKeys(String key)
        {
            this.key = key;
        }

    }

    /** List of surveys */
    private SiapSurveys siapSurveys;

    private boolean ready;

    private Configuration config;

    /**
     * Constructor.
     * 
     * @param configRegistry
     *            The ConfigurationRegistry
     * @throws ConfigurationException
     *             The ConfigurationException
     */
    @Autowired
    public SiapSurveysService(ConfigurationRegistry configRegistry) throws ConfigurationException
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
            EndPoint sia = config.getEndPoint("SIA1");
            if (sia != null)
            {
                siapSurveys = new SiapSurveys(sia.getSurveys());
                if (!this.hasSurveys())
                {
                    String sia1SurveysFileLoc = config.get(ConfigKeys.SIA1_SURVEYS_CONFIG_FILE.getKey(),
                            Configuration.DEFAULT_SIA_SURVEYS_CONFIG);

                    siapSurveys.loadFromXmlConfig(new File(sia1SurveysFileLoc));
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

    /**
     * Trigger refreshing the survey metadata.
     */
    public void refresh()
    {
        ready = false;
    }

    private void populatePageParams()
    {
        if (config != null)
        {
            if (hasSurveys())
            {
                String baseUrl = config.get(ConfigValueKeys.APP_BASE_URL);
                for (SiapSurvey survey : this.siapSurveys.getSiapSurveys())
                {
                    String sia1Endpoint = String.format("%1$ssia1/query?SURVEY=%2$s&amp;", baseUrl, survey.getCode());
                    survey.setEndpoint(sia1Endpoint);
                }
            }
        }
    }

    /**
     * Check if there are surveys loaded from config.
     *
     * @return Returns true if this service has examples loaded, otherwise, returns false.
     */
    public boolean hasSurveys()
    {
        return this.siapSurveys != null && this.siapSurveys.hasSurveys();
    }

    /**
     * Check if this surveys service has configured SIAP1 surveys.
     * 
     * @return True if SIAP1 surveys configuration is active, otherwise, false.
     * @throws ConfigurationException
     *             The ConfigurationException.
     */
    public boolean configurationExists() throws ConfigurationException
    {
        if (this.isReady())
        {
            // surveys have been provided via config.
            if (siapSurveys.hasSurveys())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieve the survey matching the supplied code.
     * 
     * @param code
     *            The code of the survey to be retrieved
     * @return The matching survey, or null if none could be found.
     */
    public SiapSurvey getSurvey(String code)
    {
        if (!hasSurveys())
        {
            return null;
        }
        
        return siapSurveys.getSurvey(code);
    }
    
    /**
     * @return The list of codes for known surveys. 
     */
    public List<String> getSurveyCodeList()
    {
        List<String> codes = new ArrayList<>();
        if (hasSurveys())
        {
            for (SiapSurvey survey : siapSurveys.getSiapSurveys())
            {
                codes.add(survey.getCode());
            }
        }
        
        return codes;
        
    }

    /**
     * @return The list of configured surveys.
     */
    public List<SiapSurvey> getSiapSurveys()
    {
        return siapSurveys.getSiapSurveys();
    }
}
