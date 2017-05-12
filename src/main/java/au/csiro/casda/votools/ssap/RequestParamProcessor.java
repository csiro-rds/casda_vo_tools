package au.csiro.casda.votools.ssap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

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
 * Processor for the REQUEST param. This is a basic processor which only validates the REQUEST value.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class RequestParamProcessor implements SsapParamProcessor
{

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();
        if (ArrayUtils.isEmpty(values))
        {
            errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                    "Parameter REQUEST is required"));
        }
        else if (values.length > 1)
        {
            errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                    "Only a single REQUEST value may be specified"));
        }
        else if (!"querydata".equals(values[0].toLowerCase()))
        {
            errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                    "REQUEST value must be queryData"));
        }

        return errorList;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The request param has no impact on the query
        return "";
    }

}
