package au.csiro.casda.votools.tap;

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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import uws.UWSException;

/**
 * Tests for the TapUWSUrl.
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class TapUWSUrlTest
{

    @Test
    public void testTapUWSUrlRemovesDoubleSlashes() throws UWSException, MalformedURLException
    {
        TapUWSUrl tapUWSUrl = new TapUWSUrl("/tap");
        tapUWSUrl.load(new URL("http://localhost:8080/casda_vo_tools/tap//async"));
        assertThat(tapUWSUrl.getRequestURL(), is("http://localhost:8080/casda_vo_tools/tap/async"));
    }

    @Test
    public void testTapUWSUrlIsNullSafe() throws UWSException, MalformedURLException
    {
        TapUWSUrl tapUWSUrl = new TapUWSUrl("/tap");
        tapUWSUrl.load((URL) null);
        assertThat(tapUWSUrl.getRequestURL(), is(nullValue()));
    }
}
