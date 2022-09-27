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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.utils.VoKeys;

/**
 * RESTful web service controller for the Tap Capabilities endpoint. Describes the functions available and interfaces to
 * use them.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RestController
@RequestMapping("/{voServiceType}/capabilities")
public class CapabilitiesController
{

    private CapabilitiesService capabilitiesService;

    private static Logger logger = LoggerFactory.getLogger(CapabilitiesController.class);

    /**
     * Constructor with args
     * 
     * @param capabilitiesService
     *            the capabilities service
     */
    @Autowired
    public CapabilitiesController(CapabilitiesService capabilitiesService)
    {
        this.capabilitiesService = capabilitiesService;
    }

    /**
     * @param voServiceType
     *            VO Service type required eg TAP, SCS
     * @param capabilitiesUrl
     *            the url used in the capabilities report, this supports providing the capabilities report both local
     *            and proxied end points.
     * @return the capabilities of the VO Service type
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public @ResponseBody Object getCapabilities(@PathVariable() VoServiceType voServiceType, @RequestHeader(
            value = VoKeys.VO_HEADER_CAPABILITIES_URL, required = false) String capabilitiesUrl)
    {
        logger.info("Hit the controller for the '/{}/capabilities' url mapping - servicing request", voServiceType);
        checkReady();
        
        if (voServiceType == VoServiceType.ssa)
        {
            return getSsaCapabilities(capabilitiesUrl);
        }
        else if (voServiceType == VoServiceType.sia1)
        {
            return getSia1Capabilities(capabilitiesUrl);
        }
        else if (voServiceType == VoServiceType.sia2)
        {
            return getSia2Capabilities(capabilitiesUrl);
        }
        else if (voServiceType == VoServiceType.datalink)
        {
            return getDatalinkCapabilities(capabilitiesUrl);
        }
        else if (voServiceType == VoServiceType.tap)
        {
            return getTapCapabilities(capabilitiesUrl);
        }
        else if (voServiceType == VoServiceType.scs)
        {
            return getScsCapabilities(capabilitiesUrl);
        }
        return null;
    }

    private ModelAndView getScsCapabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("scs/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getScsConfigParams(capabilitiesUrl));
        return mav;
    }

    private ModelAndView getTapCapabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("tap/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getTapConfigParams(capabilitiesUrl));
        return mav;
    }

    private ModelAndView getDatalinkCapabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("datalink/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getDatalinkConfigParams(capabilitiesUrl));
        return mav;
    }

    private ModelAndView getSia1Capabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("sia1/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getSia1ConfigParams(capabilitiesUrl));
        return mav;
    }

    private ModelAndView getSia2Capabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("sia2/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getSia2ConfigParams(capabilitiesUrl));
        return mav;
    }

    private ModelAndView getSsaCapabilities(String capabilitiesUrl)
    {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("ssa/capabilities.xml");
        mav.getModel().putAll(capabilitiesService.getSsaConfigParams(capabilitiesUrl));
        return mav;
    }

    /**
     * Checks is this controller is ready to serve requests by checking readiness of the services it depends on. Updates
     * configurable fields. If not ready, throws a Runtime Exception.
     * 
     */
    private void checkReady()
    {
        try
        {
            if (capabilitiesService == null || !capabilitiesService.isReady())
            {
                throw new ConfigurationException("CapabilitiesController is not ready to process requests.");
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }

    }

}