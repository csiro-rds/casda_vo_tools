package au.csiro.casda.votools.capabilities;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.BaseTest;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * Tests the VO TAP Capabilities Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CapabilitiesControllerTest extends BaseTest
{

    @Mock
    private CapabilitiesService mockService;

    @InjectMocks
    private CapabilitiesController capabilitiesController;

    private MockMvc mockMvc;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.mockMvc = MockMvcBuilders.standaloneSetup(capabilitiesController).build();
        doReturn(true).when(mockService).isReady();
    }


    /**
     * Basic test
     * 
     * @throws Exception
     *             Problem performing get request
     */
    @Test
    public void testGetCapabilities() throws Exception
    {
        Map<String, String> configParams = new HashMap<>();
        configParams.put("tapURL", "http://nowhere/tap");
        when(mockService.getTapConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/tap/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("tap/capabilities.xml"))
                .andExpect(model().attribute("tapURL", "http://nowhere/tap"));

        verify(mockService).getTapConfigParams(eq(null));
    }

    @Test
    public void testGetCapabilitiesProxyUrlInHeader() throws Exception
    {
        this.mockMvc.perform(
                get("/tap/capabilities").header(VoKeys.VO_HEADER_CAPABILITIES_URL, "http://my.proxy.url"))
                .andExpect(status().isOk());

        verify(mockService).getTapConfigParams(eq("http://my.proxy.url"));
    }

    /**
     * Test capabilities with Vo type we don't recognise
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithInvalidVoType() throws Exception
    {
        this.mockMvc.perform(get("/xyz/capabilities")).andExpect(status().is4xxClientError());
    }

    /**
     * Test capabilities with cone Search with Vo type
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithConeSearchVoType() throws Exception
    {
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("scsTestCatalog", "casda.continuum_component");
        configParams.put("scsMaxRadius", "10.2");
        when(mockService.getScsConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/scs/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("scs/capabilities.xml"))
                .andExpect(model().attribute("scsTestCatalog", "casda.continuum_component"))
                .andExpect(model().attribute("scsMaxRadius", "10.2"));
    }

    /**
     * Test capabilities with simple image access v1 with VO type
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithSia1VoType() throws Exception
    {
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("siapURL", "http://nowhere/sia1/query?");
        when(mockService.getSia1ConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/sia1/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("sia1/capabilities.xml"))
                .andExpect(model().attribute("siapURL", "http://nowhere/sia1/query?"));
    }

    /**
     * Test capabilities with simple image access v2 with VO type
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCapabilitiesWithSia2VoType() throws Exception
    {
        Map<String, String> configParams = new HashMap<>();
        configParams.put("siapURL", "http://nowhere/sia2/query?");
        when(mockService.getSia2ConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/sia2/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("sia2/capabilities.xml"))
                .andExpect(model().attribute("siapURL", "http://nowhere/sia2/query?"));
    }

    /**
     * Test capabilities for datalink
     */
    @Test
    public void testCapabilitiesWithDatalinkVoType() throws Exception
    {
        Map<String, String> configParams = new HashMap<>();
        configParams.put("datalinkURL", "http://nowhere/datalink/links");
        when(mockService.getDatalinkConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/datalink/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("datalink/capabilities.xml"))
                .andExpect(model().attribute("datalinkURL", "http://nowhere/datalink/links"));
    }

    /**
     * Test capabilities for Simple Spectral Access
     */
    @Test
    public void testCapabilitiesWithSsaVoType() throws Exception
    {
        Map<String, String> configParams = new HashMap<>();
        configParams.put("ssapURL", "http://nowhere/ssa/query?");
        when(mockService.getSsaConfigParams(eq(null))).thenReturn(configParams);
        
        this.mockMvc.perform(get("/ssa/capabilities")).andExpect(status().isOk()).andDo(print())
                .andExpect(forwardedUrl("ssa/capabilities.xml"))
                .andExpect(model().attribute("ssapURL", "http://nowhere/ssa/query?"));
    }

}
