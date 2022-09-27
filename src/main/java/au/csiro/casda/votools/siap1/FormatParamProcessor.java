package au.csiro.casda.votools.siap1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
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
 * Processor for the FORMAT param. Only FITS format (and aliases) are supported currently.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class FormatParamProcessor implements Siap1ParamProcessor
{

    private final static Set<String> FITS_FORMATS =
            new HashSet<>(Arrays.asList(new String[] { "all", "application/fits", "image/fits" }));
    private final static Set<String> GRAPHICS_FORMATS = new HashSet<>(Arrays
            .asList(new String[] { "graphic", "graphic-all", "image/png" }));
    private final static Set<String> METADATA_FORMATS =
            new HashSet<>(Arrays.asList(new String[] { "metadata" }));

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        // Allow all values - we just ignore any we don't understand
        return new ArrayList<String>();
    }

    /**
     * @return The set of allowed formats. All are expected to be case insensitive 
     */
    Set<String> getAllowedFormats()
    {
        Set<String> allowedFormats = new TreeSet<>(FITS_FORMATS);
        allowedFormats.addAll(GRAPHICS_FORMATS);
        allowedFormats.addAll(METADATA_FORMATS);
        return allowedFormats;
    }

    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        String formats = StringUtils.join(getSelectedFormats(criteria), "', '");
        return String.format("sia1_format.content_type IN ('%s')", formats);
    }

    /**
     * Produce an ordered list of formats to be produced for a query based on the user's request.
     * @param values The values supplied by the user.
     * @return The list of formats to be output.
     */
    public List<String> getSelectedFormats(String[] values)
    {
        Set<String> formatParams = new HashSet<>();
        if (values != null)
        {
            for (String param : values)
            {
                for (String formatString : param.split(","))
                {
                    formatParams.add(formatString.toLowerCase());
                }
            }
        }
        
        List<String> selectedFormats = new ArrayList<>();
        if (ArrayUtils.isEmpty(values))
        {
            selectedFormats.add("image/fits");
            selectedFormats.add("image/png");
        }
        else 
        {
            if (!SetUtils.intersection(formatParams, FITS_FORMATS).isEmpty())
            {
                selectedFormats.add("image/fits");
            }
            if (formatParams.contains("image/png") || formatParams.contains("graphic")
                    || formatParams.contains("graphic-all") || formatParams.contains("all"))
            {
                selectedFormats.add("image/png");
            }
        }
        
        return selectedFormats;
    }
    
    /**
     * Return the processing metadata pix flags (see SIA v1 section 4.2) that should be used for the format. 
     * @param format The output format
     * @return The pix flags value.
     */
    public String getPixFlags(String format)
    {
        return "image/fits".equals(format) ? "C" : "V";
    }

}
