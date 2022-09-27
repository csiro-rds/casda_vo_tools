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


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class TestVotableError
{

    @Test
    public void testError() throws IOException, ParserConfigurationException, SAXException
    {
        String resourceName = "Query the Cone tap service";
        String errorMsg = "Error getting the results for the query";

        String result = VotableError.reportError(resourceName, errorMsg);

        assertThat(result, containsString(resourceName));
        assertThat(result, containsString(errorMsg));

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(result.getBytes("UTF-8")));
        Element votable = doc.getDocumentElement();
        assertThat(votable.getLocalName(), is("VOTABLE"));
        NodeList childnodes = votable.getChildNodes();
        assertThat(childnodes.getLength(), is(3));
        Node resource = childnodes.item(1);
        assertThat(resource.getLocalName(), is("RESOURCE"));
        assertThat(resource.getAttributes().getNamedItem("name").getNodeValue(), is(resourceName));
        Node info = resource.getChildNodes().item(1);
        assertThat(info.getLocalName(), is("INFO"));
        assertThat(info.getAttributes().getNamedItem("name").getNodeValue(), is("QUERY_STATUS"));
        assertThat(info.getAttributes().getNamedItem("value").getNodeValue(), is("ERROR"));
    }

}
