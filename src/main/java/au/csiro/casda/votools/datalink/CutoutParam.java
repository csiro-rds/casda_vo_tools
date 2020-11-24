package au.csiro.casda.votools.datalink;

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
 * The supported Cutout parameters.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public enum CutoutParam
{
    /** The ID parameter is a string-valued parameter that specifies the identifier of dataset(s).  */
    ID,
    
    /** The POS parameter defines the positional region(s) to be retrieved for data. */
    POS, 
    
    /** The BAND parameter defines the energy interval(s) to be retrieved for data. */
    BAND,
    
    /** The channel parameter defines the range of pixels to be retrieved.  E.g. an image cutout.*/
    CHANNEL,
    
    /** The POL parameter defines the polarization state(s) to be retrieved for matching data.  */
    POL,
    
    /** The COORD parameter includes the axis name and a numeric value or range to be retrieved for matching data.*/
    COORD;
}
