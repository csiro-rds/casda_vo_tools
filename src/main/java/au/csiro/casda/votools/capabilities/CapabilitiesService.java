package au.csiro.casda.votools.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.examples.TapExamplesService;
import au.csiro.casda.votools.scs.ConeSearchTable;
import au.csiro.casda.votools.scs.ScsService;
import au.csiro.casda.votools.tap.TapService;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2014 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Service to generate service capability information
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Service
@ComponentScan(basePackages="au.csiro.casda.votools.examples")
public class CapabilitiesService extends Configurable
{

    private static Logger logger = LoggerFactory.getLogger(CapabilitiesController.class);

    private String baseUrl;

    private String languageName;

    private String languageVersion;

    private String languageDescription;

    private String outputFormatMime;

    private String outputFormatAlias;

    private String retentionPeriodDefault;

    private String retentionPeriodHard;

    private String executionDurationDefault;

    private String executionDurationHard;

    private String outputLimitHard;

    private String uploadEnabled;
    
    private String uploadLimit;

    private float scsMaxRadius;

    private int scsMaxRecords;

    private float scsTestRa;

    private float scsTestDec;

    private String scsTestCatalog;

    private int scsTestVerbose;

    private String scsTestExtras;

    @Autowired
    private TapExamplesService tapExamplesService;
    
    @Autowired
    private ScsService scsService;
    
    @Autowired
    private TapService tapService;
    
    private boolean ready;

    private Configuration config;
    private String ssapOutputLimit;
    private String ssapDefaultMaxrec;
    
    private boolean hasTapExamples;


    /**
     * A constructor
     * 
     * @param configRegistry
     *            Configuration registry
     * @throws ConfigurationException
     *             is there were configuration problems
     */
    @Autowired
    public CapabilitiesService(ConfigurationRegistry configRegistry) throws ConfigurationException
    {
        configRegistry.register(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        ready = false;
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public boolean isReady() throws ConfigurationException
    {
        if (!ready && config != null && scsService != null && scsService.isReady())
        {
            EndPoint scs = config.getEndPoint("SCS");
            EndPoint tap = config.getEndPoint("TAP");
            baseUrl = config.get(ConfigValueKeys.APP_BASE_URL, baseUrl);
            if (StringUtils.isEmpty(baseUrl))
            {
                logger.error("A value for " + ConfigValueKeys.APP_BASE_URL + " must be provided.");
                return false;
            }
            if (!baseUrl.endsWith("/"))
            {
                baseUrl += "/";
            }
            
            ssapOutputLimit = config.get(ConfigKeys.SSAP_OUTPUT_LIMIT.getKey());
            ssapDefaultMaxrec = config.get(ConfigKeys.SSAP_DEFAULT_MAX_REC.getKey());

            if (tap != null)
            {
                languageName = tap.get("tap.language.name", languageName);
                languageVersion = tap.get("tap.language.version", languageVersion);
                languageDescription = tap.get("tap.language.description", languageDescription);
                outputFormatMime = tap.get("tap.output.format.mime", outputFormatMime);
                outputFormatAlias = tap.get("tap.output.format.alias", outputFormatAlias);
                retentionPeriodDefault = tap.get("tap.retention.period.default", retentionPeriodDefault);
                retentionPeriodHard = tap.get("tap.retention.period.hard", retentionPeriodHard);
                executionDurationDefault = tap.get("tap.execution.duration.default", executionDurationDefault);
                executionDurationHard = tap.get("tap.execution.duration.hard", executionDurationHard);
                outputLimitHard = tap.get("tap.output.limit.hard", outputLimitHard);
                uploadEnabled = tap.get(ConfigKeys.TAP_UPLOAD_ENABLED.getKey(), uploadEnabled);
                uploadLimit = tap.get(ConfigKeys.TAP_UPLOAD_LIMIT.getKey(), uploadLimit);
                hasTapExamples = tapExamplesService.isReady() && tapExamplesService.configurationExists();
                if (scs != null)
                {
                    scsMaxRadius = scs.getFloat("scs.max.radius", scsMaxRadius);
                    scsMaxRecords = scs.getInt("scs.max.records", scsMaxRecords);
                    scsTestRa = scs.getFloat("scs.test.ra", scsTestRa);
                    scsTestDec = scs.getFloat("scs.test.dec", scsTestDec);
                    scsTestCatalog = scs.get("scs.test.catalog", scsTestCatalog);
                    scsTestVerbose = scs.getInt("scs.test.verbose", scsTestVerbose);
                    scsTestExtras = scs.get("scs.test.extras", scsTestExtras);
                    ready = true;
                }
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
        ready = false;
        config = null;
    }

    /**
     * Retrieve the configuration to be exported in the cone search capabilities document.
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Map of the cone search configuration to be exported.
     */
    public Map<String, Object> getScsConfigParams(String capabilitiesUrl)
    {
        Map<String, Object> configParams = new HashMap<>();
        
        String serviceBaseUrl = getServiceBaseUrl(capabilitiesUrl, VoServiceType.scs) + "/";

        configParams.put("capabilitiesURL", serviceBaseUrl + "capabilities");
        configParams.put("availabilityURL", serviceBaseUrl + "availability");

        // add the specific cone search capabilities for each catalog
        List<String[]> catalogues = new ArrayList<>();
        for (Entry<String, ConeSearchTable> entry : scsService.getCatalogueMap().entrySet())
        {
            String[] catData = new String[] {serviceBaseUrl + entry.getKey(), entry
                    .getValue().getTable().getDescription()}; 
            catalogues.add(catData);
        }
        configParams.put("scsCatalogues", catalogues);

        configParams.put("scsTestCatalog", scsTestCatalog);
        configParams.put("scsTestRa", String.valueOf(scsTestRa));
        configParams.put("scsTestDec", String.valueOf(scsTestDec));
        configParams.put("scsTestVerbose", String.valueOf(scsTestVerbose));
        configParams.put("scsTestExtras", scsTestExtras);
        configParams.put("scsTestCatalog", scsTestCatalog);
        
        configParams.put("scsMaxRecords", String.valueOf(scsMaxRecords));
        configParams.put("scsMaxRadius", String.valueOf(scsMaxRadius));
        
        return configParams;
    }

    /**
     * Retrieve the configuration to be exported in the TAP capabilities document.
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Map of the TAP configuration to be exported.
     */
    public Map<String, String> getTapConfigParams(String capabilitiesUrl)
    {
        Map<String, String> configParams = new HashMap<>();
        
        String serviceBaseUrl = getServiceBaseUrl(capabilitiesUrl, VoServiceType.tap);

        configParams.put("capabilitiesURL", serviceBaseUrl + "/capabilities");
        configParams.put("availabilityURL", serviceBaseUrl + "/availability");
        configParams.put("tablesURL", serviceBaseUrl + "/tables");
        
        configParams.put("tapURL", serviceBaseUrl);

        configParams.put("languageName", languageName);
        configParams.put("languageVersion", languageVersion);
        configParams.put("languageDesc", languageDescription);
        configParams.put("outputFormatMime", outputFormatMime);
        configParams.put("outputFormatAlias", outputFormatAlias);
        configParams.put("retentionPeriodDefault", retentionPeriodDefault);
        configParams.put("retentionPeriodHard", retentionPeriodHard);
        configParams.put("execDurationDefault", executionDurationDefault);
        configParams.put("execDurationHard", executionDurationHard);
        configParams.put("outputLimitHard", outputLimitHard);
        configParams.put("uploadEnabled", isUploadEnabled().toString());
        configParams.put("uploadLimit", uploadLimit);
        configParams.put("tapExamplesUrl", tapExamplesService.getExamplesUrl() == null && hasTapExamples 
                ? serviceBaseUrl + "/examples" : tapExamplesService.getExamplesUrl());
        configParams.put("obscoreVersion", tapService.getObsCoreVersion());
        
        return configParams;
    }

    /**
     * Check if TAP upload has been enabled via config.
     * 
     * @return True if tap upload is enabled, otherwise, returns false.
     */
    private Boolean isUploadEnabled()
    {
        return "1".equalsIgnoreCase(uploadEnabled) || "true".equalsIgnoreCase(uploadEnabled)
                || "Y".equalsIgnoreCase(uploadEnabled);
    }

    /**
     * Retrieve the configuration to be exported in the DataLink capabilities document.
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Map of the DataLink configuration to be exported.
     */
    public Map<String, String> getDatalinkConfigParams(String capabilitiesUrl)
    {
        Map<String, String> configParams = new HashMap<>();
        
        String serviceBaseUrl = getServiceBaseUrl(capabilitiesUrl, VoServiceType.datalink) + "/";
        
        configParams.put("capabilitiesURL", serviceBaseUrl + "capabilities");
        configParams.put("availabilityURL", serviceBaseUrl + "availability");
        configParams.put("datalinkURL", serviceBaseUrl + "links");

        return configParams;
    }

    /**
     * Retrieve the configuration to be exported in the SIAP v2 capabilities document.
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Map of the SIAP v2 configuration to be exported.
     */
    public Map<String, String> getSia2ConfigParams(String capabilitiesUrl)
    {
        Map<String, String> configParams = new HashMap<>();
        
        String serviceBaseUrl = getServiceBaseUrl(capabilitiesUrl, VoServiceType.sia2) + "/";

        configParams.put("capabilitiesURL", serviceBaseUrl + "capabilities");
        configParams.put("availabilityURL", serviceBaseUrl + "availability");
        configParams.put("ssapURL", serviceBaseUrl + "query?");

        return configParams;
    }

    /**
     * Retrieve the configuration to be exported in the SSAP capabilities document.
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Map of the SSAP configuration to be exported.
     */
    public Map<String, String> getSsaConfigParams(String capabilitiesUrl)
    {
        Map<String, String> configParams = new HashMap<>();
        
        String serviceBaseUrl = getServiceBaseUrl(capabilitiesUrl, VoServiceType.ssa) + "/";

        configParams.put("capabilitiesURL", serviceBaseUrl + "capabilities");
        configParams.put("availabilityURL", serviceBaseUrl + "availability");
        configParams.put("ssapURL", serviceBaseUrl + "query?");

        configParams.put("outputLimit.hard", ssapOutputLimit);
        configParams.put("max.records", ssapDefaultMaxrec);
        
        return configParams;
    }

    private String getServiceBaseUrl(String capabilitiesUrl, VoServiceType serviceType)
    {
        if (StringUtils.isBlank(capabilitiesUrl))
        {
            capabilitiesUrl = baseUrl;
        }
        if (!capabilitiesUrl.endsWith("/"))
        {
            capabilitiesUrl += "/";
        }
        
        return capabilitiesUrl + serviceType.toString();
    }

}
