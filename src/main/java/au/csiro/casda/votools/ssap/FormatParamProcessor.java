package au.csiro.casda.votools.ssap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.csiro.casda.votools.utils.Utils;

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
 * Processor for the FORMAT param. Only FITS format (and aliases) are supported currently. 
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class FormatParamProcessor implements SsapParamProcessor
{

    private final static List<String> FITS_FORMATS =
            Arrays.asList(new String[] { "all", "compliant", "native", "fits", "application/fits", "image/fits" });
    private final static List<String> GRAPHICS_FORMATS = Arrays.asList(new String[] { "all", "compliant", "graphic",
            "image/jpg", "image/jpeg", "image/png", "image/gif", "image/tiff" });
    private final static List<String> VOTABLE_FORMATS = Arrays.asList(new String[] { "all", "compliant", "votable",
            "application/xml", "application/x-votable+xml", "text/xml", "xml" });
    private final static List<String> METADATA_FORMATS = Arrays.asList(new String[] { "metadata" });
    
    @Override
    public List<String> validate(String paramName, String[] values)
    {
        Set<String> allowedFormats = new HashSet<>(FITS_FORMATS);
        allowedFormats.addAll(GRAPHICS_FORMATS);
        allowedFormats.addAll(VOTABLE_FORMATS);
        allowedFormats.addAll(METADATA_FORMATS);
        
        List<String> errorList = new ArrayList<String>();
        
        for (String param : values)
        {
            for (String formatString : param.split(","))
            {
                if (!allowedFormats.contains(formatString.toLowerCase()))
                {
                    errorList.add(String.format(SsapParamProcessor.USAGE_FAULT_MSG,
                            "FORMAT " + formatString + " is not supported"));

                }
            }
        }

        return errorList;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        boolean hasFits = false; 
    
        for (String param : criteria)
        {
            for (String formatString : param.split(","))
            {
                hasFits |= FITS_FORMATS.contains(formatString.toLowerCase());
            }
        }

        StringBuilder fieldSelect = new StringBuilder();
        if (!hasFits)
        {
            // We only support fits, so ensure an empty result is returned is any other format is required
            Utils.appendFragment(fieldSelect, "1=0");
        }

        return fieldSelect.toString();
    }

}
