package au.csiro.casda.votools.config;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import au.csiro.casda.votools.utils.Utils;
import uws.UWSToolBox;

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

/**
 * VO configuration controller
 * 
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Controller
public class ConfigurationController
{
    private static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @Autowired
    private ConfigurationService cfgService;

    /**
     * Home page
     * 
     * @param request
     *            HTTP servlet request object
     * @param response
     *            HTTP servlet response object
     * @return the model-and-view
     * @throws Exception
     *             an exception
     */
    @RequestMapping(value = "/configure/home", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) throws Exception
    {

        ModelAndView result = new ModelAndView("configure");
        result.getModel().put("config", "");
        String password = Utils.retrieveFromFile()[1];
        result.getModel().put("passwordSetup", Utils.DEFAULT_PASSWORD.equals(password));

        logger.info("Hit the controller for the '/configure' url mapping - servicing {} request", request.getMethod());
        return result;
    }

    /**
     * Form submission
     * 
     * @param request
     *            HTTP servlet request object
     * @param response
     *            HTTP servlet response object
     * @param redirectAttributes
     *            an object for carrying values between redirects
     * @return result configuration or error message
     */
    @RequestMapping(value = "/configure/act", method = RequestMethod.POST, produces = "text/html")
    public ModelAndView configure(HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttributes)
    {
        logger.info("Hit the controller for the '/configure/act' url mapping - servicing {} request.",
                request.getMethod());
        Map<String, String> paramsMap = UWSToolBox.getParamsMap(request);
        ModelAndView result = new ModelAndView("configure");
        result.getModel().put("applied", false);
        result.getModel().put("explored", false);
        result.getModel().put("passwordError", false);
        result.getModel().put("ioError", false);

        try
        {
            if (Utils.DEFAULT_PASSWORD.equals(Utils.retrieveFromFile()[1]))
            {
                if (Utils.validatePassword(paramsMap.get("password1"), paramsMap.get("password2")))
                {
                    Utils.writeToFile(new String[] { Utils.USERNAME, paramsMap.get("password1") });
                }
                else
                {
                    redirectAttributes.addFlashAttribute("passwordError", "true");
                }
                // done in case a user logs in, then logs out, then tries to login in again. this ensures they still get
                // redirected back to the configuration page
                return new ModelAndView("redirect:/configure/home");
            }
            else
            {
                try
                {
                    String text = cfgService.process(request, response, paramsMap);
                    result.getModel().put("config", text);
                    if ("APPLY".equals(paramsMap.get("submit")))
                    {
                        result.getModel().put("success", true);
                        result.getModel().put("successMessage", "New configuration successfully saved!");
                    }
                    else if ("EXPLORE".equals(paramsMap.get("submit")))
                    {
                        result.getModel().put("success", true);
                        result.getModel().put("successMessage", "Exploration successful!");
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to process request: ", e);
                    
                    String message = e.getMessage();
                    if(StringUtils.isBlank(message))
                    {
                        message = "This action could not be completed. For more details on this issue please refer "
                                + "to the logs.";
                    }
                    result.getModel().put("config", message);
                }

                return result;
            }
        }
        catch (IOException ioe)
        {
            // catches IOException thrown by writing password to file
            logger.debug(ioe.getMessage());
            result.getModel().put("ioError", true);
            return result;
        }

    }
}
