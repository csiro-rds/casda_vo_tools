package au.csiro.casda.votools.siap1;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
 * Processor for the POS param. This customises the NumericParamProcessor to the requirements of the POS param which can 
 * take just a single pair of numbers. These numbers are assumed to be longitude (RA) and latitude (Dec) in J2000. 
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class PosParamProcessor extends NumericParamProcessor
{

    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LATITUDE = 90;
    private static final int MAX_LONGITUDE = 360;

    /**
     * Create a new PosParamProcessor instance.
     */
    public PosParamProcessor()
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
        
        for (String param : values)
        {
            if (StringUtils.isEmpty(param))
            {
                continue;
            }
            
            String[] list = param.trim().split(",+");
            if (list.length != 2)
            {
                errorList.add(String.format(USAGE_FAULT_MSG, "Must have exactly two coordinate values in "
                        + paramName.toUpperCase() + " value " + param));
            }
            else
            {
                // Check bounds of values
                double longitude = Double.valueOf(list[0]);
                double latitude = Double.valueOf(list[1]);
                if (longitude < 0 || longitude > MAX_LONGITUDE)
                {
                    errorList.add(String.format(USAGE_FAULT_MSG,
                            "Invalid right ascension in " + paramName.toUpperCase() + " value " + param));
                }
                if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE)
                {
                    errorList.add(String.format(USAGE_FAULT_MSG,
                            "Invalid declination in " + paramName.toUpperCase() + " value " + param));
                }
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
     * Extract the J2000 equatorial coordinates (right ascension and declination) from the provided criterion. The
     * criterion must have already passed validation. 
     * 
     * @param criterion The POS criteria
     * @return The right ascension and declination.
     */
    public double[] getRaDec(String criterion)
    {
        String[] coords = criterion.trim().split(",");
        double ra = Double.parseDouble(coords[0]);
        double dec = Double.parseDouble(coords[1]);
        
        return new double[] {ra, dec};
    }

}
