package au.csiro.casda.votools.siap1;

import java.beans.Transient;

import org.apache.commons.lang3.StringUtils;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * A DTO describing a field to be returned in a SIAP v1 result.
 * <p>
 * Copyright 2022, CSIRO Australia. All rights reserved.
 */
public class Siap1Field
{
    private final String fieldName;
    private final String key;
    private String arraysize;
    private String datatype;
    private String unit;
    private String ucd;
    private String description;
    private int order;
    
    /**
     * Constructor.
     * @param fieldName The name of the field in the results. 
     * @param key The key for the field in the VOTable field map. 
     */
    public Siap1Field(String fieldName, String key)
    {
        this.fieldName = fieldName;
        this.key = key;
    }

    public String getArraysize()
    {
        return arraysize;
    }

    public void setArraysize(String arraysize)
    {
        this.arraysize = arraysize;
    }

    public String getDatatype()
    {
        return datatype;
    }

    public void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getUcd()
    {
        return ucd;
    }

    public void setUcd(String ucd)
    {
        this.ucd = ucd;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public String getKey()
    {
        return key;
    }
    
    /**
     * @return An XML string containing the description of the field as a VOTABLE FIELD node.
     */
    @Transient
    public String getFieldDef()
    {
        StringBuffer s = new StringBuffer();
        s.append("<FIELD ID=\"");
        s.append(fieldName);
        s.append("\" name=\"");
        s.append(fieldName);
        s.append("\" datatype=\"");
        s.append(datatype);
        s.append("\"");
        if (StringUtils.isNotBlank(arraysize))
        {
            s.append(String.format(" arraysize=\"%s\"", arraysize));
        }
        if (StringUtils.isNotBlank(unit))
        {
            s.append(String.format(" unit=\"%s\"", unit));
        }
        if (StringUtils.isNotBlank(ucd))
        {
            s.append(String.format(" ucd=\"%s\"", ucd));
        }
        s.append(">");
        if (StringUtils.isNotBlank(description))
        {
            s.append(String.format("<DESCRIPTION>%s</DESCRIPTION>", description));
        }
        s.append(" </FIELD>");
        return s.toString();
    }
}
