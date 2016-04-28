package au.csiro.casda.votools.siap2;

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
 * The type of SIA parameter. This defines the formats accepted and how the parameter is processed.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public enum Siap2ParamType
{
    /**
     * A numeric parameter that takes decimals, scientific notation and ranges.
     */
    NUMERIC(new NumericParamProcessor()),

    /**
     * The POS parameter specifying positional region(s) to be searched.
     */
    POSITION(new PositionParamProcessor()),
    
    /**
     * The DATE parameter specifying the time frame to be searched.
     */
    DATE(new DateParamProcessor()),
    
    /**
     * The STATE parameter specifying the state type to be searched.
     */
    STATE(new StateParamProcessor()),
    
    /**
     * A text parameter that takes plain text.
     */
    TEXT(new TextParamProcessor()),
    
    /**
     * A parameter to be ignored.
     */
    IGNORED(new IgnoredParamProcessor());

    
    private final SiapParamProcessor processor;

    /**
     * Create a new enum instance.
     * @param processor The processing class for this param type.
     */
    private Siap2ParamType(SiapParamProcessor processor)
    {
        this.processor = processor;

    }

    public SiapParamProcessor getProcessor()
    {
        return processor;
    }

}
