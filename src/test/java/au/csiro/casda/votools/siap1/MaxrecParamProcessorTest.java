package au.csiro.casda.votools.siap1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Test the SIA1 MaxrecParamProcessor
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class MaxrecParamProcessorTest
{

    @Test
    public void testValidateValid()
    {
        MaxrecParamProcessor processor = new MaxrecParamProcessor();

        List<String> errors = processor.validate("MAXREC", new String[] { "20" });
        assertThat(errors, empty());

        errors = processor.validate("MAXREC", new String[] { "9" });
        assertThat(errors, empty());

        errors = processor.validate("MAXREC", new String[] { "03" });
        assertThat(errors, empty());
    }

    @Test
    public void testValidateInValid()
    {
        MaxrecParamProcessor processor = new MaxrecParamProcessor();

        List<String> errors = processor.validate("MAXREC", new String[] { "20 10" });
        assertThat(errors, contains("UsageFault: Query can only contain a single MAXREC value"));

        errors = processor.validate("MAXREC", new String[] { "9a" });
        assertThat(errors,
                contains("UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number"));

        errors = processor.validate("MAXREC", new String[] { "3e2" });
        assertThat(errors,
                contains("UsageFault: The maximum amount of records is invalid. MAXREC must be a valid whole number"));
    }

    @Test
    public void testBuildQuery()
    {
        MaxrecParamProcessor processor = new MaxrecParamProcessor();
        assertEquals("", processor.buildQuery("foo", "bar", new String[] { "20" }));
    }

}
