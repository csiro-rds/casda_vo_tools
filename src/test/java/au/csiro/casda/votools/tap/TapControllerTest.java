package au.csiro.casda.votools.tap;

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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Document;

import au.csiro.casda.votools.result.OutputFormat;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * Tests the VO TAP main endpoint Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class TapControllerTest
{
    @Mock
    private TapService mockService;

    @Spy
    private DummyUWService uwService;

    @InjectMocks
    private TapController tapController;

    private MockMvc mockMvc;

    private final static long SECONDS_TO_WAIT_FOR_JOB = 5;

    /**
     * Set up the controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        doReturn(true).when(mockService).isReady();
        this.mockMvc = MockMvcBuilders.standaloneSetup(tapController).build();
    }

    /**
     * Basic test of the sync endpoint. Checks the content type returned for each format.
     * 
     * @throws Exception
     *             from performing get request
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSyncViaEndpoint() throws Exception
    {
        String adql = "select * from obs_core";
        String queryString = "request=doQuery&LANG=ADQL&query=" + adql + "&format=";

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

        when(mockService.getFormat("VOTABLE")).thenReturn(OutputFormat.VOTABLE);
        when(mockService.getFormat("CSV")).thenReturn(OutputFormat.CSV);
        when(mockService.getFormat("TSV")).thenReturn(OutputFormat.TSV);
        when(mockService.getFormat(null)).thenReturn(OutputFormat.VOTABLE);
        when(mockService.processQuery((Writer) anyObject(), mapCaptor.capture())).thenReturn(true);

        this.mockMvc.perform(get("/tap/sync?" + queryString + "VOTABLE")).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(0).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(0).get(VoKeys.USER_PROJECTS));

        this.mockMvc.perform(get("/tap/sync?" + queryString + "CSV")).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/csv;header=present"))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".csv\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(1).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(1).get(VoKeys.USER_PROJECTS));

        this.mockMvc.perform(get("/tap/sync?" + queryString + "TSV")).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/tab-separated-values"))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".tsv\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(2).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(2).get(VoKeys.USER_PROJECTS));

        // check content type set to a default
        this.mockMvc.perform(get("/tap/sync?" + "request=doQuery&LANG=ADQL&query=" + adql)).andExpect(status().isOk())
                .andDo(print()).andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(3).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(3).get(VoKeys.USER_PROJECTS));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSyncViaEndpointAddsUserInfoIfProxiedRequest() throws Exception
    {
        String adql = "select * from obs_core";
        String queryString = "request=doQuery&LANG=ADQL&query=" + adql + "&format=";
        String userId = "smi123";
        String loginSystem = "OPAL";
        String userProjects = "all";

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

        when(mockService.getFormat("VOTABLE")).thenReturn(OutputFormat.VOTABLE);
        when(mockService.getFormat("CSV")).thenReturn(OutputFormat.CSV);
        when(mockService.getFormat("TSV")).thenReturn(OutputFormat.TSV);
        when(mockService.getFormat(null)).thenReturn(OutputFormat.VOTABLE);
        when(mockService.processQuery((Writer) anyObject(), mapCaptor.capture())).thenReturn(true);
        when(mockService.trustAuthHeader(any(HttpServletRequest.class))).thenReturn(true);

        this.mockMvc
                .perform(get("/tap/sync?" + queryString + "VOTABLE").header(VoKeys.VO_AUTH_HEADER_USER_ID, userId)
                        .header(VoKeys.VO_AUTH_HEADER_LOGIN_SYSTEM, loginSystem)
                        .header(VoKeys.VO_AUTH_HEADER_USER_PROJECTS, userProjects))
                .andExpect(status().isOk()).andDo(print()).andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(userId, mapCaptor.getAllValues().get(0).get(VoKeys.USER_ID));
        assertEquals(loginSystem, mapCaptor.getAllValues().get(0).get(VoKeys.LOGIN_SYSTEM));
        assertEquals(userProjects, mapCaptor.getAllValues().get(0).get(VoKeys.USER_PROJECTS));

        this.mockMvc.perform(get("/tap/sync?" + queryString + "VOTABLE")).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(1).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(1).get(VoKeys.LOGIN_SYSTEM));
        assertEquals("", mapCaptor.getAllValues().get(1).get(VoKeys.USER_PROJECTS));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSyncViaEndpointIgnoresUserInfoIfNotproxiedRequest() throws Exception
    {
        String adql = "select * from obs_core";
        String queryString = "request=doQuery&LANG=ADQL&query=" + adql + "&format=";
        String userId = "smi123";
        String loginSystem = "NEXUS";
        String userProjects = "all";

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

        when(mockService.getFormat("VOTABLE")).thenReturn(OutputFormat.VOTABLE);
        when(mockService.getFormat("CSV")).thenReturn(OutputFormat.CSV);
        when(mockService.getFormat("TSV")).thenReturn(OutputFormat.TSV);
        when(mockService.getFormat(null)).thenReturn(OutputFormat.VOTABLE);
        when(mockService.processQuery((Writer) anyObject(), mapCaptor.capture())).thenReturn(true);
        when(mockService.trustAuthHeader(any(HttpServletRequest.class))).thenReturn(false);

        this.mockMvc
                .perform(get("/tap/sync?" + queryString + "VOTABLE").header(VoKeys.VO_AUTH_HEADER_USER_ID, userId)
                        .header(VoKeys.VO_AUTH_HEADER_LOGIN_SYSTEM, loginSystem)
                        .header(VoKeys.VO_AUTH_HEADER_USER_PROJECTS, userProjects))
                .andExpect(status().isOk()).andDo(print()).andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(0).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(0).get(VoKeys.USER_PROJECTS));
        assertEquals("", mapCaptor.getAllValues().get(0).get(VoKeys.LOGIN_SYSTEM));

        this.mockMvc.perform(get("/tap/sync?" + queryString + "VOTABLE")).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(header().string("content-disposition", CoreMatchers.startsWith("attachment; filename=\"")))
                .andExpect(header().string("content-disposition", CoreMatchers.endsWith(".xml\"")));
        assertEquals(VoKeys.ANONYMOUS_USER, mapCaptor.getAllValues().get(1).get(VoKeys.USER_ID));
        assertEquals("", mapCaptor.getAllValues().get(1).get(VoKeys.USER_PROJECTS));
        assertEquals("", mapCaptor.getAllValues().get(0).get(VoKeys.LOGIN_SYSTEM));
    }

    /**
     * Test of the sync endpoint. Checks the error handling returns correct content type
     * 
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testSyncViaEndpointErrorMsg() throws Exception
    {
        String adql = "select * from obxxxs_core";
        String queryString = "request=doQuery&LANG=ADQL&query=" + adql;

        // use the default output format
        when(mockService.getFormat(null)).thenReturn(OutputFormat.CSV);
        when(mockService.processQuery((Writer) anyObject(), anyObject())).thenReturn(false);

        // content is empty as we are using a mockk service, but output format is using votable as an error is
        // excpected.
        this.mockMvc.perform(get("/tap/sync?" + queryString)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/x-votable+xml"))
                .andExpect(content().string(CoreMatchers.containsString("")));

    }

    /**
     * Test of the sync endpoint with invalid format requested.
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testSyncErrorMsgForInvalidFormat() throws Exception
    {
        String adql = "select * from obxxxs_core";
        String queryString = "request=doQuery&LANG=ADQL&query=" + adql + "&format=XXX";

        // will return votable xml with no error message as using a mock service
        this.mockMvc.perform(get("/tap/sync?" + queryString)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/x-votable+xml"));

    }

    /**
     * Basic test of async endpoint show all joblists show a joblist add a job show job in joblist show a job - check
     * completed get result
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAsyncFastSuccess() throws Exception
    {
        when(mockService.isTrustedUserId(any(HttpServletRequest.class))).thenReturn(true);

        // confirm the top level resource to list jobLists contains our given joblist
        // confirm the joblist resource returns our joblist called testtap
        this.mockMvc.perform(get("/tap/async")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/jobList/@name").string("async"));
        // post to create a new job with phase RUN to trigger immediate execution
        MockHttpServletResponse createResponse =
                this.mockMvc.perform(post("/tap/async/").param("Phase", "RUN").param("eRRor", "false"))
                        .andExpect(status().isSeeOther()).andDo(print()).andReturn().getResponse();
        // get location and jobId from result
        String loc = createResponse.getHeader("Location");
        // job Id is last part of url
        String[] splitUrl = loc.split("/");
        String jobId = splitUrl[splitUrl.length - 1];

        // confirm that our joblist now contains an entry
        this.mockMvc.perform(get("/tap/async/")).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/jobList/@name").string("async"))
                .andExpect(xpath("count(/jobList/jobRef)").number(is(1.0)));

        // Confirm out job details can be displayed and has completed
        this.waitForStatus(loc, "COMPLETED", SECONDS_TO_WAIT_FOR_JOB);
        MvcResult result = this.mockMvc.perform(get(loc)).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/job/phase").string("COMPLETED"))
                .andExpect(xpath("/job/results/result/@type").string("simple")).andReturn();

        String resultUrl = this.getResultsUrl(result.getResponse().getContentAsString());

        // get the results - for test job this will return the contents of a temp file with the job id in it
        this.mockMvc.perform(get(resultUrl)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/tab-separated-values"))
                .andExpect(content().string(CoreMatchers.containsString(jobId)));
    }

    @Test
    public void testAsyncJobListWithUntrustedUser() throws Exception
    {
        when(mockService.isTrustedUserId(any(HttpServletRequest.class))).thenReturn(false);

        this.mockMvc.perform(get("/tap/async/")).andExpect(status().isUnauthorized()).andDo(print());
    }
    
    /**
     * Basic test of a failed async endpoint add a job show a job - check errored Also ensure parameters are case
     * insensitive as per Tap spec.
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAsyncFastFail() throws Exception
    {
        // post to create a new job with phase RUN to trigger immediate execution
        MockHttpServletResponse createResponse =
                this.mockMvc.perform(post("/tap/async/").param("Phase", "RUN").param("doErRor", "true"))
                        .andExpect(status().isSeeOther()).andDo(print()).andReturn().getResponse();
        // get location and jobId from result
        String loc = createResponse.getHeader("Location");
        // job Id is last part of url

        this.waitForStatus(loc, "ERROR", SECONDS_TO_WAIT_FOR_JOB);
        // Confirm out job details can be displayed and has completed
        this.mockMvc.perform(get(loc)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("application/xml")).andExpect(xpath("/job/phase").string("ERROR"))
                .andExpect(xpath("/job/errorSummary/message").string("Bad Happened")).andReturn();
    }

    /**
     * Test async job endpoint deletes files add a job show a job - check completed delete a job try and get result
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAsyncJobDelete() throws Exception
    {
        when(mockService.isTrustedUserId(any(HttpServletRequest.class))).thenReturn(false);

        // post to create a new job with phase RUN to trigger immediate execution
        MockHttpServletResponse createResponse =
                this.mockMvc.perform(post("/tap/async/").param("Phase", "RUN").param("error", "false"))
                        .andExpect(status().isSeeOther()).andDo(print()).andReturn().getResponse();
        // get location and jobId from result
        String loc = createResponse.getHeader("Location");
        // job Id is last part of url
        String[] splitUrl = loc.split("/");
        String jobId = splitUrl[splitUrl.length - 1];

        // confirm that our joblist now contains an entry
        this.mockMvc.perform(get(loc)).andExpect(status().isOk()).andDo(print());

        this.waitForStatus(loc, "COMPLETED", SECONDS_TO_WAIT_FOR_JOB);
        // Confirm out job details can be displayed and has completed
        MvcResult result = this.mockMvc.perform(get(loc)).andExpect(status().isOk()).andDo(print())
                .andExpect(xpath("/job/phase").string("COMPLETED")).andReturn();
        String resultUrl = this.getResultsUrl(result.getResponse().getContentAsString());

        // get the results - for test job this will return the contents of a temp file with the job id in it
        this.mockMvc.perform(get(resultUrl)).andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType("text/tab-separated-values"))
                .andExpect(content().string(CoreMatchers.containsString(jobId)));

        // delete the job
        this.mockMvc.perform(post(loc).param("ACTION", "DELETE")).andExpect(status().isSeeOther()).andDo(print())
                .andReturn().getResponse();        
        // Check that the job now returns a 404 (UWS 4 fixed this)
        this.mockMvc.perform(get(loc)).andExpect(status().isNotFound()).andDo(print());
        // Check the results now return a 404
        this.mockMvc.perform(get(resultUrl)).andExpect(status().isNotFound()).andDo(print());
    }

    private void waitForStatus(String resultUrl, String status, long maxSecondsToWait) throws Exception
    {
        XPath pathStatus = XPathFactory.newInstance().newXPath();
        XPathExpression xStatus = pathStatus.compile("/job/phase");
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        int x = 0;
        while (x < maxSecondsToWait)
        {
            String content = this.mockMvc.perform(get(resultUrl)).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString();
            // remove first line
            content = content.substring(content.indexOf(">") + 1);
            Document document = builder.parse(new ByteArrayInputStream(content.getBytes("UTF-8")));
            String statusFound = (String) xStatus.evaluate(document, XPathConstants.STRING);
            if (statusFound.equalsIgnoreCase(status))
            {
                return;
            }
            TimeUnit.SECONDS.sleep(1l);
            x++;
        }
    }

    private String getResultsUrl(String content) throws Exception
    {
        XPath pathStatus = XPathFactory.newInstance().newXPath();
        XPathExpression xResult = pathStatus.compile("/job/results/result/@href");
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(content.getBytes("UTF-8")));
        String resultUrl = (String) xResult.evaluate(document, XPathConstants.STRING);
        return URLDecoder.decode(resultUrl, "UTF-8");
    }

    /**
     * Test async job endpoint deletes files using the http DELETE method
     *
     * @throws Exception
     *             from performing get request
     */
    @Test
    public void testCheckAsyncJobDeleteWithHttpMethod() throws Exception
    {
        when(mockService.isTrustedUserId(any(HttpServletRequest.class))).thenReturn(false);

        // post to create a new job with phase RUN to trigger immediate execution
        MockHttpServletResponse createResponse =
                this.mockMvc.perform(post("/tap/async/").param("Phase", "RUN").param("eRRor", "false"))
                        .andExpect(status().isSeeOther()).andDo(print()).andReturn().getResponse();
        // get location and jobId from result
        String loc = createResponse.getHeader("Location");

        // confirm that our joblist now contains an entry
        this.mockMvc.perform(get(loc)).andExpect(status().isOk()).andDo(print());

        this.waitForStatus(loc, "COMPLETED", SECONDS_TO_WAIT_FOR_JOB);

        // delete the job
        this.mockMvc.perform(delete(loc)).andExpect(status().isSeeOther()).andDo(print()).andReturn().getResponse();
        // confirm that our joblist now contains an entry
        this.mockMvc.perform(get(loc)).andExpect(status().isNotFound()).andDo(print());

    }
}
