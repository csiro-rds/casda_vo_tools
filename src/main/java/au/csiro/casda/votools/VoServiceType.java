package au.csiro.casda.votools;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */


/**
 * Enum of the types of VO Service that the votools project will support
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public enum VoServiceType
{
    /**
     * TAP - Table Access Protocol service 
     */
    tap,
    /**
     * SCS - Simple Cone Search service
     */
    scs,
    /**
     * Data-link - the datalink service
     */
    datalink,
    /**
     * SIAP v2 - Simple Image Access Protocol v2.
     */
    sia2,
    
    /** Simple Spectral Access - search for spectra.*/
    ssa,
    
    /** Access data - downloads and cutouts of image data and other files.*/
    data
}
