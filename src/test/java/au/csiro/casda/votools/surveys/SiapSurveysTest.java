package au.csiro.casda.votools.surveys;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for the class SiapSurveys
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class SiapSurveysTest
{

    @Test
    public void testSiapSurveysConstructor()
    {
        SiapSurveys siapSurveys = new SiapSurveys(getMockSurveys());
        SiapSurvey survey = siapSurveys.getSiapSurveys().get(0);
        assertEquals("RACS-Low", survey.getCode());
        assertEquals("Rapid ASKAP Continuum Survey (Low)", survey.getName());
        assertEquals("Conducted...", survey.getDescription());
        assertEquals("(dataproduct_type = 'cube' and calib_level = 3 and filename LIKE ‘RACS%DR1%A.fits’)",
                survey.getWhereClause());
        assertEquals(1, siapSurveys.getSiapSurveys().size());
    }

    private List<Map<String, String>> getMockSurveys()
    {
        List<Map<String, String>> surveys = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put(SiapSurveysService.SurveyKeys.CODE.getKey(), "RACS-Low");
        map.put(SiapSurveysService.SurveyKeys.NAME.getKey(), "Rapid ASKAP Continuum Survey (Low)");
        map.put(SiapSurveysService.SurveyKeys.DESCRIPTION.getKey(), "Conducted...");
        map.put(SiapSurveysService.SurveyKeys.WHERE_CLAUSE.getKey(),
                "(dataproduct_type = 'cube' and calib_level = 3 and filename LIKE ‘RACS%DR1%A.fits’)");
        surveys.add(map);
        return surveys;
    }

}
