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
 * The type of Polarization State.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public enum PolarizationStateType
{
    /** Stokes parameters : Total intensity*/
    I,
    /** Stokes parameters :  Linear polarization*/
    Q,
    /** Stokes parameters :  Linear polarization*/
    U,
    /** Stokes parameters : Circular polarization*/
    V,
    /** Circular feeds */
    RR,
    /** Circular feeds */
    LL,
    /** Circular feeds */
    RL,
    /** Circular feeds */
    LR,
    /** Linear feeds */
    XX,
    /** Linear feeds */
    YY,
    /** Linear feeds */
    XY,
    /** Linear feeds */
    YX,
    /** Linearly polarized intensity */
    POLI,
    /** Linear polarization angle */
    POLA
}
