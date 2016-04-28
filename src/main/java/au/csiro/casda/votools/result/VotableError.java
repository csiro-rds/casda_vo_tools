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


import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Utility class to generate Votable errors.
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class VotableError
{

    /**
     * Reports an error for a VOSI request by outputting a VOTABLE in xml format.
     *
     * @param resourceName
     *            The name to use in the resource element of the votable
     * @param errorMsg
     *            The description of the error.
     * @return the votable formatted xml containing the error
     * @throws java.io.IOException
     *             If the error cannot be written.
     */
    public static String reportError(String resourceName, String errorMsg) throws IOException
    {
        StringBuilder writer = new StringBuilder();
        writer.append("<?xml version=\"1.0\"?>\r\n");
        writer.append("<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\" "
                + "xmlns:stc=\"http://www.ivoa.net/xml/STC/v1.30\" >\r\n");
        writer.append("<RESOURCE name=\"");
        writer.append(resourceName);
        writer.append("\" type='results'>\r\n");
        writer.append("<INFO name=\"QUERY_STATUS\" value=\"ERROR\">");
        writer.append(StringEscapeUtils.escapeXml10(errorMsg));
        writer.append("</INFO>\r\n");
        writer.append("</RESOURCE>\r\n");
        writer.append("</VOTABLE>\r\n");
        return writer.toString();
    }
    
    /**
     * Reports an error for a SCS request by outputting a VOTABLE in xml format.
     * This is an older spec, and has requirements that are different to TAP.
     *
     * @param resourceName
     *            The name to use in the resource element of the votable
     * @param errorMsg
     *            The description of the error.
     * @return the votable formatted xml containing the error
     * @throws java.io.IOException
     *             If the error cannot be written.
     */
    public static String reportScsError(String resourceName, String errorMsg) throws IOException
    {
        StringBuilder writer = new StringBuilder();
        writer.append("<?xml version=\"1.0\"?>\r\n");
        writer.append("<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\" "
                + "xmlns:stc=\"http://www.ivoa.net/xml/STC/v1.30\" >\r\n");
        writer.append("<DESCRIPTION>");
        writer.append(resourceName);
        writer.append("</DESCRIPTION>\r\n");
        writer.append("<INFO ID=\"Error\" name=\"Error\" value=\"");
        writer.append(StringEscapeUtils.escapeXml10(errorMsg));
        writer.append("\"/>\r\n");
        writer.append("</VOTABLE>\r\n");
        return writer.toString();
    }
}
