package au.csiro.casda.votools.surveys;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.BaseTest;

/**
 * Tests for the SIAP1 surveys controller.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class SiapSurveysControllerTest extends BaseTest
{

    @Mock
    private SiapSurveysService mockService;

    @InjectMocks
    private SiapSurveysController surveysController;

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
        this.mockMvc = MockMvcBuilders.standaloneSetup(surveysController).build();

        doReturn(true).when(mockService).isReady();
    }

    @Test
    public void testGetSurvey() throws Exception
    {
        List<SiapSurvey> surveyList = new ArrayList<>();
        when(mockService.getSiapSurveys()).thenReturn(surveyList);
        when(mockService.hasSurveys()).thenReturn(true);

        this.mockMvc.perform(get("/sia1/surveys")).andExpect(status().isOk()).andDo(print())
                .andExpect(view().name("sia1/surveys.xml")).andExpect(model().attribute("surveys", surveyList));
    }

    @Test
    public void testGetSurveyNoSurveys() throws Exception
    {
        this.mockMvc.perform(get("/sia1/surveys")).andExpect(status().isNotFound()).andDo(print())
                .andExpect(view().name("sia1/surveys.xml"))
                .andExpect(model().attribute("error", "No surveys configured in this environment."));
    }

}
