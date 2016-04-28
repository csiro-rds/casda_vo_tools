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


import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Stores the date the system has been up since. Could be expanded to include further attributes used in availability
 * endpoint such as scheduled downtime.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Component
@Scope("application")
public class SystemStatus
{

    private ZonedDateTime upSince;

    @Value("${log.timezone}")
    private String logTimezone = "UTC";

    /**
     * Constructor, sets the date that the system has been up since to now
     */
    public SystemStatus()
    {
        upSince = ZonedDateTime.now(ZoneId.of(logTimezone));
    }

    public ZonedDateTime getUpSince()
    {
        return upSince;
    }

}
