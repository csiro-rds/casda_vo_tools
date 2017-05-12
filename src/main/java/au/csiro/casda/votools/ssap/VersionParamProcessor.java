package au.csiro.casda.votools.ssap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
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
 * Processor for the VERSION param. This is a basic processor which only validates the VERSION value.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class VersionParamProcessor implements SsapParamProcessor
{

    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(new String[] {"1.1", "1.0"});

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();
        if (ArrayUtils.isNotEmpty(values))
        {
            if (values.length > 1)
            {
                errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                        "Only a single VERSION value may be specified"));
            }
            else
            {
                String errorMsg = isVersionSupported(values[0]);
                if (StringUtils.isNotEmpty(errorMsg))
                {
                    errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG, errorMsg));
                }
            }
        }

        return errorList;
    }

    private String isVersionSupported(String requestedVersion)
    {
        if (!SUPPORTED_VERSIONS.contains(requestedVersion))
        {
            return "Version mismatch error";
        }

        return null;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The version param has no impact on the query
        return "";
    }

}
