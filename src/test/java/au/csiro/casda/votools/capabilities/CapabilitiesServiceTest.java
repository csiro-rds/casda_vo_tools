package au.csiro.casda.votools.capabilities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import au.csiro.casda.votools.VoToolsApplication.ConfigLocation;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.ConfigurationTest;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapSchema;
import au.csiro.casda.votools.jpa.TapTable;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.scs.ScsService;
import au.csiro.casda.votools.siap1.Siap1Service;
import au.csiro.casda.votools.surveys.SiapSurveysService;
import au.csiro.casda.votools.tap.TapService;

/**
 * Unit tests for capability service layer. Note the configuration file unittest/application.properties is used.
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
        populateSia1Surveys(config);
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
    public void testGetSia1ConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, Object> siaConfigParams = capabilityService.getSia1ConfigParams("");
        assertThat(siaConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/sia1/capabilities"));
        @SuppressWarnings("unchecked")
        List<String[]> siaSurveys = (List<String[]>) siaConfigParams.get("siaSurveys");
        assertThat(siaSurveys, is(not(empty())));
        for (String[] entry : siaSurveys)
        {
            assertThat(entry.length, is(2));
        }
    }

    @Test
    public void testGetSia2ConfigParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> siaConfigParams = capabilityService.getSia2ConfigParams("");
        assertThat(siaConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/sia2/capabilities"));
        assertThat(siaConfigParams,
                hasEntry("siapURL", "http://localhost:8040/casda_vo_tools/sia2/query?"));
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
    public void testGetTapConfigHasUploadParams() throws ConfigurationException
    {
        configRegistry.register(capabilityService);
        capabilityService.isReady();
        
        Map<String, String> tapConfigParams = capabilityService.getTapConfigParams("");
        assertThat(tapConfigParams,
                hasEntry("capabilitiesURL", "http://localhost:8040/casda_vo_tools/tap/capabilities"));
        assertThat(tapConfigParams, hasEntry("outputLimitHard", "20000000"));
        assertThat(tapConfigParams, hasEntry("uploadEnabled", "false"));
        assertThat(tapConfigParams, hasEntry("uploadLimit", "100000"));
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

    private void populateSia1Surveys(Configuration config)
    {
        EndPoint endPoint = config.getEndPoint("SIA1");
        
        List<Map<String, String>> surveyDef = new ArrayList<>();
        Map<String, String> testSurvey = new HashMap<>();
        testSurvey.put("code", "RACS-Low");
        testSurvey.put("name", "RACS Low");
        testSurvey.put("group", "RACS");
        testSurvey.put("whereClause", "(a>5)");
        testSurvey.put("description", "RACS DR1 simple image access");
        surveyDef.add(testSurvey);
        endPoint.setSurveys(surveyDef);
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
        private static VoTableRepositoryService voTableRepositoryService;

        private ConfigLocation configLocation;

        /**
         * Constructor
         * @throws ConfigurationException 
         */
        public Config() throws ConfigurationException
        {
            voTableRepositoryService = Mockito.mock(VoTableRepositoryService.class);
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
            configLocation = new ConfigLocation(new HashSet<>(Arrays.asList(new String[] {"config"})));
        }
        
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
            Configuration config = ConfigurationTest.getTestConfiguration();
            ScsService scsService =
                    new ScsService(voTableRepositoryService, ConfigurationRegistry.getStaticRegistry());
            scsService.setConfiguration(config);
            scsService.isReady();
            return scsService;
        }

        /**
         * Create a Siap1Service instance driven by mock objects populated with our test catalogue definitions.
         * 
         * @return The Siap1Service instance.
         * @throws ConfigurationException
         */
        @Bean
        @Autowired
        public static Siap1Service getSiap1Service(TapService tapService, SiapSurveysService siapSurveysService)
                throws ConfigurationException
        {
            Configuration config = ConfigurationTest.getTestConfiguration();
            Siap1Service sia1Service =
                    new Siap1Service(ConfigurationRegistry.getStaticRegistry(), tapService, siapSurveysService);
            sia1Service.setConfiguration(config);
            sia1Service.isReady();
            return sia1Service;
        }

        /**
         * Create a Siap1Service instance driven by mock objects populated with our test catalogue definitions.
         * 
         * @return The Siap1Service instance.
         * @throws ConfigurationException
         */
        @Bean
        @Autowired
        public static SiapSurveysService getSiapSurveysService() throws ConfigurationException
        {
            Configuration config = ConfigurationTest.getTestConfiguration();
            //populateSurveys(config);
            SiapSurveysService siapSurveysService =
                    new SiapSurveysService(ConfigurationRegistry.getStaticRegistry());
            siapSurveysService.setConfiguration(config);
            siapSurveysService.isReady();
            return siapSurveysService;
        }
        
        /**
         * Create a TapService instance driven by mock objects populated with our test catalogue definitions.
         * 
         * @return The TapService instance.
         * @throws ConfigurationException
         */
        @Bean
        public static TapService getTapService() throws ConfigurationException
        {
            Configuration config = ConfigurationTest.getTestConfiguration();
            TapService tapService = new TapService(ConfigurationRegistry.getStaticRegistry(), voTableRepositoryService);
            tapService.setConfiguration(config);
            tapService.isReady();
            return tapService;
        }
        
        /**
         * @return A bean to hold the configuration locations.
         */
        @Bean
        public ConfigLocation getConfigLocation()
        {
            return configLocation;
        }

    }

}
