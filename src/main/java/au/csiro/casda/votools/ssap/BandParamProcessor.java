package au.csiro.casda.votools.ssap;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

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
 * Specialisation of the NumericParamProcessor for handling the BAND parameter.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class BandParamProcessor extends NumericParamProcessor
{

    private static final int MAX_LIST_LENGTH = 100;
    private static List<String> allowedFrames = Arrays.asList(new String[] { "SOURCE", "OBSERVER" });

    /**
     * Create a new  BandParamProcessor instance.
     */
    public BandParamProcessor()
    {
        super(MAX_LIST_LENGTH, true, allowedFrames, "spectral rest frame");
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
            List<double[]> rangeListValues = getRangeListValues(param);
            for (double[] rangeVals : rangeListValues)
            {
                for (double num : rangeVals)
                {
                    if (!Double.isNaN(num) && num <= 0)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid wavelength in " + paramName.toUpperCase() + " value " + param));
                    }
                }
            }
        }
        
        return errorList;
    }
}
