package au.csiro.casda.votools.config;

/**
 * Enum describing the types of resources available. Used for configuration/datalink
 * <p>
 * Copyright 2024, CSIRO Australia. All rights reserved.
 */

/**
 * Supported datalink resources
 * <p>
 * Copyright 2024, CSIRO Australia. All rights reserved.
 */
public enum DataLinkResourceType
{
    /** IMAGE_CUBE Resource */
    IMAGE_CUBE,
    
    /** CATALOGUE Resource */
    CATALOGUE,
    
    /** SPECTRUM Resource */
    SPECTRUM,
    
    /** MOMENT_MAP Resource */
    MOMENT_MAP,
    
    /** CUBELET Resource */
    CUBELET,
    
    /** EVALUATION Resource */
    EVALUATION,
    
    /** FITS Resource */
    FITS,
    
    /** VISIBILITY Resource */
    VISIBILITY,
    
    /** SCAN Resource */
    SCAN
}
