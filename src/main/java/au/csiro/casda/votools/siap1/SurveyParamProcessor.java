package au.csiro.casda.votools.siap1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Processor for the SURVEY param. 
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class SurveyParamProcessor implements Siap1ParamProcessor
{

    private Set<String> surveyCodeSet;

    @Override
    public List<String> validate(String paramName, String[] values)
    {

        List<String> errorList = new ArrayList<String>();

        if (values.length > 1)
        {
            errorList.add(String.format(Siap1ParamProcessor.USAGE_FAULT_MSG,
                    "Query can only contain a single SURVEY value"));
        }

        for (String surveyCode : values)
        {
            if (!surveyCodeSet.contains(surveyCode.toLowerCase()))
            {
                errorList.add(String.format(Siap1ParamProcessor.USAGE_FAULT_MSG,
                        "SURVEY " + surveyCode + " is not supported"));

            }
        }

        return errorList;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The survey specific where clause is handled in the Siap1Service
        return "";
    }

    public void setSurveys(List<String> surveyCodes)
    {
        surveyCodeSet = surveyCodes.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
