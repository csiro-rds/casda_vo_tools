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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

/**
 * 
 * Test methods formatter methods.
 * 
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 *
 */
public class CasdaFormatterTest
{

    /**
     * Test that the format file list method concatenates the list of filenames with a comma delimiter. 
     */
    @Test
    public void testFormatFileList()
    {
        List<Path> pathList = new ArrayList<>();
        for (int j = 0; j < 3; j++)
        {
            pathList.add(new File("something" + j + ".txt").toPath());
        }
        assertEquals("something0.txt, something1.txt, something2.txt", CasdaFormatter.formatFileListForLog(pathList));
    }

    /**
     * Test that the format date time method uses the expected format, eg Thu, 02 Dec 2004 10:15:16 +1100.
     */
    @Test
    public void testFormatDateTimeForLog()
    {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("Australia/Canberra"));
        calendar.set(Calendar.YEAR, 2004);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR, 10);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 16);
        calendar.set(Calendar.MILLISECOND, 123);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        assertEquals("2004-12-01 23:15:16.123Z", CasdaFormatter.formatDateTimeForLog(calendar.getTime()));
    }

}
