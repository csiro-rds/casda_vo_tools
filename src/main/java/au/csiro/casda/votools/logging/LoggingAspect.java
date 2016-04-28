package au.csiro.casda.votools.logging;

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


import java.time.Duration;
import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.csiro.casda.logging.CasdaCommonEvents;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.CasdaLoggingSettings;
import au.csiro.casda.votools.VoToolsApplication;

/**
 * Logging aspect - intercepts the CSIRO web endpoints and adds logging information to the MDC.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Aspect
@Component
public class LoggingAspect
{
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings(VoToolsApplication.APPLICATION_NAME, null);

    /**
     * Around any public csiro method, make sure the logging settings is updated, and an instance id is added. Also log
     * timing information before and after the request call.
     * 
     * @param proceedingJoinPoint
     *            the method that has been intercepted
     * @return Object return result from the method that has been intercepted
     * @throws Throwable
     *             from proceedingJoinPoint.proceed()
     */
    @Around("execution(* au.csiro..*(..))")
    public Object addLoggingContextInformation(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        loggingSettings.addGeneralLoggingSettings();
        loggingSettings.addLoggingInstanceId();

        Object value = null;

        // log the start time
        Instant startTime = Instant.now();
        String joinPoint = proceedingJoinPoint.toShortString();
        logger.debug("{}", CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(CasdaCommonEvents.E063).add(joinPoint));

        value = proceedingJoinPoint.proceed();

        // log the end time
        Duration timeTaken = Duration.between(startTime, Instant.now());
        logger.debug("{}", CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(CasdaCommonEvents.E064).add(joinPoint)
                .addTimeTaken(timeTaken.toMillis()));

        return value;

    }

}
