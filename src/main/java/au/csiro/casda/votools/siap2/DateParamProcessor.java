package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
 * A processor for Date SIAP parameter types. These may be either a single value or a range, as defined in section 2.1
 * of the IVOA Simple Image Access Version 2.0 Recommendation.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class DateParamProcessor implements SiapParamProcessor
{
    private static final String NUMBER = "\\d+(\\.\\d+)?";
    //TODO once the siap2 specs are finilised in regards to date formats this code can 
    //either be reinstated or deleted
    //private static final String DATE_ONLY = "^[0-9]{4}-[0-9]{2}-[0-9]{2}?";
    //private static final String DATE_TIME = "^[0-9]{4}-[0-9]{2}-[0-9]{2}(T[0-9]{2}:[0-9]{2}:[0-9]{2}(.[0-9]*)?)??";
    //private static final String[][] DATE_ENDINGS =
    //        new String[][] { { "T00:00:00.000Z", ".000Z" }, { "T23:59:59.999Z", ".999Z" } };
    private static final String ERROR_MESSAGE_INVALID =
            "Your query contained an invalid date format. This query accepts the MJD format, e.g.\t55678.123456";
    private static final String ERROR_MESSAGE_EMPTY =
            "Your query contained no dates. This query must contain either a single date or a minimum and a maximum "
                    + "date in the MJD format, e.g.\t55678.123456";
    private static final String ERROR_MESSAGE_TOO_MANY_DATES = "You have entered too many dates please no more than "
            + "two dates (a starting date and an ending date for the range in question)";
    private static final String ERROR_MESSAGE_DATE_ORDER = 
            "The first date in your query must be earlier (chronoligically) than the second";

    /**
     * {@inheritDoc}
     */
    @Override
    public Siap2ParamType getSupportedParamType()
    {
        return Siap2ParamType.DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> validate(String paramName, String[] values)
    {

        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            if (StringUtils.isBlank(param))
            {
                errorList.add(String.format(USAGE_FAULT_MSG, ERROR_MESSAGE_EMPTY));
            }
            else
            {
                for (String date : param.trim().split(" +"))
                {
                    //TODO once the siap2 specs are finalised in regards to date formats this code can 
                    //either be reinstated or deleted
                    //if (!date.matches(NUMBER) && !date.matches(DATE_TIME))
                    if (!date.matches(NUMBER))
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG, ERROR_MESSAGE_INVALID));
                    }

                }
                if (errorList.isEmpty())
                {
                    String[] dates = param.trim().split(" +");
                    if (dates.length > 2)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG, ERROR_MESSAGE_TOO_MANY_DATES));
                    }
                    else if (dates.length == 2 && Double.parseDouble(dates[1]) < Double.parseDouble(dates[0]))
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG, ERROR_MESSAGE_DATE_ORDER));
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
            String[] dates = splitDates(criterion);

            String template;

            if (StringUtils.isNotBlank(dates[0]) && StringUtils.isNotBlank(dates[1]))
            {
                // has both dates
                template = "%1$s >= " + dates[0] + " AND %2$s <= " + dates[1];
            }
            else
            {
                // only contains a single value
                template = "%1$s <= " + dates[0] + " AND %2$s >= " + dates[0];
            }

            Utils.appendFragment(fieldSelect, String.format(template, minColName, maxColName));
        }

        return fieldSelect.toString();
    }

    /**
     * 
     * @return the date(s) in MJD format as a string array
     */
    private String[] splitDates(String criterion)
    {
        String[] dates = new String[2];

        if (!criterion.trim().contains(" "))
        {
            // only contains min value
            dates[0] = criterion.trim();
        }
        else
        {
            // both a start and end value
            dates = criterion.trim().split(" +");
        }
        //dates = completeDates(dates);
        //return convertDates(dates);
        return dates;
    }

  //TODO once the siap2 specs are finilised in regards to date formats this code can 
    //either be reinstated or deleted
    /**
     * Completes the hh:mm:ss of the date if the date given is incomplete. taking it to 00:00:00 if starting date and
     * 23:59:59 for an ending date
     * 
     * @param dates
     * @return
     
    private String[] completeDates(String[] dates)
    {
        for (int i = 0; i < 2; i++)
        {
            // conversion only done if date not empty and not already in MJD format
            if (StringUtils.isNotBlank(dates[i]) && !dates[i].matches(NUMBER))
            {
                if (dates[i].matches(DATE_ONLY))
                {
                    dates[i] = dates[i].concat(DATE_ENDINGS[i][0]);
                }
                else if (!dates[i].contains("."))
                {
                    dates[i] = dates[i].concat(DATE_ENDINGS[i][1]);
                }
                else
                {
                    dates[i] = dates[i].concat("Z");
                }
            }
        }

        return dates;
    }
     */
    
  //TODO once the siap2 specs are finilised in regards to date formats this code can 
    //either be reinstated or deleted
    /**
     * 
     * @param dates
     *            the dates to be converted
     * @return the dates in MJD format
    
    private String[] convertDates(String[] dates)
    {
        for (int i = 0; i < 2; i++)
        {
            // skips dates already in double format (assuming they are already in MJD format)
            if (StringUtils.isNotBlank(dates[i]) && !dates[i].matches(NUMBER))
            {
                dates[i] = String.valueOf(ObservationTime.dateTimeToModifiedJulianDate(DateTime.parse(dates[i])));
            }
        }
        return dates;
    }
     */

}
