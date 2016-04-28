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


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 
 * Static formatter methods for CASDA logging.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaFormatter
{

    private static final FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS",
            TimeZone.getTimeZone("UTC"));

    /**
     * Format a list of files for logging.
     * 
     * @param files
     *            list of files
     * @return filenames concatenated with comma delimiter
     */
    public static String formatFileListForLog(List<Path> files)
    {
        List<String> filenames = new ArrayList<>();
        for (Path path : files)
        {
            filenames.add(path.toString());
        }
        return StringUtils.join(filenames, ", ");
    }

    /**
     * Format a Date/Time object for logging.
     * 
     * @param dateTime
     *            the date to format
     * @return date formatted using yyyy-MM-dd HH:mm:ss.SSSZ
     */
    public static String formatDateTimeForLog(Date dateTime)
    {
        return DATE_FORMATTER.format(dateTime) + "Z";
    }

}
