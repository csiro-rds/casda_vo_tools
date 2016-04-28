package au.csiro.casda.votools.availability;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.jaxb.availability.Availability;

/**
 * RESTful web service controller. Endpoint indicates whether the service is operable and the reliability of the
 * service.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@RestController
@RequestMapping("/{voServiceType}/availability")
public class AvailabilityController
{

    private AvailabilityService availabilityService;

    private static Logger logger = LoggerFactory.getLogger(AvailabilityController.class);

    /**
     * Constructor with args
     * 
     * @param availabilityService
     *            the availability service
     */
    @Autowired
    public AvailabilityController(AvailabilityService availabilityService)
    {
        this.availabilityService = availabilityService;
    }

    /**
     * @param voServiceType
     *            VO Service type required eg TAP, SCS
     * @return the availability of the VO Service type
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public @ResponseBody Availability checkAvailability(@PathVariable() VoServiceType voServiceType)
    {
        logger.info("Hit the controller for the '/{}/availability' url mapping - servicing request", voServiceType);
        return availabilityService.getAvailability(voServiceType);
    }
    
}