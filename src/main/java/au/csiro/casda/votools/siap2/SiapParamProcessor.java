package au.csiro.casda.votools.siap2;

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
 * An interface defining a processor for a particular type of SIAP parameter.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public interface SiapParamProcessor
{
    
    /** Invalid input  */
    public static final String USAGE_FAULT_MSG = "UsageFault: %s";
    /** Service is not currently able to function */
    public static final String TRANSIENT_FAULT_MSG = "TransientFault: Service is not currently able to function";
    /** Service cannot perform requested action */
    public static final String FATAL_FAULT_MSG = "FatalFault: Service cannot perform requested action";
    /** General error (not covered above) */
    public static final String DEFAULT_FAULT_MSG = "DefaultFault: %s";
    /** Unknown ID value (IVOA DataLink Version 1.0)*/
    public static final String NOT_FOUND_FAULT_MSG = "NotFoundFault: %s";
    
    /**
     * @return The parameter type supported by this implementation.
     */
    Siap2ParamType getSupportedParamType();
    
    /**
     * Check the values of a parameter on a SIAP v2 request. Implementations will each deal with a specific type of
     * parameter.
     * 
     * @param paramName
     *            The name of the param for use in error messages
     * @param values
     *            The values to be validated.
     * @return The error messages, or an empty list if all values are valid.
     */
    List<String> validate(String paramName, String[] values);

    /**
     * Build an ADQL where clause fragment which will apply the restrictions of the param to the query.
     * 
     * @param firstColName The name of the first column supporting this param, e.g. the minimum value column
     * @param secondColName The name of the second column supporting this param, e.g. the maximum value column
     * @param criteria The criteria specified by the caller, to be applied to the param. e.g. "10 50", "60"
     * @return The where clause fragment.
     */
    String buildQuery(String firstColName, String secondColName, String[] criteria);

}
