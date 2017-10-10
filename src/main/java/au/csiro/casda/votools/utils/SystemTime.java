package au.csiro.casda.votools.utils;

import org.joda.time.DateTime;

/**
 * Signature for getting current system DateTime.
 * 
 * Implemented primarily for ease of unit testing.
 * 
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public interface SystemTime
{
    /**
     * Get Current UTC System DateTime
     * 
     * @return DateTime
     */
    DateTime getCurrentUTCDateTime();
}
