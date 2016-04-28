package au.csiro.casda.votools.result;

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


import java.util.Arrays;
import java.util.List;

/** The formats in which we can output the results of the TAP query. */
public enum OutputFormat
{
    /** A generic XML table format specified by IVOA. */
    VOTABLE("xml", "application/x-votable+xml", "text/xml", "votable", "xml"),

    /** Comma separated values format. */
    CSV("csv", "text/csv;header=present", "text/csv", "csv"),

    /** Tab separated values format. */
    TSV("tsv", "text/tab-separated-values", "tsv");

    private List<String> identifiers;

    private String fileExtension;

    /**
     * Enum constructor
     * 
     * @param fileExtension
     *            the file extension (eg xml)
     * @param formats
     *            list of valid formats for this OuptutFormat (eg for VO table, includes "text/xml", "votable")
     */
    OutputFormat(String fileExtension, String... formats)
    {
        this.fileExtension = fileExtension;
        this.identifiers = Arrays.asList(formats);
    }

    /**
     * @return a list of valid mime-types/short-formats that can be used to refer to this OutputFormat. @see
     *         #findMatchingFormat
     */
    public List<String> getIdentifiers()
    {
        return identifiers;
    }

    /**
     * Find a format based on its mime type or short format name.
     *
     * @param formatStr
     *            The requested format.
     * @return The matching format, or null if none match.
     */
    public static OutputFormat findMatchingFormat(String formatStr)
    {
        // case insensitive
        String formatStrLower = formatStr.toLowerCase();
        return Arrays.asList(OutputFormat.values()).stream().filter(outputFormat -> {
            return outputFormat.identifiers.contains(formatStrLower);
        }).findFirst().orElse(null);
    }

    /**
     * @return The default MIME type for the format.
     */
    public String getDefaultContentType()
    {
        return identifiers.get(0);
    }

    /**
     * @return The file extension to use for this output type.
     */
    public String getFileExtension()
    {
        return this.fileExtension;
    }
}