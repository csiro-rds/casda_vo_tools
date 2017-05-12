package au.csiro.casda.votools.ssap;

import java.util.Collections;
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
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class SizeParamProcessor extends NumericParamProcessor
{

    private static final double MAX_SIZE_DEGREES = 15;

    /**
     * Create a new  BandParamProcessor instance.
     */
    public SizeParamProcessor()
    {
        super(1, false, Collections.emptyList(), "qualifier");
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = super.validate(paramName, values);
        if (CollectionUtils.isNotEmpty(errorList))
        {
            return errorList;
        }

        for (String param : values)
        {
            if (StringUtils.isEmpty(param))
            {
                continue;
            }
            String[] sizeQual = param.trim().split(";");

            double size_val = Double.valueOf(sizeQual[0]);

            if (size_val <= 0 || size_val > MAX_SIZE_DEGREES)
            {
                errorList.add(String.format(USAGE_FAULT_MSG,
                        "Value must be between 0 and 15 in " + paramName.toUpperCase() + " value " + param));
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
     * @return The maximum search radius in degrees.
     */
    public static double getMaxSizeDegrees()
    {
        return MAX_SIZE_DEGREES;
    }
}
