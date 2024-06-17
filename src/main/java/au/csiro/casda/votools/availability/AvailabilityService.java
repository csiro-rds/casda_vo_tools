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


import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.jaxb.availability.Availability;
import au.csiro.casda.votools.siap1.Siap1Service;
import au.csiro.casda.votools.ssap.SsapService;

/**
 * Service to generate service availability information
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Service
public class AvailabilityService
{
    private final SystemStatus systemStatus;

    private final HealthEndpoint healthEndpoint;
    
    private SsapService ssapService; 

    private static Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    private Siap1Service siap1Service;

    /**
     * @param healthEndpoint
     *            Spring Actuator HealthEndpoint - used here to check application
     * @param systemStatus
     *            contains system upSince
     * @param ssapService
     *            The SsapService 
     * @param siap1Service
     *            The SIAP v1 service 
     */
    @Autowired
    public AvailabilityService(HealthEndpoint healthEndpoint, SystemStatus systemStatus, SsapService ssapService,
            Siap1Service siap1Service)
    {
        this.systemStatus = systemStatus;
        this.healthEndpoint = healthEndpoint;
        this.ssapService = ssapService;
        this.siap1Service = siap1Service;
    }

    /**
     * @param voServiceType
     *            VO Service type required eg TAP, SCS
     * @return Availability to return information to VO clients about the service.
     */
    public Availability getAvailability(VoServiceType voServiceType)
    {
        Availability avail = new Availability();
        if (voServiceType == VoServiceType.ssa && !ssapService.isEnabled())
        {
            avail.setAvailable(false);
            avail.getNote().add("SSAP is not supported by this service");
        }
        else if (voServiceType == VoServiceType.sia1 && !siap1Service.isEnabled())
        {
            avail.setAvailable(false);
            avail.getNote().add("SIAP v1 is not supported by this service");
        }
        else
        {
            Status status = healthEndpoint.health().getStatus();
            if (Status.UP.equals(status))
            {
                avail.setAvailable(true);
                avail.getNote().add("");
            }
            else
            {
                avail.setAvailable(false);
                avail.getNote().add("Health check FAILED");
            }
        }
        try
        {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            GregorianCalendar upSinceDate = GregorianCalendar.from(systemStatus.getUpSince());
            XMLGregorianCalendar upSince = datatypeFactory.newXMLGregorianCalendar(upSinceDate);
            avail.setUpSince(upSince);
        }
        catch (DatatypeConfigurationException e)
        {
            logger.error("Exception creating XMLGregorianCalendar for availability", e);
        }
        return avail;
    }

}
