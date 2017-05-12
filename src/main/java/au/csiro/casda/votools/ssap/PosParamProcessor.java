package au.csiro.casda.votools.ssap;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Processor for the POS param. This customises the NumericParamProcessor to the requirements of the POS param which can 
 * take just a single pair of numbers and an optional qualifier indicating a supported reference frame. 
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class PosParamProcessor extends NumericParamProcessor
{

    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LATITUDE = 90;
    private static final int MIN_GAL_LONGITUDE = -180;
    private static final int MAX_LONGITUDE = 360;
    
    // We support the J2000 equatorial and galactic reference frames 
    private static List<String> allowedFrames =
            Arrays.asList(new String[] { "ICRS", "FK5", "GALACTIC", "GALACTIC-II" });

    /**
     * Create a new PosParamProcessor instance.
     */
    public PosParamProcessor()
    {
        super(2, false, allowedFrames, "coordinate system reference frame");
    }
    
    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = super.validate(paramName, values);
        if (CollectionUtils.isNotEmpty(errorList))
        {
            return errorList;
        }
        
        for (String param : values)
        {
            if (StringUtils.isEmpty(param))
            {
                continue;
            }
            String refFrame = "";
            String[] posQual = param.trim().split(";");
            if (posQual.length == 2)
            {
                refFrame = posQual[1].toUpperCase();
            }
            
            String[] list = posQual[0].split(",+");
            if (list.length != 2)
            {
                errorList.add(String.format(USAGE_FAULT_MSG, "Must have exactly two coordinate values in "
                        + paramName.toUpperCase() + " value " + param));
            }
            else
            {
                // Check bounds of values
                double longitude = Double.valueOf(list[0]);
                double latitude = Double.valueOf(list[1]);
                if (refFrame.startsWith("GALACTIC"))
                {
                    if (longitude < MIN_GAL_LONGITUDE || longitude > MAX_LONGITUDE)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid longitude in " + paramName.toUpperCase() + " value " + param));
                    }
                    if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid latitude in " + paramName.toUpperCase() + " value " + param));
                    }
                }
                else
                {
                    if (longitude < 0 || longitude > MAX_LONGITUDE)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid right ascension in " + paramName.toUpperCase() + " value " + param));
                    }
                    if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE)
                    {
                        errorList.add(String.format(USAGE_FAULT_MSG,
                                "Invalid declination in " + paramName.toUpperCase() + " value " + param));
                    }
                }
            }
        }

        return errorList;
    }

    
    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        // The query needs to be composed of POS and SIZE, thus we don't attempt to provide a query here 
        return "";
    }

    /**
     * Extract the J2000 equatorial coordinates (right ascension and declination) from the provided criterion. The
     * criterion must have already passed validation. If the criterion is a set of galactic coordinates these will be
     * converted to equatorial.
     * 
     * @param criterion The POS criteria
     * @return The right ascension and declination.
     */
    public double[] getRaDec(String criterion)
    {
        String refFrame = "";
        String[] posQual = criterion.trim().split(";");
        if (posQual.length > 1)
        {
            refFrame = posQual[1];
        }
        
        String[] coords = posQual[0].split(",");
        double ra = Double.parseDouble(coords[0]);
        double dec = Double.parseDouble(coords[1]);
        
        // Deal with Galactic transform, others are assumed to be J2000 equatorial already (e.g. FK5, ICRS)
        if (refFrame.startsWith("GALACTIC"))
        {
            return convertGalacticToEquatorial(ra, dec);
        }
        return new double[] {ra, dec};
    }

    /**
     * Convert a galactic coordinate to the equatorial systems. Both values must be in the J2000 epoch. This routine 
     * is only accurate to half an arcsecond, however that should be sufficient for its use in POS queries.
     * @param galLong The galactic longitude to convert.
     * @param galLat The galactic latitude (in decimal degrees) to convert.
     * @return The right ascension and declination of the location. 
     */
    private double[] convertGalacticToEquatorial(double galLong, double galLat)
    {
        double gLongRad = Math.toRadians(galLong);
        double gLatRad = Math.toRadians(galLat);
        
        // Conversion coordinates sourced from 
        // https://github.com/astropy/astropy/blob/v1.2.x/astropy/coordinates/builtin_frames/galactic.py
        // J2000 Equatorial coordinates of the North Galactic Pole
        final double ngpRaDeg = 192.8594812065348;
        final double ngpDecRad = Math.toRadians(27.12825118085622);
        // Galactic longitude of the north celestial pole
        final double galPlaneLatRad = Math.toRadians(32.9319185680026);
        
        double sinDec = Math.cos(gLatRad) * Math.cos(ngpDecRad) * Math.sin(gLongRad - galPlaneLatRad)
                + Math.sin(gLatRad) * Math.sin(ngpDecRad);
        double decDeg = Math.toDegrees(Math.asin(sinDec));
        
        double yRad = Math.cos(gLatRad)*Math.cos(gLongRad-galPlaneLatRad);
        double xRad = Math.sin(gLatRad) * Math.cos(ngpDecRad)
                - Math.cos(gLatRad) * Math.sin(ngpDecRad) * Math.sin(gLongRad - galPlaneLatRad);

        double raDeg = Math.toDegrees(Math.atan2(yRad,  xRad))+ngpRaDeg;
        // 
        final double degInCircle = 360;
        raDeg = raDeg - degInCircle*Math.floor(raDeg/degInCircle); 

        return new double[] {raDeg, decDeg};
    }
}
