package au.csiro.casda.votools.surveys;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import au.csiro.casda.votools.config.ConfigurationException;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * SIAP1 surveys REST Controller that handles the configuration and production of SIAP1 surveys.
 * 
 * Copyright 2022, CSIRO Australia All rights reserved.
 *
 */
@RestController
@RequestMapping("/sia1/surveys")
public class SiapSurveysController
{
    @Autowired
    private SiapSurveysService surveysService;

    private static Logger logger = LoggerFactory.getLogger(SiapSurveysController.class);

    /**
     * Get SIA1 surveys.
     * 
     * @param response
     *            The HttpServletResponse.
     * @return the availability of the VO Service type
     */
    @RequestMapping(method = RequestMethod.GET, produces = { "text/xml", "application/xml" })
    public @ResponseBody ModelAndView getSurveys(HttpServletResponse response)
    {
        checkReady();

        ModelAndView mav = new ModelAndView();
        mav.setViewName("sia1/surveys.xml");

        // otherwise, build the surveys page from config.
        if (surveysService.hasSurveys())
        {
            mav.getModel().put("surveys", surveysService.getSiapSurveys());
        }
        else
        {
            // if neither method is available. Return 404. SIA1 surveys do not exist.
            mav.addObject("error", "No surveys configured in this environment.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return mav;
    }

    private void checkReady()
    {
        try
        {
            if (this.surveysService == null || !this.surveysService.isReady())
            {
                throw new ConfigurationException(
                        SiapSurveysController.class.getName() + " is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            logger.warn("Check Ready failed.");
            throw new RuntimeException(e);
        }
    }
}
