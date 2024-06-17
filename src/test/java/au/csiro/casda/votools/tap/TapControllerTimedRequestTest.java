package au.csiro.casda.votools.tap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import au.csiro.casda.votools.tap.TapController.TimedRequest;
import au.csiro.casda.votools.utils.VoKeys;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Test for Tap Controller timed requests
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class TapControllerTimedRequestTest
{
    @Test
    public void testAsyncAddsUserInfoIfProxied() throws Exception
    {
        String userId = "smi123";
        String userProjects = "all";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)).thenReturn(userId);
        when(request.getHeader(VoKeys.VO_AUTH_HEADER_USER_PROJECTS)).thenReturn(userProjects);

        TimedRequest timedRequest = new TimedRequest(request, TapService.SUBMITTED_MODE_ASYNC, true);

        assertEquals(userId, timedRequest.getParameterMap().get(VoKeys.USER_ID)[0]);
        assertEquals(userProjects, timedRequest.getParameterMap().get(VoKeys.USER_PROJECTS)[0]);

        assertEquals(userId, timedRequest.getParameter(VoKeys.USER_ID));
        assertEquals(userProjects, timedRequest.getParameter(VoKeys.USER_PROJECTS));
    }

    @Test
    public void testAsyncIgnoresUserInfoIfNotproxied() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);

        TimedRequest timedRequest = new TimedRequest(request, TapService.SUBMITTED_MODE_ASYNC, false);

        assertEquals(VoKeys.ANONYMOUS_USER, timedRequest.getParameterMap().get(VoKeys.USER_ID)[0]);
        assertEquals(StringUtils.EMPTY, timedRequest.getParameterMap().get(VoKeys.USER_PROJECTS)[0]);

        assertEquals(VoKeys.ANONYMOUS_USER, timedRequest.getParameter(VoKeys.USER_ID));
        assertEquals(StringUtils.EMPTY, timedRequest.getParameter(VoKeys.USER_PROJECTS));
    }
}
