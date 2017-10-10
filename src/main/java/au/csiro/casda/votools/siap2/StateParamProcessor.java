package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
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
 * A processor for 'STATE' SIAP parameter types.
 * 
 * Symbols from the {I Q U V RR LL RL LR XX YY XY YX POLI POLA} set.
 * 
 * eg: POL=I&amp;POL=Q&amp;POL=YY
 * 
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class StateParamProcessor implements SiapParamProcessor
{
    @Override
    public Siap2ParamType getSupportedParamType()
    {
        return Siap2ParamType.STATE;
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            String testParam = StringUtils.trimToEmpty(param).toUpperCase();
            if (testParam.length() != 0 && !EnumUtils.isValidEnum(PolarizationStateType.class, testParam))
            {
                errorList.add(String.format(USAGE_FAULT_MSG, "Invalid " + paramName.toUpperCase() + " value "  + param));
            }
        }
        return errorList;
    }

    @Override
    public String buildQuery(String firstColName, String secondColName, String[] criteria)
    {
        StringBuilder fieldSelect = new StringBuilder();
        for (String criterion : criteria)
        {
            String state = StringUtils.trimToEmpty(criterion).toUpperCase();
            if (state.length() != 0 && EnumUtils.isValidEnum(PolarizationStateType.class, state))
            {
                PolarizationStateType polState = EnumUtils.getEnum(PolarizationStateType.class, state);
                String fragment = buildFragment(polState);

                Utils.appendFragment(fieldSelect, fragment);
            }
        }
        return fieldSelect.toString();
    }

    private String buildFragment(PolarizationStateType polState)
    {
        String template = "pol_states LIKE '%%/%s/%%'";

        if (polState == null)
        {
            return "";
        }
        return String.format(template, polState.name());
    }
}
