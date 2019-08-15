package au.csiro.casda.votools.tap;

import java.io.InputStream;

/**
 * Holds details of a table that the user has uploaded to query against.
 * 
 * <p>
 * Copyright 2018, CSIRO Australia. All rights reserved.
 */
public class UploadedTable
{
    private String name;
    private String uri;
    private InputStream stream;

    /**
     * Constructor
     * @param name The name of the table
     * @param uri The address of the source of the table.
     */
    public UploadedTable(String name, String uri)
    {
        this.name = name;
        this.uri = uri;

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public InputStream getStream()
    {
        return stream;
    }

    public void setStream(InputStream stream)
    {
        this.stream = stream;
    }
}
