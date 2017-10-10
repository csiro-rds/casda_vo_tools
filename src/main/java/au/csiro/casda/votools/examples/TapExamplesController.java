package au.csiro.casda.votools.examples;

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
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Tap Examples REST Controller that handles the configuration and production of TAP examples.
 * 
 * Copyright 2017, CSIRO Australia All rights reserved.
 *
 */
@RestController
@RequestMapping("/tap/examples")
public class TapExamplesController
{
    @Autowired
    private TapExamplesService examplesService;

    private static Logger logger = LoggerFactory.getLogger(TapExamplesController.class);

    /**
     * Get TAP examples.
     * 
     * @param response
     *            The HttpServletResponse.
     * @return the availability of the VO Service type
     */
    @RequestMapping(method = RequestMethod.GET, produces = { "application/xhtml+xml", "text/html" })
    public @ResponseBody ModelAndView examples(HttpServletResponse response)
    {
        checkReady();

        return examplesService.buildResponse(response);
    }

    private void checkReady()
    {
        try
        {
            if (this.examplesService == null || !this.examplesService.isReady())
            {
                throw new ConfigurationException(
                        TapExamplesController.class.getName() + " is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            logger.warn("Check Ready failed.");
            throw new RuntimeException(e);
        }
    }
}
