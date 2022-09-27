package au.csiro.casda.votools.siap1;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Processor for the SIZE param. This customises the NumericParamProcessor to the requirements of the SIZE param which
 * can take just a single number.
 * <p>
 * Copyright 2021, CSIRO Australia. All rights reserved.
 */
public class SizeParamProcessor extends NumericParamProcessor
{

    private static final double DEFAULT_MAX_SIZE_DEGREES = 2;

    private static final double DEFAULT_SIZE_DEGREES = 0.2d; // 12 arcmin (12/60)

    private double maxSizeDegrees = DEFAULT_MAX_SIZE_DEGREES;

    private double defaultSize = DEFAULT_SIZE_DEGREES;

    /**
     * Create a new BandParamProcessor instance.
     */
    public SizeParamProcessor()
    {
        super(2, false);
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = super.validate(paramName, values);
        if (CollectionUtils.isNotEmpty(errorList))
        {
            return errorList;
        }

        if (values.length > 1)
        {
            errorList.add(String.format(USAGE_FAULT_MSG,
                    "Only one set of " + paramName.toUpperCase() + " values may be provided."));
        }

        int numValues = 0;
        for (String param : values)
        {
            if (StringUtils.isNotEmpty(param))
            {
                String[] parts = param.split(",");
                for (String part : parts)
                {
                    double sizeVal = Double.valueOf(part);
                    numValues++;

                    if (sizeVal < 0 || sizeVal > maxSizeDegrees)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG, "Value must be between 0 and " + maxSizeDegrees
                                + " in " + paramName.toUpperCase() + " value " + param));
                    }
                }
            }

            if (numValues < 1 || numValues > 2)
            {
                errorList.add(String.format(USAGE_FAULT_MSG, "Either one or two numbers must be provided in "
                        + paramName.toUpperCase() + " value " + param));
            }
        }
        return errorList;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The query needs to be composed of POS and SIZE, thus we don't attempt to provide a query here
        return "";
    }

    /**
     * Convert the user supplied values into a width (RA) and height (dec) pair in decimal degrees. If either value had
     * a 0 supplied then it will be replaced by the default size for the service.
     * 
     * @param values
     *            The user supplied values.
     * @return A two element array containing the width and height of the requested cutout in decimal degrees.
     */
    public double[] getSizeDegrees(String[] values)
    {
        double[] size2D = new double[2];
        String[] parts = values[0].split(",");
        int idx = 0;
        for (String part : parts)
        {
            double sizeVal = Double.valueOf(part);
            if (sizeVal == 0.0d)
            {
                sizeVal = defaultSize;
            }
            size2D[idx++]= sizeVal;
        }
        if (parts.length == 1)
        {
            size2D[1] = size2D[0];
        }
        return size2D;
    }

    /**
     * Find the search radius sufficient to enclose the requested cutout.
     * 
     * @param values
     *            The user supplied values.
     * @return The search radius in decimal degrees.
     */
    public double getSearchRadius(String[] values)
    {
        double[] sizeDegrees = getSizeDegrees(values);
        double largestAxisSize = Math.max(sizeDegrees[0], sizeDegrees[1]);
        return largestAxisSize / 2.0d;
    }

    /**
     * @return The maximum search radius in degrees.
     */
    public double getMaxSizeDegrees()
    {
        return maxSizeDegrees;
    }

    public void setMaxSizeDegrees(double maxSizeDegrees)
    {
        this.maxSizeDegrees = maxSizeDegrees;
    }

    /**
     * @return The default search radius in degrees.
     */
    public double getDefaultSizeDegrees()
    {
        return defaultSize;
    }
}
