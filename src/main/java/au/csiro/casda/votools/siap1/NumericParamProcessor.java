package au.csiro.casda.votools.siap1;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import au.csiro.casda.votools.utils.Utils;

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
 * A processor for numeric SIAP parameter types. These may be a single value, as defined in section
 * 4.1 of the IVOA Simple Image Access Version 1.0 Recommendation. Multiple sets of values are also supported.
 * <p>
 * Copyright 2022, CSIRO Australia All rights reserved.
 */
public class NumericParamProcessor implements Siap1ParamProcessor
{
    private static final String SINGLE_NUMERIC_VALUE_PATTERN = "[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?";

    private static final String SIMPLE_LIST_PATTERN =
            SINGLE_NUMERIC_VALUE_PATTERN + "(," + SINGLE_NUMERIC_VALUE_PATTERN + ")*";

    private int maxListlen;

    private boolean allowRange;

    /**
     * Create a new NumericParamProcessor instance.
     * 
     * @param maxListlen
     *            The maximum number of list entries that will be accepted.
     * @param allowRange
     *            Should ranges separated by / be allowed
     */
    public NumericParamProcessor(int maxListlen, boolean allowRange)
    {
        this.maxListlen = maxListlen;
        this.allowRange = allowRange;
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            String testParam = StringUtils.trimToEmpty(param);
            // May be either empty, a single value or range or a list of values or ranges
            // e.g. 
            if (testParam.length() != 0 && !Pattern.matches(SIMPLE_LIST_PATTERN, testParam))
            {
                errorList.add(
                        String.format(USAGE_FAULT_MSG, "Invalid " + paramName.toUpperCase() + " value " + testParam));
            }
            else
            {
                // check min < max
                String[] list = param.trim().split(",+");
                for (String element : list)
                {
                    String[] range = element.split("/");
                    if (range.length > 1)
                    {
                        if (!allowRange)
                        {
                            errorList.add(String.format(USAGE_FAULT_MSG,
                                    "Ranges are not allowed in " + paramName.toUpperCase() + " value " + testParam));
                        }
                        else
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
                
                // Check list length
                if (list.length > maxListlen)
                {
                    String entries = maxListlen > 1 ? "entries" : "entry";
                    errorList.add(String.format(USAGE_FAULT_MSG, "Only " + maxListlen + " " + entries + " allowed in "
                            + paramName.toUpperCase() + " value " + testParam));
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
        return Double.valueOf(value);
    }

    /**
     * Retrieve the list of range values from a parameter 
     * @param param The parameter to be converted to numerics. Must have already passed validation.
     * @return A list of the value ranges (or single values if ranges aren't allowed).
     */
    protected List<double[]> getRangeListValues(String param)
    {
        String[] list = param.trim().split(";")[0].split(",+");
        
        List<double[]> rangeList = new ArrayList<>();
        for (String criterion : list)
        {
            if (StringUtils.isBlank(criterion))
            {
                continue;
            }
            
            String[] values = criterion.split("/");
            double[] numericVals = new double[values.length];
            for (int i = 0; i < values.length; i++)
            {
                numericVals[i] = paramValue(values[i]);
            }
            rangeList.add(numericVals);
        }
        
        return rangeList;
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
        StringBuilder fieldSelect = new StringBuilder();
        for (String param : criteria)
        {
            String[] list = param.trim().split(",+");
            for (String rawCriterion : list)
            {
                if (StringUtils.isBlank(rawCriterion))
                {
                    continue;
                }

                // Qualifiers are handled in specific params
                String criterion = StringUtils.trimToEmpty(rawCriterion).split(";")[0];
                String template;
                String value = "";
                if (criterion.matches(SINGLE_NUMERIC_VALUE_PATTERN))
                {
                    // Match any row which overlaps the specified value
                    value = criterion;
                    template = "%1$s <= %3$s AND %2$s >= %3$s";
                }
                else if (criterion.equals("/"))
                {
                    // Match any record which has a min and a max value
                    template = "%s IS NOT NULL AND %s IS NOT NULL";
                }
                else if (criterion.endsWith("/"))
                {
                    // Match any row which has a max value more than the specified value
                    value = criterion.substring(0, criterion.length() - 1).trim();
                    template = "%2$s >= %3$s";
                }
                else if (criterion.startsWith("/"))
                {
                    // Match any row which has a min value less than the specified value
                    value = criterion.substring(1).trim();
                    template = "%1$s <= %3$s";
                }
                else
                {
                    // Match any row which overlaps the specified range
                    String[] range = criterion.split("/");
                    template = "%1$s <= " + range[1] + " AND %2$s >= " + range[0];
                }
    
                Utils.appendFragment(fieldSelect, String.format(template, minColName, maxColName, value));
            }
        }

        return fieldSelect.toString();
    }

}
