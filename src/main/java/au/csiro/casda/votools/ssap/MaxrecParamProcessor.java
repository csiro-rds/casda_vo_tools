package au.csiro.casda.votools.ssap;

import java.util.ArrayList;
import java.util.List;

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
 * Processor for the MAXREC param. Only FITS format (and aliases) are supported currently. 
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class MaxrecParamProcessor implements SsapParamProcessor
{

    
    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<>();
        
        if (values.length > 1 || values[0].trim().split(" ").length > 1)
        {
            errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                    "Query can only contain a single MAXREC value"));
        }
        else if (!StringUtils.isNumeric(values[0].trim()))
        {
            errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                    "The maximum amount of records is invalid. MAXREC must be a valid whole number"));
        }

        return errorList;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The version param has no impact on the query
        return "";
    }

}
