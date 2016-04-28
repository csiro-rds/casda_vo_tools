package au.csiro.casda.logging;

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


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 * Test the log message builder for unknown and other events.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class CasdaLogMessageBuilderTest
{

    /**
     * Test that a timed event with no time argument will just put 0, ie [timed:0]
     */
    @Test
    public void testTimeEventNoTime()
    {
        String message = new CasdaLogMessageBuilder(LogEvent.TIMED).toString();
        assertEquals("[duration:0] ", message);
    }

    /**
     * Test that a timed event with a valid argument will be formatted correctly, eg [time:123] message.
     */
    @Test
    public void testTimedEvent()
    {
        String message = new CasdaLogMessageBuilder(LogEvent.TIMED).addTimeTaken(1023).toString();
        assertEquals("[duration:1023] ", message);

        String messageWithText = new CasdaLogMessageBuilder(LogEvent.TIMED).addTimeTaken(234)
                .addCustomMessage("Some message").toString();
        assertEquals("[duration:234] Some message", messageWithText);
    }

    /**
     * Test that an unknown event will be formatted correctly, eg [Exxx] message.
     */
    @Test
    public void testUnknownEvent()
    {
        String message = new CasdaLogMessageBuilder(LogEvent.UNKNOWN_EVENT).toString();
        assertEquals("[Exxx] ", message);

        String messageWithText = new CasdaLogMessageBuilder(LogEvent.UNKNOWN_EVENT).add("some information").toString();
        assertEquals("[Exxx] some information", messageWithText);
    }

    /**
     * Test that a default (debug) event will output the arguments it is passed, if any.
     */
    @Test
    public void testDefaultEvent()
    {
        String message = new CasdaLogMessageBuilder(LogEvent.DEBUG).toString();
        assertEquals("", message);

        String messageWithText = new CasdaLogMessageBuilder(LogEvent.DEBUG).add("some information ")
                .add("and this also").toString();
        assertEquals("some information and this also", messageWithText);
    }
    
    @Test
    public void testAddAll()
    {
        CasdaLogMessageBuilder messageBuilder = new CasdaLogMessageBuilder(LogEvent.DEBUG);
        List<Object> args = new ArrayList<>();
        args.add(12);
        args.add("one");
        args.add("two");
        messageBuilder.addAll(args);
        
        assertEquals("12onetwo", messageBuilder.toString());
    }

}
