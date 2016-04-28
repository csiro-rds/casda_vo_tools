package au.csiro.casda.votools.uws;

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


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.csiro.casda.votools.config.ConfigurationException;
import uws.UWSException;

/**
 * Interface for UWS to allow for different implementations in controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public interface UWServiceInterface
{
    /**
     * @param request
     *            The job request
     * @param response
     *            The Job response
     * @return nothing
     * @throws UWSException
     *             If an UWS error occurs
     * @throws IOException
     *             If an IOException occurs
     */
    boolean executeRequest(HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException;

    /**
     * Checks if the service is ready to serve requests.
     * @return true if the service is ready
     * @throws ConfigurationException if configuration problem occurs
     */
    boolean isReady() throws ConfigurationException;
}
