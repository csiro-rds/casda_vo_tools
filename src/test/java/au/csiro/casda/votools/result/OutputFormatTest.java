package au.csiro.casda.votools.result;

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

import org.junit.jupiter.api.Test;

/**
 * Tests the OutputFormat Enum used for format query responses.
 * 
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 * 
 */
public class OutputFormatTest
{

    @Test
    public void testValues()
    {
        assertThat(OutputFormat.findMatchingFormat("VOTABLE").toString(), is("VOTABLE"));
        assertThat(OutputFormat.findMatchingFormat("votable").toString(), is("VOTABLE"));
        assertThat(OutputFormat.findMatchingFormat("xyz"), is(nullValue()));
        assertThat(OutputFormat.findMatchingFormat("csv").getFileExtension(), is("csv"));
    }
}
