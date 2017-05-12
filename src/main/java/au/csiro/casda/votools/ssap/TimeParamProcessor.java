package au.csiro.casda.votools.ssap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import au.csiro.atnf.voresource.ObservationTime;
import au.csiro.casda.votools.utils.Utils;

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
 * A processor for Time SSAP parameter types. These may be either a single value or a range, as defined in section 2.1
 * of the IVOA Simple Image Access Version 2.0 Recommendation.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class TimeParamProcessor implements SsapParamProcessor
{
    private static final String DATE_TIME_PATTERN = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]";
    private static final String DATE_PATTERN = "[0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?";
    private static final String SINGLE_DATE_TIME_PATTERN = "(" + DATE_PATTERN + ")|(" + DATE_TIME_PATTERN + ")";
    private static final String DATE_RANGE_PATTERN =
            "(" + SINGLE_DATE_TIME_PATTERN + ")?[/](" + SINGLE_DATE_TIME_PATTERN + ")?";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> validate(String paramName, String[] values)
    {

        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            String testParam = StringUtils.trimToEmpty(param);
            // May be either empty, a single value or range or a list of values or ranges
            // e.g.
            if (testParam.length() != 0 && !Pattern.matches(SINGLE_DATE_TIME_PATTERN, testParam)
                    && !Pattern.matches(DATE_RANGE_PATTERN, testParam))
            {
                errorList.add(
                        String.format(USAGE_FAULT_MSG, "Invalid " + paramName.toUpperCase() + " value " + testParam));
            }
            else
            {
                for (String date : param.trim().split("/"))
                {
                    if (date.isEmpty())
                    {
                        continue;
                    }
                    String[] dateParts = date.split("-|T");
                    if (dateParts.length > 1)
                    {
                        int month = Integer.parseInt(dateParts[1]);
                        final int maxMonth = 12;
                        if (month < 1 || month > maxMonth)
                        {
                            errorList.add(String.format(USAGE_FAULT_MSG,
                                    "Invalid month in " + paramName.toUpperCase() + " value " + testParam));
                        }
                    }
                    if (dateParts.length > 2)
                    {
                        int day = Integer.parseInt(dateParts[2]);
                        final int maxDaysInMonth = 31;
                        if (day < 1 || day > maxDaysInMonth)
                        {
                            errorList.add(String.format(USAGE_FAULT_MSG,
                                    "Invalid day in " + paramName.toUpperCase() + " value " + testParam));
                        }
                    }
                }
                if (errorList.isEmpty())
                {
                    String[] dates = param.trim().split("/");
                    if (dates.length == 2 && !dates[0].isEmpty() && !dates[1].isEmpty())
                    {
                        String[] mjdDates = convertDates(dates);
                        if (mjdDates[0].compareTo(mjdDates[1]) > 0)
                        {
                            errorList.add(String.format(USAGE_FAULT_MSG,
                                    "Invalid " + paramName.toUpperCase() + " value " + param));
                        }
                    }
                }
            }
        }

        return errorList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        StringBuilder fieldSelect = new StringBuilder();

        for (String criterion : criteria)
        {
            criterion = criterion.trim();
            String[] dates = criterion.split("/");

            String[] mjdDates = convertDates(dates);
            String template;

            if (criterion.matches(SINGLE_DATE_TIME_PATTERN))
            {
                // Match any row which overlaps the specified value
                template = "%1$s <= " + mjdDates[0] + " AND %2$s >= " + mjdDates[0];
            }
            else if (criterion.equals("/"))
            {
                // Match any record which has a min and a max value
                template = "%s IS NOT NULL AND %s IS NOT NULL";
            }
            else if (criterion.endsWith("/"))
            {
                // Match any row which has a max value more than the specified value
                template = "%2$s >= " + mjdDates[0];
            }
            else if (criterion.startsWith("/"))
            {
                // Match any row which has a min value less than the specified value
                template = "%1$s <= " + mjdDates[1];
            }
            else
            {
                // Match any row which overlaps the specified range
                template = "%1$s >= " + mjdDates[0] + " AND %2$s <= " + mjdDates[1];
            }

            Utils.appendFragment(fieldSelect, String.format(template, minColName, maxColName));
        }

        return fieldSelect.toString();
    }

    /**
     * 
     * @param dates
     *            the dates to be converted
     * @return the dates in MJD format
     */
    private String[] convertDates(String[] dates)
    {
        String[] mjdDates = new String[dates.length];
        for (int i = 0; i < dates.length; i++)
        {
            if (StringUtils.isNotBlank(dates[i]))
            {
                String calcDate = dates[i].trim();

                // expand out partial dates
                if (calcDate.matches(DATE_PATTERN))
                {
                    String[] dateParts = calcDate.split("-");
                    if (dateParts.length == 1)
                    {
                        calcDate += i == 0 ? "-01-01" : "-12-31";
                    }
                    else if (dateParts.length == 2)
                    {
                        if (i == 0)
                        {
                            calcDate += "-01";
                        }
                        else
                        {
                            DateTime endOfMonth = DateTime.parse(calcDate + "-01").dayOfMonth().withMaximumValue();
                            calcDate = endOfMonth.toString("YYY-MM-DD");
                        }
                    }
                    calcDate += (i == 0) ? "T00:00:00" : "T23:59:59";
                }

                mjdDates[i] = String
                        .valueOf(ObservationTime.dateTimeToModifiedJulianDate(DateTime.parse(calcDate + "+00:00")));
            }
        }
        return mjdDates;
    }

}
