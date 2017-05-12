package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
 * A processor for numeric SIAP parameter types. These may be either a single value or a range, as defined in section
 * 2.1 of the IVOA Simple Image Access Version 2.0 Recommendation. Multiple sets of values are also supported.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class NumericParamProcessor implements SiapParamProcessor
{
    private static final String NEGATIVE_INFINTY = "-Inf";
    private static final String POSITIVE_INFINTY = "+Inf";

    private static final String SINGLE_NUMERIC_VALUE_PATTERN = "\\d+(\\.\\d+)?([eE][+-]?\\d+)?";

    private static final String OPTIONAL_MIN_NUMERIC_PATTERN =
            "(" + NEGATIVE_INFINTY + ")|(" + SINGLE_NUMERIC_VALUE_PATTERN + ")";

    private static final String OPTIONAL_MAX_NUMERIC_PATTERN =
            "(\\" + POSITIVE_INFINTY + ")|(" + SINGLE_NUMERIC_VALUE_PATTERN + ")";

    private static final String NUMERIC_RANGE_PATTERN =
            "(" + OPTIONAL_MIN_NUMERIC_PATTERN + ") +(" + OPTIONAL_MAX_NUMERIC_PATTERN + ")";

    @Override
    public Siap2ParamType getSupportedParamType()
    {
        return Siap2ParamType.NUMERIC;
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            String testParam = StringUtils.trimToEmpty(param);
            // May be either empty, a single value e.g. 3e5 or a range e.g. NaN 1.76e-5
            if (testParam.length() != 0 && !Pattern.matches(SINGLE_NUMERIC_VALUE_PATTERN, testParam)
                    && !Pattern.matches(NUMERIC_RANGE_PATTERN, testParam))
            {
                errorList.add(
                        String.format(USAGE_FAULT_MSG, "Invalid " + paramName.toUpperCase() + " value " + testParam));
            }
            else
            {
                // check min < max
                String[] range = param.trim().split(" +");
                if (range.length == 2)
                {
                    Double val1 = paramValue(range[0]);
                    Double val2 = paramValue(range[1]);
                    if (!val1.equals(Double.NaN) && !val2.equals(Double.NaN) && val1 > val2)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid " + paramName.toUpperCase() + " value " + testParam));
                    }
                }
            }
        }
        return errorList;
    }

    private double paramValue(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return Double.NaN;
        }
        if (NEGATIVE_INFINTY.equals(value) || POSITIVE_INFINTY.equals(value))
        {
            return Double.NaN;
        }
        return Double.valueOf(value);
    }

    /**
     * Convert a set of numeric field values into a select clause for the field pair and add it to the ADQL query being
     * built.
     * 
     * @param minColName
     *            The name of the column holding the minimum value.
     * @param maxColName
     *            The name of the column holding the maximum value.
     * @param criteria
     *            The set of parameters that have been supplied for the field.
     * @return The current AdqlQueryBuilder instance
     */
    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        final int infPlusSpaceLength = 5;
        StringBuilder fieldSelect = new StringBuilder();
        for (String criterion : criteria)
        {
            if (StringUtils.isBlank(criterion))
            {
                continue;
            }
            criterion = StringUtils.trimToEmpty(criterion);
            String template;
            String value = "";
            if (criterion.matches(NEGATIVE_INFINTY + " +\\" + POSITIVE_INFINTY))
            {
                // Match any record which has a min and a max value
                template = "%s IS NOT NULL AND %s IS NOT NULL";
            }
            else if (criterion.matches(SINGLE_NUMERIC_VALUE_PATTERN))
            {
                // Match any row which overlaps the specified value
                value = criterion;
                template = "%1$s <= %3$s AND %2$s >= %3$s";
            }
            else if (criterion.endsWith(" " + POSITIVE_INFINTY))
            {
                // Match any row which has a max value more than the specified value
                value = criterion.substring(0, criterion.length() - infPlusSpaceLength).trim();
                template = "%2$s >= %3$s";
            }
            else if (criterion.startsWith(NEGATIVE_INFINTY + " "))
            {
                // Match any row which has a min value less than the specified value
                value = criterion.substring(infPlusSpaceLength).trim();
                template = "%1$s <= %3$s";
            }
            else
            {
                // Match any row which overlaps the specified range
                String[] range = criterion.split(" +");
                template = "%1$s <= " + range[1] + " AND %2$s >= " + range[0];
            }

            Utils.appendFragment(fieldSelect, String.format(template, minColName, maxColName, value));
        }

        return fieldSelect.toString();
    }

}
