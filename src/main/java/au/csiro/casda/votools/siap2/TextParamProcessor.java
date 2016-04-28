package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;
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
 * A processor for text SIAP parameter types. These are a single value, as defined in section 2.1 of the IVOA Simple
 * Image Access Version 2.0 Recommendation.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved. All rights reserved.
 */
public class TextParamProcessor implements SiapParamProcessor
{
    private static final String DPTYPE = "DPTYPE";

    /**
     * {@inheritDoc}
     */
    @Override
    public Siap2ParamType getSupportedParamType()
    {
        return Siap2ParamType.TEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();

        if (paramName.toUpperCase().equals(DPTYPE) && values != null)
        {
            for (String value : values)
            {
                if (!DataProductType.contains(value.toLowerCase()))
                {
                    String message = "The value '" + value
                            + "' is not valid for the DPTYPE. The value must be one of the following: ";
                    message += DataProductType.createList(false);
                    errorList.add(String.format(USAGE_FAULT_MSG, message));
                }
            }
        }

        return errorList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(String colName, String unusedForText, String[] criteria)
    {
        StringBuilder fieldSelect = new StringBuilder();

        for (String criterion : criteria)
        {
            criterion = StringUtils.trimToEmpty(criterion);

            // Match any row which contains the matching value
            String template = "lower(%1$s) = '" + criterion.toLowerCase().replaceAll("'", "''''") + "'";

            Utils.appendFragment(fieldSelect, String.format(template, colName));
        }

        return fieldSelect.toString();
    }

}
