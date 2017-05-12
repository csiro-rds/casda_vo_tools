package au.csiro.casda.votools.capabilities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import au.csiro.casda.votools.TestUtils;
import au.csiro.casda.votools.VoToolsApplication;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.scs.ScsService;

/**
 * Unit tests for capability service layer
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CapabilitiesServiceTest.Config.class })
public class CapabilitiesServiceTest
{
    
    @Autowired
    private ConfigurationRegistry configRegistry;

    @Autowired
    private CapabilitiesService capabilityService;

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
    public void testGetScsConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();

        Map<String, Object> scsConfigParams = capabilityService.getScsConfigParams("");
        @SuppressWarnings("unchecked")
        List<String[]> scsCatalogues = (List<String[]>) scsConfigParams.get("scsCatalogues");
        assertThat(scsCatalogues, is(not(empty())));
        for (String[] entry : scsCatalogues)
        {
            assertThat(entry.length, is(2));
        }
    }

    @Test
    public void testGetSsaConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> ssaConfigParams = capabilityService.getSsaConfigParams("");
        assertThat(ssaConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/ssa/capabilities"));
        assertThat(ssaConfigParams, hasEntry("outputLimit.hard", "20000000"));
    }

    @Test
    public void testGetSia2ConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> ssaConfigParams = capabilityService.getSia2ConfigParams("");
        assertThat(ssaConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/sia2/capabilities"));
    }

    @Test
    public void testGetDatalinkConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> datalinkConfigParams = capabilityService.getDatalinkConfigParams("");
        assertThat(datalinkConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/datalink/capabilities"));
        assertThat(datalinkConfigParams,
                hasEntry("datalinkURL", "http://localhost:8040/casda_vo_tools/datalink/links"));
    }

    @Test
    public void testGetTapConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> tapConfigParams = capabilityService.getTapConfigParams("");
        assertThat(tapConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/tap/capabilities"));
        assertThat(tapConfigParams, hasEntry("outputLimitHard", "20000000"));
    }

    @Test
    public void testGetTapConfigParamsWithUrl() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> tapConfigParams = capabilityService.getTapConfigParams("https://some.proxy/vo");
        assertThat(tapConfigParams,
                hasEntry("capabilitiesURL", "https://some.proxy/vo/tap/capabilities"));
        assertThat(tapConfigParams, hasEntry("outputLimitHard", "20000000"));
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
