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


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.VoToolsApplication;
import au.csiro.casda.votools.TestUtils;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.jaxb.conesearch.ConeSearch;
import au.csiro.casda.votools.jaxb.tapregext.TableAccess;
import au.csiro.casda.votools.jaxb.voresource.Capability;
import au.csiro.casda.votools.jaxb.voresource.Interface;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.scs.ScsService;

/**
 * Unit tests for availability service layer
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CapabilitiesServiceTest.Config.class })
public class CapabilitiesServiceTest
{
    private static String SCS_STD = "ivo://ivoa.net/std/SCS";
    
    @Autowired
    private ConfigurationRegistry configRegistry;

    @Autowired
    private CapabilitiesService capabilityService;

    private static final int EXPECTED_TAP_CAPABILITY_SIZE = 4;
    private static final int EXPECTED_SCS_CAPABILITY_SIZE = 3;

    /**
     * Set up a configuration object
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        Configuration config = ConfigurationTest.getTestConfiguration();
        configRegistry.switchConfiguration(config, false);
    }

    @Test
    public void testGetTapCapabilities() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps = capabilityService.getCapabilities(VoServiceType.tap, null).getCapability();
        assertThat(caps.size(), is(EXPECTED_TAP_CAPABILITY_SIZE));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://localhost:8040/casda_vo_tools//tap/capabilities"));
        assertThat(caps.get(3), is(CoreMatchers.instanceOf(TableAccess.class)));
        TableAccess tapCap = (TableAccess) caps.get(3);
        assertThat(tapCap.getDataModel().size(), is(1));
        assertThat(tapCap.getDataModel().get(0).getIvoId(), is("ivo://ivoa.net/std/ObsCore/v1.0"));
    }

    @Test
    public void testGetTapCapabilitiesWithUrl() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps =
                capabilityService.getCapabilities(VoServiceType.tap, "http://my.proxy.url/vo").getCapability();
        assertThat(caps.size(), is(EXPECTED_TAP_CAPABILITY_SIZE));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://my.proxy.url/vo/tap/capabilities"));
        assertThat(caps.get(3), is(CoreMatchers.instanceOf(TableAccess.class)));
        TableAccess tapCap = (TableAccess) caps.get(3);
        assertThat(tapCap.getDataModel().size(), is(1));
        assertThat(tapCap.getDataModel().get(0).getIvoId(), is("ivo://ivoa.net/std/ObsCore/v1.0"));
    }

    @Test
    public void testGetScsCapabilities() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps = capabilityService.getCapabilities(VoServiceType.scs, null).getCapability();
        assertThat(caps.size(), is(EXPECTED_SCS_CAPABILITY_SIZE));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://localhost:8040/casda_vo_tools//scs/capabilities"));
        // check we have a scs Capability
        boolean scsFound = false;
        for (Capability cap : caps)
        {
            if (cap.getStandardID().equals(SCS_STD))
            {
                scsFound = true;
                assertThat(cap, is(CoreMatchers.instanceOf(ConeSearch.class)));
                ConeSearch coneSearch = (ConeSearch) cap;
                assertThat(coneSearch.getMaxRecords(), is(BigInteger.valueOf(20000)));
                Interface scsInterface = cap.getInterface().get(0);
                assertThat(scsInterface.getAccessURL().get(0).getValue(),
                        containsString("http://localhost:8040/casda_vo_tools//scs/"));
            }
        }
        assertThat(scsFound, is(true));
    }

    @Test
    public void testGetScsCapabilitiesWithUrl() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps =
                capabilityService.getCapabilities(VoServiceType.scs, "http://my.proxy.url/vo").getCapability();
        assertThat(caps.size(), is(EXPECTED_SCS_CAPABILITY_SIZE));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://my.proxy.url/vo/scs/capabilities"));
        // check we have a scs Capability
        boolean scsFound = false;
        for (Capability cap : caps)
        {
            if (cap.getStandardID().equals(SCS_STD))
            {
                scsFound = true;
                assertThat(cap, is(CoreMatchers.instanceOf(ConeSearch.class)));
                ConeSearch coneSearch = (ConeSearch) cap;
                assertThat(coneSearch.getMaxRecords(), is(BigInteger.valueOf(20000)));
                Interface scsInterface = cap.getInterface().get(0);
                assertThat(scsInterface.getAccessURL().get(0).getValue(), containsString("http://my.proxy.url/vo/scs/"));
            }
        }
        assertThat(scsFound, is(true));
    }

    @Test
    public void testGetAccessDataCapabilitiesWithUrl() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps =
                capabilityService.getCapabilities(VoServiceType.data, "http://my.proxy.url/vo").getCapability();
        assertThat(caps.size(), is(3));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://my.proxy.url/vo/data/capabilities"));
        Capability cap = caps.get(2);
        assertThat(cap.getStandardID(), is("ivo://ivoa.net/std/AccessData#sync"));
        Interface scsInterface = cap.getInterface().get(0);
        assertThat(scsInterface.getAccessURL().get(0).getValue(), containsString("http://my.proxy.url/vo/data/"));
    }

    @Test
    public void testGetSia2CapabilitiesWithUrl() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        List<Capability> caps =
                capabilityService.getCapabilities(VoServiceType.sia2, "http://my.proxy.url/vo").getCapability();
        assertThat(caps.size(), is(3));
        assertThat(caps.get(0).getInterface().get(0).getAccessURL().get(0).getValue(),
                is("http://my.proxy.url/vo/sia2/capabilities"));
        Capability cap = caps.get(2);
        assertThat(cap.getStandardID(), is("ivo://ivoa.net/std/SIA#query-2.0"));
        Interface scsInterface = cap.getInterface().get(0);
        assertThat(scsInterface.getAccessURL().get(0).getValue(), containsString("http://my.proxy.url/vo/sia2/"));
    }

    /**
     * Test-specific Configuration class
     */
    @PropertySource("classpath:/application.properties")
    @PropertySource("classpath:/unittest/application.properties")
    @ComponentScan(useDefaultFilters = false, basePackageClasses = { VoToolsApplication.class }, includeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CapabilitiesService.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ConfigurationRegistry.class) })
    public static class Config
    {
        /**
         * Required to configure the PropertySource(s) (see https://jira.spring.io/browse/SPR-8539)
         * 
         * @return a PropertySourcesPlaceholderConfigurer
         */
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
        {
            return new PropertySourcesPlaceholderConfigurer();
        }

        /**
         * Create a ScsService instance driven by mock objects populated with our test catalogue definitions.
         * 
         * @return The ScsService instance.
         * @throws ConfigurationException
         */
        @Bean
        public static ScsService getScsService() throws ConfigurationException
        {
            VoTableRepositoryService voTableRepositoryService = Mockito.mock(VoTableRepositoryService.class);
            TapSchema ivoaSchema = new TapSchema();
            ivoaSchema.setSchemaName("ivoa");
            ivoaSchema.setTables(new ArrayList<>());
            List<TapTable> tableList = new ArrayList<>();
            TapTable obscoreTable =
                    TestUtils.createTapTable("casda", "obs_core", ivoaSchema, ivoaSchema.getSchemaName() + ".ObsCore",
                            true, false);
            tableList.add(obscoreTable);

            List<TapColumn> columnList = new ArrayList<>();
            TapColumn idColumn =
                    TestUtils.createTapColumn(obscoreTable, "obs_id", "VARCHAR", 255, "meta.id;meta.main", 1, 0);
            columnList.add(idColumn);
            TapColumn raColumn =
                    TestUtils.createTapColumn(obscoreTable, "s_ra", "DOUBLE", 15, "pos.eq.ra;meta.main", 1, 1);
            columnList.add(raColumn);
            TapColumn decColumn =
                    TestUtils.createTapColumn(obscoreTable, "s_dec", "DOUBLE", 15, "pos.eq.dec;meta.main", 1, 2);
            columnList.add(decColumn);
            TapColumn instrumentColumn =
                    TestUtils.createTapColumn(obscoreTable, "instrument_name", "VARCHAR", 255, "meta.id;instr", 2, 3);
            columnList.add(instrumentColumn);

            Mockito.when(voTableRepositoryService.getTables()).thenReturn(tableList);
            Mockito.when(voTableRepositoryService.getColumns()).thenReturn(columnList);
            Mockito.when(voTableRepositoryService.isReady()).thenReturn(true);

            Configuration config = ConfigurationTest.getTestConfiguration();
            ScsService scsService =
                    new ScsService(voTableRepositoryService, ConfigurationRegistry.getStaticRegistry());
            scsService.setConfiguration(config);
            scsService.isReady();
            return scsService;
        }

    }

}
