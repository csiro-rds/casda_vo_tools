package au.csiro.casda.votools.siap2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

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
 * Verify the workings of IgnoredParamProcessor.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class IgnoredParamProcessorTest
{
    private IgnoredParamProcessor processor = new IgnoredParamProcessor();

    @Test
    public void testValidate()
    {
        assertThat("Expected 'REQUEST=query' to be valid.", processor.validate("REQUEST", new String[] { "query" }),
                is(empty()));
    }

    @Test
    public void testBuildQuery()
    {
        assertThat("Expected no criteria for 'REQUEST=query'.",
                processor.buildQuery("REQUEST", "", new String[] { "query" }), is(""));
    }
}
