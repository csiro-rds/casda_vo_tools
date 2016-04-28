package au.csiro.casda.votools.tap;

import javax.servlet.http.HttpServletRequest;

import uws.UWSException;
import uws.service.UWSUrl;

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
 * Extends the default UWSUrl class to handle double slashes in urls from topcat ie
 * http://localhost:8080/casda_vo_tools/tap//async when tap service is defined as
 * http://localhost:8080/casda_vo_tools/tap/
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class TapUWSUrl extends UWSUrl
{

    private static final long serialVersionUID = 1L;

    /**
     * Constructor, calls the UWSUrl constructor
     * 
     * @param toCopy
     *            the TAP url for UWS
     */
    public TapUWSUrl(UWSUrl toCopy)
    {
        super(toCopy);
    }

    /**
     * Constructor
     * 
     * @param baseURI
     *            the TAP base uri for
     * @throws UWSException
     *             if there is a problem with the base uri
     */
    public TapUWSUrl(String baseURI) throws UWSException
    {
        super(baseURI);
    }

    /**
     * Constructor
     * 
     * @param request
     *            the http request
     * @throws UWSException
     *             if there is a problem with the request
     */
    public TapUWSUrl(HttpServletRequest request) throws UWSException
    {
        super(request);
    }

    @Override
    protected void loadUwsURI()
    {
        if (this.uwsURI != null && this.uwsURI.contains("//"))
        {
            this.uwsURI = this.uwsURI.replace("//", "/");
        }
        super.updateRequestURL();
        super.loadUwsURI();
    }
}
