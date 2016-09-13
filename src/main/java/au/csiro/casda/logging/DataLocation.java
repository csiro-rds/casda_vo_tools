package au.csiro.casda.logging;

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
 * Locations where CASDA data resides in various stages.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public enum DataLocation
{
    /**
     * The archive (NGAS + HSM)
     */
    ARCHIVE,

    /**
     * Next Generation Archive System
     */
    NGAS,

    /**
     * FS3/Scratch/Cache where files are ready for download through web.
     */
    DATA_ACCESS,

    /**
     * Real Time Computer
     */
    RTC,

    /**
     * CASDA Database
     */
    CASDA_DB,

    /**
     * Virtual Observatory Tools
     */
    VO_TOOLS;
}
