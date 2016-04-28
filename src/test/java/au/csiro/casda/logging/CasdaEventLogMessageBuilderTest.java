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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.TimeZone;

import org.junit.Test;

/**
 * 
 * Test the message builder for known events.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class CasdaEventLogMessageBuilderTest
{

    private enum CasdaTestEvent implements CasdaEvent 
    {
        TEST("%d", "E123", null),
        TEST_MULTIPLE_OBJ("%d %s %s", "E111", "type");

        private final String formatString;
        private final String code;
        private final String type;
        
        private CasdaTestEvent(String formatString, String code, String type)
        {
            this.formatString = formatString;
            this.code = code;
            this.type = type;
        }
        
        @Override
        public String getFormatString()
        {
            return formatString;
        }

        @Override
        public String getCode()
        {
            return code;
        }

        @Override
        public String getType()
        {
            return type;
        }
        
    }
    
    /**
     * Test building a message with a missing argument. This should throw an exception.
     */
    @Test(expected = MissingFormatArgumentException.class)
    public void testMissingArgument()
    {
        new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST).toString();
        fail("Message building should throw an exception if no argument is provided");
    }

    /**
     * Test building a message with an argument of the incorrect type. This should throw an exception.
     */
    @Test(expected = IllegalFormatConversionException.class)
    public void testWrongArgumentType()
    {
        new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST).add("wrong argument type").toString();
        fail("Message building should throw an exception if the wrong type of argument is provided");
    }

    /**
     * Test adding a string adds it to the args list.
     */
    @Test
    public void testAddString()
    {
        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST);
        messageBuilder.add("dummy string");
        assertEquals("dummy string", messageBuilder.args.get(messageBuilder.args.size() - 1));
    }

    /**
     * Test adding a path list adds a string representation of the path list to the arguments.
     */
    @Test
    public void testAddPathList()
    {

        List<Path> pathList = new ArrayList<>();
        for (int j = 0; j < 3; j++)
        {
            pathList.add(new File("something" + j + ".txt").toPath());
        }

        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST);
        messageBuilder.add(pathList);
        assertEquals("something0.txt, something1.txt, something2.txt",
                messageBuilder.args.get(messageBuilder.args.size() - 1));

    }

    /**
     * Test adding a date adds a string representation of the date to the arguments.
     */
    @Test
    public void testAddDate()
    {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Australia/Canberra"));
        calendar.set(Calendar.YEAR, 2004);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR, 10);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 16);
        calendar.set(Calendar.MILLISECOND, 156);
        calendar.set(Calendar.AM_PM, Calendar.AM);

        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST);
        messageBuilder.add(calendar.getTime());

        assertEquals("2004-12-01 23:15:16.156Z", messageBuilder.args.get(messageBuilder.args.size() - 1));
    }

    /**
     * Test adding a path adds a Path, and the string representation of the path is expected.
     */
    @Test
    public void testAddPath()
    {
        Path path = new File("somepath.txt").toPath();
        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST);
        messageBuilder.add(path);
        Object lastArgElement = messageBuilder.args.get(messageBuilder.args.size() - 1);
        assertTrue(lastArgElement instanceof Path);
        assertEquals("somepath.txt", lastArgElement.toString());
    }

    /** 
     * Test adding time taken
     */
    @Test
    public void testMessageWithTimeTaken()
    {
        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST);
        assertEquals(-1, messageBuilder.timeTaken);
        assertFalse(messageBuilder.timedEvent);
        messageBuilder.addTimeTaken(150);
        assertEquals(0, messageBuilder.args.size());

        assertEquals(150, messageBuilder.timeTaken);
        assertTrue(messageBuilder.timedEvent);
    }
    
    @Test
    public void testAddAll()
    {
        CasdaEventLogMessageBuilder messageBuilder = new CasdaEventLogMessageBuilder(CasdaTestEvent.TEST_MULTIPLE_OBJ);
        List<Object> args = new ArrayList<>();
        args.add(12);
        args.add("one");
        args.add("two");
        messageBuilder.addAll(args);
        
        assertEquals(3, messageBuilder.args.size());
        assertEquals("[E111] [type] [12 one two] ", messageBuilder.toString());
    }

}
