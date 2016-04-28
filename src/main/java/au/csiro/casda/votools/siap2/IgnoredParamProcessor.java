package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;

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
 * A processor for ignored SIAP parameter types. The content of these parameters is ignored and not validated.
 * <p>
 * Copyright 2016, CSIRO Australia All rights reserved. All rights reserved.
 */
public class IgnoredParamProcessor implements SiapParamProcessor
{

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
        return errorList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildQuery(String colName, String unusedForText, String[] criteria)
    {
        return "";
    }

}
