package au.csiro.casda.votools.capabilities;

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


import java.math.BigInteger;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jaxb.capabilities.Capabilities;
import au.csiro.casda.votools.jaxb.conesearch.ConeSearch;
import au.csiro.casda.votools.jaxb.conesearch.Query;
import au.csiro.casda.votools.jaxb.tapregext.DataLimit;
import au.csiro.casda.votools.jaxb.tapregext.DataLimits;
import au.csiro.casda.votools.jaxb.tapregext.DataModelType;
import au.csiro.casda.votools.jaxb.tapregext.Language;
import au.csiro.casda.votools.jaxb.tapregext.OutputFormat;
import au.csiro.casda.votools.jaxb.tapregext.TableAccess;
import au.csiro.casda.votools.jaxb.tapregext.TimeLimits;
import au.csiro.casda.votools.jaxb.tapregext.Version;
import au.csiro.casda.votools.jaxb.vodataservice.HTTPQueryType;
import au.csiro.casda.votools.jaxb.vodataservice.InputParam;
import au.csiro.casda.votools.jaxb.vodataservice.ParamHTTP;
import au.csiro.casda.votools.jaxb.vodataservice.ParamUse;
import au.csiro.casda.votools.jaxb.vodataservice.SimpleDataType;
import au.csiro.casda.votools.jaxb.voresource.AccessURL;
import au.csiro.casda.votools.jaxb.voresource.Capability;
import au.csiro.casda.votools.scs.ConeSearchTable;
import au.csiro.casda.votools.scs.ScsService;

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
public class CapabilitiesService extends Configurable
{

    private static final String VOSI_CAPABILITIES_STD = "ivo://ivoa.net/std/VOSI#capabilities";
    private static final String VOSI_AVAILABILITY_STD = "ivo://ivoa.net/std/VOSI#availability";
    private static final String VOSI_TABLE_STD = "ivo://ivoa.net/std/VOSI#tables";
    private static final String TAP_STD = "ivo://ivoa.net/std/TAP";
    private static final String OBSCORE_STD = "ivo://ivoa.net/std/ObsCore/v1.0";
    private static final String ADQL_STD = "ivo://ivoa.net/std/ADQL#v2.0";
    private static final String OBSCORE_NAME = "ObsCore 1.0";
    private static final String SCS_STD = "ivo://ivoa.net/std/SCS";
    private static final String DATALINK_STD = "ivo://ivoa.net/std/DataLink#links-1.0";
    private static final String SIAP2_STD = "ivo://ivoa.net/std/SIA#query-2.0";
    private static final String SODA_STD = "ivo://ivoa.net/std/SODA#sync-1.0";
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

    private float scsMaxRadius;

    private int scsMaxRecords;

    private float scsTestRa;

    private float scsTestDec;

    private String scsTestCatalog;

    private int scsTestVerbose;

    private String scsTestExtras;

    @Autowired
    private ScsService scsService;

    private boolean ready;

    private Configuration config;

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

    /**
     * @param voServiceType
     *            VO Service type required eg TAP, SCS
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return Capabilities to return information to VO clients about this service.
     */
    public Capabilities getCapabilities(VoServiceType voServiceType, String capabilitiesUrl)
    {
        if (StringUtils.isBlank(capabilitiesUrl))
        {
            capabilitiesUrl = baseUrl;
        }
        logger.info("Hit the getCapabilities for the {} service, at url {}", voServiceType, capabilitiesUrl);
        Capabilities caps = new Capabilities();
        caps.getCapability().add(
                this.buildStdInterfaceCapability(VOSI_CAPABILITIES_STD,
                        String.format("%s/%s/capabilities", capabilitiesUrl, voServiceType), "full"));
        caps.getCapability().add(
                this.buildStdInterfaceCapability(VOSI_AVAILABILITY_STD,
                        String.format("%s/%s/availability", capabilitiesUrl, voServiceType), "full"));
        switch (voServiceType)
        {
        case tap:
            caps.getCapability().add(
                    this.buildStdInterfaceCapability(VOSI_TABLE_STD, capabilitiesUrl + "/tap/tables", "full"));
            caps.getCapability().add(this.buildTapInterfaceCapability(capabilitiesUrl + "/tap"));
            break;

        case scs:
            // add the specific cone search capabilities for each catalog
            for (Entry<String, ConeSearchTable> entry : scsService.getCatalogueMap().entrySet())
            {
                caps.getCapability().add(
                        this.buildScsInterfaceCapability(
                                String.format("%s/%s/%s", capabilitiesUrl, voServiceType, entry.getKey()), entry
                                        .getValue().getTable().getDescription()));
            }
            break;

        case sia2:
            caps.getCapability().add(this.buildSiap2InterfaceCapability(capabilitiesUrl + "/sia2/query"));
            break;
        case datalink:
            caps.getCapability().add(buildDataLinkInterfaceCapability(capabilitiesUrl + "/datalink/links"));
            break;
        case data:
            caps.getCapability().add(buildAccessDataInterfaceCapability(capabilitiesUrl + "/data/sync"));
            break;
        default:
            // Don't provide any specific services
            break;
        }

        return caps;
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
     * Generate a capability description for a standard VOSI service.
     * 
     * @param std
     *            the IVOA-standard function
     * @param accessUrl
     *            To access the service
     * @param use
     *            whether this url is the full url or a base url
     * @return Capability object representing the IVOA standard function
     */
    private Capability buildStdInterfaceCapability(String std, String accessUrl, String use)
    {
        Capability cap = new Capability();
        cap.setStandardID(std);
        cap.getInterface().add(this.getParamHTTPInterface(accessUrl, use));
        return cap;
    }
    
    /**
     * Generate a TAP capability using the TableAccess TAP extension class. Attributes are populated from application
     * properties.
     * 
     * @param accessUrl
     *            base url for the TAP endpoint
     * @return capability object representing the TAP standard endpoint
     */
    private Capability buildTapInterfaceCapability(String accessUrl)
    {
        TableAccess ta = new TableAccess();
        ta.setStandardID(TAP_STD);
        ta.getInterface().add(this.getParamHTTPInterface(accessUrl, "base"));
        ta.getLanguage().add(this.getLanguage(languageName, languageVersion, languageDescription));
        ta.setOutputLimit(this.getHardLimits(outputLimitHard));
        ta.getOutputFormat().add(this.getOutputFormat(outputFormatMime, outputFormatAlias));
        ta.setRetentionPeriod(this.getTimeLimits(retentionPeriodDefault, retentionPeriodHard));
        ta.setExecutionDuration(this.getTimeLimits(executionDurationDefault, executionDurationHard));
        ta.getDataModel().add(this.getDataModel(OBSCORE_STD, OBSCORE_NAME));
        return ta;
    }

    private Language getLanguage(String name, String version, String description)
    {
        Language lan = new Language();
        lan.setName(name);
        Version ver = new Version();
        ver.setValue(version);
        ver.setIvoId(ADQL_STD);
        lan.getVersion().add(ver);
        lan.setDescription(description);
        return lan;
    }

    private ParamHTTP getParamHTTPInterface(String accessUrl, String use)
    {
        ParamHTTP param = new ParamHTTP();
        param.getAccessURL().add(this.getAccessurl(accessUrl, use));
        param.setRole("std");
        HTTPQueryType qt = HTTPQueryType.GET;

        param.getQueryType().add(qt);
        param.setResultType("application/xml");
        return param;
    }

    private AccessURL getAccessurl(String value, String use)
    {
        AccessURL au = new AccessURL();
        au.setValue(value);
        au.setUse(use);
        return au;
    }

    private DataLimits getHardLimits(String outputLimitValue)
    {
        DataLimit outputLimit = new DataLimit();
        outputLimit.setValue(new BigInteger(outputLimitValue));
        outputLimit.setUnit("row");
        DataLimits limits = new DataLimits();
        limits.setHard(outputLimit);
        return limits;
    }

    private OutputFormat getOutputFormat(String mime, String alias)
    {
        OutputFormat format = new OutputFormat();
        format.setMime(mime);
        format.getAlias().add(alias);
        return format;
    }

    private TimeLimits getTimeLimits(String defaultValue, String hardValue)
    {
        TimeLimits tl = new TimeLimits();
        tl.setDefault(new BigInteger(defaultValue));
        tl.setHard(new BigInteger(hardValue));
        return tl;
    }

    private DataModelType getDataModel(String ivorn, String name)
    {
        DataModelType dataModel = new DataModelType();
        dataModel.setIvoId(ivorn);
        dataModel.setValue(name);
        return dataModel;
    }

    /**
     * Generate an SCS capability using the ConeSearch extension class. Attributes are populated from aplication
     * properties.
     * 
     * @param accessUrl
     *            base url for the SCS endpoint
     * @param description
     *            catalog description for the SCS endpoint
     * @return capability object representing the SCS standard endpoint
     */
    private Capability buildScsInterfaceCapability(String accessUrl, String description)
    {
        ConeSearch cs = new ConeSearch();
        cs.setStandardID(SCS_STD);
        cs.setDescription(description);
        cs.getInterface().add(this.getParamHTTPInterface(accessUrl, "base"));
        cs.setMaxRecords(BigInteger.valueOf(scsMaxRecords));
        cs.setMaxSR(Float.valueOf(scsMaxRadius));
        cs.setTestQuery(this.getTestQuery());
        return cs;
    }

    /**
     * Generate a capability description for a SIA v2 query service.
     * 
     * @param accessUrl
     *            To access the service
     * @return Capability object representing the SIAv2 standard endpoint
     */
    private Capability buildSiap2InterfaceCapability(String accessUrl)
    {
        Capability cap = new Capability();
        cap.setStandardID(SIAP2_STD);
        ParamHTTP paramHTTPInterface = this.getParamHTTPInterface(accessUrl, null);
        paramHTTPInterface.setVersion("2.0");
        cap.getInterface().add(paramHTTPInterface);
        return cap;
    }
    
    /**
     * Generate a Data Link capability description for the data link service.
     * 
     * @param accessUrl
     *            base url for the TAP endpoint
     * @return capability object representing the TAP standard endpoint
     */
    private Capability buildDataLinkInterfaceCapability(String accessUrl)
    {
        Capability cap = new Capability();
        cap.setStandardID(DATALINK_STD);
        ParamHTTP paramHTTPInterface = this.getParamHTTPInterface(accessUrl, null);
        paramHTTPInterface.setVersion("1.0");
        paramHTTPInterface.setResultType("application/x-votable+xml;content=datalink");
        paramHTTPInterface.getQueryType().add(HTTPQueryType.POST);       
        paramHTTPInterface.getParam().add(createHttpInterfaceInputParam());
        cap.getInterface().add(paramHTTPInterface);
        return cap;
    }
    
    private InputParam createHttpInterfaceInputParam()
    {
        InputParam param = new InputParam();
        param.setStd(true);
        param.setUse(ParamUse.REQUIRED);
        param.setName("ID");
        param.setDescription("publisher dataset identifier");
        param.setUcd("meta.id;meta.main");
        SimpleDataType type = new SimpleDataType();
        type.setValue("string");
        param.setDataType(type);
        return param;
    }

    private Capability buildAccessDataInterfaceCapability(String accessUrl)
    {
        Capability cap = new Capability();
        cap.setStandardID(SODA_STD);
        ParamHTTP paramHTTPInterface = this.getParamHTTPInterface(accessUrl, "full");
        paramHTTPInterface.setVersion("1.0");
        cap.getInterface().add(paramHTTPInterface);
        return cap;
    }

    /**
     * @return the testQuery
     */
    public Query getTestQuery()
    {
        Query testQuery = new Query();
        testQuery.setCatalog(scsTestCatalog);
        testQuery.setRa(scsTestRa);
        testQuery.setDec(scsTestDec);
        testQuery.setVerb(BigInteger.valueOf(scsTestVerbose));
        testQuery.setExtras(scsTestExtras);
        testQuery.setCatalog(scsTestCatalog);
        return testQuery;
    }

}
