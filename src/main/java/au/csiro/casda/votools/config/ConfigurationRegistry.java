package au.csiro.casda.votools.config;

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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Component;

import au.csiro.casda.votools.VoToolsApplication.ConfigLocation;

/**
 * A registry for configurable objects. When adding new options, use the ConfigKeys enum rather than adding 
 * more entries here.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
@Component
public class ConfigurationRegistry
{
    private static Logger logger = LoggerFactory.getLogger(ConfigurationRegistry.class);

    /** Objects that have registered to be called when configuration changes */
    private Set<Configurable> registry = new HashSet<Configurable>();

    /** On-demand current configuration */
    private Configuration current;

    /** For access from static methods in tests */
    private static ConfigurationRegistry staticRegistry;

    @Value("${tap.jobNamePrefix}")
    private String tapJobNamePrefix;

    @Value("${tap.dataAccessUrl}")
    private String tapDataAccessUrl;

    @Value("${tap.maxRunningJobs}")
    private int tapMaxRunningJobs;

    @Value("${tap.asyncBaseUrl}")
    private String tapAsyncBaseUrl;

    @Value("${tap.asyncDescription}")
    private String tapAsyncDescription;

    @Value("${tap.asyncJobListName}")
    private String tapAsyncJobListName;

    @Value("${tap.language.name}")
    private String tapLanguageName;

    @Value("${tap.language.version}")
    private String tapLanguageVersion;

    @Value("${tap.language.description}")
    private String tapLanguageDescription;

    @Value("${tap.outputFormat.mime}")
    private String tapOutputFormatMime;

    @Value("${tap.outputFormat.alias}")
    private String tapOutputFormatAlias;

    @Value("${tap.retentionPeriod.default}")
    private int tapRetentionPeriodDefault;

    @Value("${tap.retentionPeriod.hard}")
    private int tapRetentionPeriodHard;

    @Value("${tap.executionDuration.default}")
    private int tapExecutionDurationDefault;

    @Value("${tap.sync.timeout}")
    private int tapSyncTimeout;

    @Value("${tap.async.timeout}")
    private int tapAsyncTimeout;

    @Value("${tap.executionDuration.hard}")
    private int tapExecutionDurationHard;

    @Value("${tap.outputLimit.hard}")
    private int tapOutputLimitHard;

    @Value("${log.timezone}")
    private String logTimezone;

    @Value("${tap.max.records}")
    private int tapMaxRecords;

    // SCS properties

    @Value("${scs.outputFormat.mime}")
    private String scsOutputFormatMime;

    @Value("${scs.outputFormat.alias}")
    private String scsOutputFormatAlias;

    @Value("${scs.max.radius}")
    private int scsMaxRadius;

    @Value("${scs.max.records}")
    private int scsMaxRecords;

    @Value("${scs.test.ra}")
    private float scsTestRa;

    @Value("${scs.test.dec}")
    private float scsTestDec;

    @Value("${scs.test.catalog}")
    private String scsTestCatalog;

    @Value("${scs.test.schema}")
    private String scsTestSchema;

    @Value("${scs.test.verbose}")
    private int scsTestVerbose;

    @Value("${scs.test.extras}")
    private String scsTestExtras;

    // Configuration properties

    @Value("${application.base.url}")
    private String applicationBaseUrl;

    @Value("${application.message}")
    private String applicationMessage;

    @Value("${connection.url}")
    private String connectionUrl;

    @Value("${connection.username}")
    private String connectionUsername;

    @Value("${connection.password}")
    private String connectionPassword;

    @Value("${connection.driverClassName}")
    private String connectionDriverClassName;

    @Value("${auth.trusted.ip}")
    private String authTrustedIp;
  
    @Value("${auth.trusted.userId}")
    private String authTrustedUserId;
    
    @Value("${siap.shared.secret.key}")
    private String siapSharedSecretKey;

    @Value("${default.db.schema}")
    private String defaultDbSchema;

    // TAP metadata properties
    @Value("${metadata.instrument}")
    private String metadataInstrument;

    @Value("${metadata.server}")
    private String metadataServer;

    @Value("${metadata.serviceShortName}")
    private String metadataServiceShortName;

    @Value("${metadata.serviceTitle}")
    private String metadataServiceTitle;

    @Value("${metadata.identifier}")
    private String metadataIdentifier;

    @Value("${metadata.servicePublisher}")
    private String metadataServicePublisher;

    @Value("${metadata.furtherInformation}")
    private String metadataFurtherInformation;

    @Value("${metadata.contactPerson}")
    private String metadataContactPerson;

    @Value("${metadata.copyright}")
    private String metadataCopyright;
    
    // Datalink properties
    @Value("${datalink.base.url}")
    private String datalinkBaseUrl;
    
    @Value("${datalink.cutout.url}")
    private String datalinkCutoutUrl;

    @Value("${datalink.cutout.service.name}")
    private String datalinkCutoutServiceName;
    
    @Value("${datalink.generate.spectrum.url}")
    private String datalinkGenerateSpectrumUrl;

    @Value("${datalink.generate.spectrum.service.name}")
    private String datalinkGenerateSpectrumServiceName;
    
    @Value("${datalink.sync.service.name}")
    private String datalinkSyncServiceNameWeb;
    
    @Value("${datalink.sync.service.name.internal:}")
    private String datalinkSyncServiceNameInternal;
    
    @Value("${datalink.sync.service.url}")
    private String datalinkSyncServiceUrl;
    
    @Value("${datalink.sync.service.url.internal}")
    private String datalinkSyncServiceUrlInternal;
    
    @Value("${datalink.async.service.name}")
    private String datalinkAsyncServiceName;
    
    @Value("${datalink.async.service.name.internal:}")
    private String datalinkAsyncServiceNameInternal;
    
    @Value("${datalink.async.service.url}")
    private String datalinkAsyncServiceUrl;
    
    @Value("${datalink.web.service.name}")
    private String datalinkWebServiceName;

    @Value("${datalink.web.service.url}")
    private String datalinkWebServiceUrl;
    
    @Value("${datalink.links.url}")
    private String datalinkLinksUrl;
    
    @Value("${datalink.download.limit.http:}")
    private String datalinkDownloadLimitHttp;
    
    @Value("${datalink.large.web.download.limit.http}")
    private String datalinkLargeWebDownloadLimitHttp;

    @Value("${build.environment}")
    private String environment;
    
    @Value("${stylesheet.information}")
    private String css;
    
    @Value("${logo.url}")
    private String logoUrl;
    
    private Set<String> verifiedDbs;

    @Autowired
    private Environment springEnv;
    
    @Autowired
    private ConfigLocation configLocation;
    
    /**
     * 
     */
    public ConfigurationRegistry()
    {
        verifiedDbs = new HashSet<String>();
    }

    public static ConfigurationRegistry getStaticRegistry()
    {
        return staticRegistry;
    }

    /**
     * Creates and registers a default configuration
     * 
     * @throws ConfigurationException
     *             if there were configuration problems
     */
    @PostConstruct
    public void init() throws ConfigurationException
    {
        ConfigurationRegistry.setStaticRegistry(this);
        File configFileName = this.getConfigurationYamlFile();
        logger.info("YAML configuration file: {}", configFileName.getAbsolutePath());

        try
        {
            Configuration config = Configuration.newConfiguration(configFileName, this);
            if (config.get(ConfigValueKeys.CONNECTION_URL) == null || config.get("connection.username") == null
                    || config.get("connection.password") == null)
            {
                logger.warn("Default configuration file is missing required options and can't be used yet.");
            }
            switchConfiguration(config, false);
        }
        catch (ConfigurationException e)
        {
            logger.info("Could not read configuration file, using a blank one: {}", e.getMessage());
            Configuration config = new Configuration();
            config.addDefaults();
            config.wire();
            current = config;
        }
    }

    /**
     * Register configurable object to keep it informed of configuration changes
     * 
     * @param object
     *            object that wants to be called when configuration changes
     * @return current configuration object
     * @throws ConfigurationException
     *             if could not reconfigure all objects
     */
    public synchronized Configuration register(Configurable object) throws ConfigurationException
    {
        registry.add(object);
        if (object != null && current != null)
        {
            object.setConfiguration(current);
        }
        return current;
    }

    /**
     * Get current configuration.
     * 
     * @return Configuration current configuration object
     */
    public Configuration getCurrent()
    {
        return current;
    }

    /**
     * Make registered configurable objects switch to new configuration
     * 
     * @param newConfiguration
     *            new configuration to switch to
     * @param createTap
     *            create TAP metadata when switching configuration
     * @throws ConfigurationException
     *             if could not reconfigure all objects
     */
    public synchronized void switchConfiguration(Configuration newConfiguration, boolean createTap)
            throws ConfigurationException
    {
        current = newConfiguration;
        if (createTap && current.initDao() != null)
        {
            try
            {
                current.initDao().checkTapDbVersion();
            }
            catch (CannotGetJdbcConnectionException e)
            {
                throw new ConfigurationException("Unable to connect to provided database");
            }
            catch (ConfigurationException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Configuration error", e);
            }
            current.createTap(); // make sure basic TAP metadata exists for listed tables
        }
        for (Configurable object : registry)
        {
            object.invalidate();
        }
        for (Configurable object : registry)
        {
            object.setConfiguration(newConfiguration);
        }
        for (Configurable object : registry)
        { // now all objects have access to new configuration, let's trigger reconfiguring cascade
            object.isReady();
        }
        current.saveConfigurationYaml();
    }

    /**
     * Remove configurable object from the set of registered ones
     * 
     * @param object
     *            object that does not want to be called when configuration is changed
     * 
     */
    public synchronized void deregister(Configurable object)
    {
        registry.remove(object);
    }

    /**
     * Checks if the DB that this configuration uses has been verified to have compliant TAP tables
     * 
     * @param config
     *            the Configuration object
     * @return true if the database has been verified for TAP version compliance, else false
     */
    public boolean isVerifiedTap(Configuration config)
    {
        return verifiedDbs.contains(
                config.get(ConfigValueKeys.CONNECTION_URL) + ":" + config.get(Configuration.DEFAULT_DB_SCHEMA));
    }

    /**
     * Registers the DB that this configuration uses as having compliant TAP tables
     * 
     * @param config
     *            the Configuration object
     */
    public void setVerifiedTap(Configuration config)
    {
        verifiedDbs.add(config.get(ConfigValueKeys.CONNECTION_URL) + ":" + config.get(Configuration.DEFAULT_DB_SCHEMA));
    }

    public String getTapJobNamePrefix()
    {
        return tapJobNamePrefix;
    }

    public void setTapJobNamePrefix(String tapJobNamePrefix)
    {
        this.tapJobNamePrefix = tapJobNamePrefix;
    }

    public String getTapDataAccessUrl()
    {
        return tapDataAccessUrl;
    }

    public void setTapDataAccessUrl(String tapDataAccessUrl)
    {
        this.tapDataAccessUrl = tapDataAccessUrl;
    }

    public int getTapMaxRunningJobs()
    {
        return tapMaxRunningJobs;
    }

    public void setTapMaxRunningJobs(int tapMaxRunningJobs)
    {
        this.tapMaxRunningJobs = tapMaxRunningJobs;
    }

    public String getTapAsyncBaseUrl()
    {
        return tapAsyncBaseUrl;
    }

    public void setTapAsyncBaseUrl(String tapAsyncBaseUrl)
    {
        this.tapAsyncBaseUrl = tapAsyncBaseUrl;
    }

    public String getTapAsyncDescription()
    {
        return tapAsyncDescription;
    }

    public void setTapAsyncDescription(String tapAsyncDescription)
    {
        this.tapAsyncDescription = tapAsyncDescription;
    }

    public String getTapAsyncJobListName()
    {
        return tapAsyncJobListName;
    }

    public void setTapAsyncJobListName(String tapAsyncJobListName)
    {
        this.tapAsyncJobListName = tapAsyncJobListName;
    }

    public String getTapLanguageName()
    {
        return tapLanguageName;
    }

    public void setTapLanguageName(String tapLanguageName)
    {
        this.tapLanguageName = tapLanguageName;
    }

    public String getTapLanguageVersion()
    {
        return tapLanguageVersion;
    }

    public void setTapLanguageVersion(String tapLanguageVersion)
    {
        this.tapLanguageVersion = tapLanguageVersion;
    }

    public String getTapLanguageDescription()
    {
        return tapLanguageDescription;
    }

    public void setTapLanguageDescription(String tapLanguageDescription)
    {
        this.tapLanguageDescription = tapLanguageDescription;
    }

    public String getTapOutputFormatMime()
    {
        return tapOutputFormatMime;
    }

    public void setTapOutputFormatMime(String tapOutputFormatMime)
    {
        this.tapOutputFormatMime = tapOutputFormatMime;
    }

    public String getTapOutputFormatAlias()
    {
        return tapOutputFormatAlias;
    }

    public void setTapOutputFormatAlias(String tapOutputFormatAlias)
    {
        this.tapOutputFormatAlias = tapOutputFormatAlias;
    }

    public int getTapRetentionPeriodDefault()
    {
        return tapRetentionPeriodDefault;
    }

    public void setTapRetentionPeriodDefault(int tapRetentionPeriodDefault)
    {
        this.tapRetentionPeriodDefault = tapRetentionPeriodDefault;
    }

    public int getTapRetentionPeriodHard()
    {
        return tapRetentionPeriodHard;
    }

    public void setTapRetentionPeriodHard(int tapRetentionPeriodHard)
    {
        this.tapRetentionPeriodHard = tapRetentionPeriodHard;
    }

    public int getTapExecutionDurationDefault()
    {
        return tapExecutionDurationDefault;
    }

    public void setTapExecutionDurationDefault(int tapExecutionDurationDefault)
    {
        this.tapExecutionDurationDefault = tapExecutionDurationDefault;
    }

    public int getTapSyncTimeout()
    {
        return tapSyncTimeout;
    }

    public void setTapSyncTimeout(int tapSyncTimeout)
    {
        this.tapSyncTimeout = tapSyncTimeout;
    }

    public int getTapAsyncTimeout()
    {
        return tapAsyncTimeout;
    }

    public void setTapAsyncTimeout(int tapAsyncTimeout)
    {
        this.tapAsyncTimeout = tapAsyncTimeout;
    }

    public int getTapExecutionDurationHard()
    {
        return tapExecutionDurationHard;
    }

    public void setTapExecutionDurationHard(int tapExecutionDurationHard)
    {
        this.tapExecutionDurationHard = tapExecutionDurationHard;
    }

    public int getTapOutputLimitHard()
    {
        return tapOutputLimitHard;
    }

    public void setTapOutputLimitHard(int tapOutputLimitHard)
    {
        this.tapOutputLimitHard = tapOutputLimitHard;
    }

    public String getLogTimezone()
    {
        return logTimezone;
    }

    public void setLogTimezone(String logTimezone)
    {
        this.logTimezone = logTimezone;
    }

    public int getTapMaxRecords()
    {
        return tapMaxRecords;
    }

    public void setTapMaxRecords(int tapMaxRecords)
    {
        this.tapMaxRecords = tapMaxRecords;
    }

    public String getScsOutputFormatMime()
    {
        return scsOutputFormatMime;
    }

    public void setScsOutputFormatMime(String scsOutputFormatMime)
    {
        this.scsOutputFormatMime = scsOutputFormatMime;
    }

    public String getScsOutputFormatAlias()
    {
        return scsOutputFormatAlias;
    }

    public void setScsOutputFormatAlias(String scsOutputFormatAlias)
    {
        this.scsOutputFormatAlias = scsOutputFormatAlias;
    }

    public int getScsMaxRadius()
    {
        return scsMaxRadius;
    }

    public void setScsMaxRadius(int scsMaxRadius)
    {
        this.scsMaxRadius = scsMaxRadius;
    }

    public int getScsMaxRecords()
    {
        return scsMaxRecords;
    }

    public void setScsMaxRecords(int scsMaxRecords)
    {
        this.scsMaxRecords = scsMaxRecords;
    }

    public float getScsTestRa()
    {
        return scsTestRa;
    }

    public void setScsTestRa(float scsTestRa)
    {
        this.scsTestRa = scsTestRa;
    }

    public float getScsTestDec()
    {
        return scsTestDec;
    }

    public void setScsTestDec(float scsTestDec)
    {
        this.scsTestDec = scsTestDec;
    }

    public String getScsTestCatalog()
    {
        return scsTestCatalog;
    }

    public void setScsTestCatalog(String scsTestCatalog)
    {
        this.scsTestCatalog = scsTestCatalog;
    }

    public int getScsTestVerbose()
    {
        return scsTestVerbose;
    }

    public void setScsTestVerbose(int scsTestVerbose)
    {
        this.scsTestVerbose = scsTestVerbose;
    }

    public String getScsTestExtras()
    {
        return scsTestExtras;
    }

    public void setScsTestExtras(String scsTestExtras)
    {
        this.scsTestExtras = scsTestExtras;
    }

    public String getApplicationBaseUrl()
    {
        return applicationBaseUrl;
    }

    public void setApplicationBaseUrl(String applicationBaseUrl)
    {
        this.applicationBaseUrl = applicationBaseUrl;
    }

    public String getApplicationMessage()
    {
        return applicationMessage;
    }

    public void setApplicationMessage(String applicationMessage)
    {
        this.applicationMessage = applicationMessage;
    }

    public String getConnectionUrl()
    {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl)
    {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUsername()
    {
        return connectionUsername;
    }

    public void setConnectionUsername(String connectionUsername)
    {
        this.connectionUsername = connectionUsername;
    }

    public String getConnectionPassword()
    {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword)
    {
        this.connectionPassword = connectionPassword;
    }

    public String getConnectionDriverClassName()
    {
        return connectionDriverClassName;
    }

    /**
     * Gets the location of the configuration yaml file
     * 
     * @return the configured yaml file or the default location
     */
    public File getConfigurationYamlFile()
    {
        String configFileName = System.getProperty("config.file");
        if (configFileName == null)
        {
            configFileName = Configuration.DEFAULT_CONFIG;
        }

        File configFile = configLocation.getConfigFile(configFileName, true);

        return configFile;
    }

    /**
     * Get TAP examples config file location.
     * 
     * @return Tap Examples config location.
     */
    public File getConfigurationTapExamplesFile()
    {
        String configFileName = System.getProperty("tap.examples.config.file");
        if (configFileName == null)
        {
            configFileName = Configuration.DEFAULT_TAP_EXAMPLES_CONFIG;
        }
        return new File(configFileName);
    }
    
    public static void setStaticRegistry(ConfigurationRegistry staticRegistry)
    {
        ConfigurationRegistry.staticRegistry = staticRegistry;
    }

    public String getAuthTrustedIp()
    {
        return authTrustedIp;
    }

    public void setAuthTrustedIp(String authTrustedIp)
    {
        this.authTrustedIp = authTrustedIp;
    }
    
    public String getAuthTrustedUserId()
    {
        return authTrustedUserId;
    }

    public void setAuthTrustedUserId(String authTrustedUserId)
    {
        this.authTrustedUserId = authTrustedUserId;
    }

    public String getMetadataInstrument()
    {
        return metadataInstrument;
    }

    public void setMetadataInstrument(String metadataInstrument)
    {
        this.metadataInstrument = metadataInstrument;
    }

    public String getMetadataServer()
    {
        return metadataServer;
    }

    public void setMetadataServer(String metadataServer)
    {
        this.metadataServer = metadataServer;
    }

    public String getMetadataServiceShortName()
    {
        return metadataServiceShortName;
    }

    public void setMetadataServiceShortName(String metadataServiceShortName)
    {
        this.metadataServiceShortName = metadataServiceShortName;
    }

    public String getMetadataServiceTitle()
    {
        return metadataServiceTitle;
    }

    public void setMetadataServiceTitle(String metadataServiceTitle)
    {
        this.metadataServiceTitle = metadataServiceTitle;
    }

    public String getMetadataIdentifier()
    {
        return metadataIdentifier;
    }

    public void setMetadataIdentifier(String metadataIdentifier)
    {
        this.metadataIdentifier = metadataIdentifier;
    }

    public String getMetadataServicePublisher()
    {
        return metadataServicePublisher;
    }

    public void setMetadataServicePublisher(String metadataServicePublisher)
    {
        this.metadataServicePublisher = metadataServicePublisher;
    }

    public String getMetadataFurtherInformation()
    {
        return metadataFurtherInformation;
    }

    public void setMetadataFurtherInformation(String metadataFurtherInformation)
    {
        this.metadataFurtherInformation = metadataFurtherInformation;
    }

    public String getMetadataContactPerson()
    {
        return metadataContactPerson;
    }

    public void setMetadataContactPerson(String metadataContactPerson)
    {
        this.metadataContactPerson = metadataContactPerson;
    }

    public String getMetadataCopyright()
    {
        return metadataCopyright;
    }

    public void setMetadataCopyright(String metadataCopyright)
    {
        this.metadataCopyright = metadataCopyright;
    }

    public String getScsTestSchema()
    {
        return scsTestSchema;
    }

    public void setScsTestSchema(String scsTestSchema)
    {
        this.scsTestSchema = scsTestSchema;
    }

    public String getDefaultDbSchema()
    {
        return defaultDbSchema;
    }

    public void setDefaultDbSchema(String defaultDbSchema)
    {
        this.defaultDbSchema = defaultDbSchema;
    }

    public String getDatalinkBaseUrl()
    {
        return datalinkBaseUrl;
    }

    public void setDatalinkBaseUrl(String datalinkBaseUrl)
    {
        this.datalinkBaseUrl = datalinkBaseUrl;
    }
    
    public String getDatalinkCutoutUrl()
    {
        return datalinkCutoutUrl;
    }

    public void setDatalinkCutoutUrl(String datalinkCutoutUrl)
    {
        this.datalinkCutoutUrl = datalinkCutoutUrl;
    }

    public String getDatalinkGenerateSpectrumUrl() 
    {
		return datalinkGenerateSpectrumUrl;
	}

	public void setDatalinkGenerateSpectrumUrl(String datalinkGenerateSpectrumUrl)
	{
		this.datalinkGenerateSpectrumUrl = datalinkGenerateSpectrumUrl;
	}

	public String getDatalinkGenerateSpectrumServiceName() 
	{
		return datalinkGenerateSpectrumServiceName;
	}

	public void setDatalinkGenerateSpectrumServiceName(String datalinkGenerateSpectrumServiceName)
	{
		this.datalinkGenerateSpectrumServiceName = datalinkGenerateSpectrumServiceName;
	}

	public String getDatalinkCutoutServiceName()
    {
        return datalinkCutoutServiceName;
    }

    public void setDatalinkCutoutServiceName(String datalinkCutoutServiceName)
    {
        this.datalinkCutoutServiceName = datalinkCutoutServiceName;
    }

    public String getDatalinkSyncServiceNameWeb()
    {
        return datalinkSyncServiceNameWeb;
    }

    public void setDatalinkSyncServiceNameWeb(String datalinkSyncServiceNameWeb)
    {
        this.datalinkSyncServiceNameWeb = datalinkSyncServiceNameWeb;
    }

    public String getDatalinkSyncServiceNameInternal()
    {
        return datalinkSyncServiceNameInternal;
    }

    public void setDatalinkSyncServiceNameInternal(String datalinkSyncServiceNameInternal)
    {
        this.datalinkSyncServiceNameInternal = datalinkSyncServiceNameInternal;
    }

    public String getDatalinkSyncServiceUrl()
    {
        return datalinkSyncServiceUrl;
    }

    public void setDatalinkSyncServiceUrl(String datalinkSyncServiceUrl)
    {
        this.datalinkSyncServiceUrl = datalinkSyncServiceUrl;
    }
    
    public void setDatalinkSyncServiceUrlInternal(String datalinkSyncServiceUrlInternal)
    {
        this.datalinkSyncServiceUrlInternal = datalinkSyncServiceUrlInternal;
    }
    
    public String getDatalinkSyncServiceUrlInternal()
    {
        return datalinkSyncServiceUrlInternal;
    }
    
    public String getDatalinkAsyncServiceName()
    {
        return datalinkAsyncServiceName;
    }

    public void setDatalinkAsyncServiceName(String datalinkAsyncServiceName)
    {
        this.datalinkAsyncServiceName = datalinkAsyncServiceName;
    }

    public String getDatalinkAsyncServiceNameInternal()
    {
        return datalinkAsyncServiceNameInternal;
    }

    public void setDatalinkAsyncServiceNameInternal(String datalinkAsyncServiceNameInternal)
    {
        this.datalinkAsyncServiceNameInternal = datalinkAsyncServiceNameInternal;
    }

    public String getDatalinkAsyncServiceUrl()
    {
        return datalinkAsyncServiceUrl;
    }

    public void setDatalinkAsyncServiceUrl(String datalinkAsyncServiceUrl)
    {
        this.datalinkAsyncServiceUrl = datalinkAsyncServiceUrl;
    }
    
    public String getDatalinkWebUrl()
    {
        return datalinkWebServiceUrl;
    }

    public void setDatalinkWebUrl(String datalinkWebUrl)
    {
        this.datalinkWebServiceUrl = datalinkWebUrl;
    }

    public String getDatalinkLinksUrl()
    {
        return datalinkLinksUrl;
    }

    public void setDatalinkLinksUrl(String datalinkLinksUrl)
    {
        this.datalinkLinksUrl = datalinkLinksUrl;
    }

    public String getDatalinkWebServiceName()
    {
        return datalinkWebServiceName;
    }

    public void setDatalinkWebServiceName(String datalinkWebServiceName)
    {
        this.datalinkWebServiceName = datalinkWebServiceName;
    }

    public String getSiapSharedSecretKey()
    {
        return siapSharedSecretKey;
    }

    public void setSiapSharedSecretKey(String siapSharedSecretKey)
    {
        this.siapSharedSecretKey = siapSharedSecretKey;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public String getDatalinkDownloadLimitHttp()
    {
        return datalinkDownloadLimitHttp;
    }

    public void setDatalinkDownloadLimitHttp(String datalinkDownloadLimitHttp)
    {
        this.datalinkDownloadLimitHttp = datalinkDownloadLimitHttp;
    }

    public String getDatalinkLargeWebDownloadLimitHttp()
    {
        return datalinkLargeWebDownloadLimitHttp;
    }

    public void setDatalinkLargeWebDownloadLimitHttp(String datalinkLargeWebDownloadLimitHttp)
    {
        this.datalinkLargeWebDownloadLimitHttp = datalinkLargeWebDownloadLimitHttp;
    }
    
    public String getCss()
    {
        return css;
    }

    public void setCss(String css)
    {
        this.css = css;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = logoUrl;
    }

    /**
     * Retrieve the application property value for the provided key. This does a dynamic lookup of the Spring
     * environment.
     * 
     * @param key The key of the config itemto be retrieved.
     * @return The value, or null if the key is not present.
     */
    public String getConfigValue(String key)
    {
        String value = null;
        if (springEnv != null)
        {
            value = springEnv.getProperty(key);
        }
        return value;
    }
    
}
