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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;

import au.csiro.casda.votools.VoServiceType;
import au.csiro.casda.votools.jaxb.availability.Availability;

/**
 * Unit tests for availability service layer
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class AvailabilityServiceTest
{

    @Mock
    private SystemStatus mockSystemStatus;

    @Mock
    private HealthEndpoint mockHealthEndpoint;

    @InjectMocks
    private AvailabilityService availabilityService;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAvailableUp()
    {
        ZonedDateTime cal = ZonedDateTime.now(ZoneId.of("UTC"));
        Mockito.when(mockSystemStatus.getUpSince()).thenReturn(cal);
        Health health = Health.up().build();
        Mockito.when(mockHealthEndpoint.invoke()).thenReturn(health);

        Availability availability = availabilityService.getAvailability(VoServiceType.tap);
        assertThat(availability.isAvailable(), is(true));
        assertThat(availability.getNote(), contains(""));
    }

    @Test
    public void testGetAvailableDown()
    {
        ZonedDateTime cal = ZonedDateTime.now(ZoneId.of("UTC"));
        Mockito.when(mockSystemStatus.getUpSince()).thenReturn(cal);
        Health health = Health.down().build();
        Mockito.when(mockHealthEndpoint.invoke()).thenReturn(health);

        Availability availability = availabilityService.getAvailability(VoServiceType.tap);
        assertThat(availability.isAvailable(), is(false));
        assertThat(availability.getNote(), contains("Health check FAILED"));
    }
}
