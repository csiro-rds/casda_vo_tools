package au.csiro.casda.votools.ssap;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * The supported SIA v2 parameters. Each parameter is mapped to its type.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public enum SsapParam
{
    /** The POS parameter defines the positional region(s) to be searched for data. */
    POS(new PosParamProcessor()), 

    /** The SIZE parameter defines the diameter of the search region in decimal degrees */
    SIZE(new SizeParamProcessor()), 
    
    /** The BAND parameter defines the energy interval(s) to be searched for data. */
    BAND(new BandParamProcessor(), "em_min", "em_max"),
    
    /** The TIME parameter defines the time interval(s) to be searched for data */
    TIME(new TimeParamProcessor(), "t_min", "t_max"),

    /** The FORMAT parameter defines what type sof files should be returned. */
    FORMAT(new FormatParamProcessor()), 

    /** The MAXREC parameter defines how many records should be returned. */
    MAXREC(new MaxrecParamProcessor()), 

    /** The REQUEST parameter defines what the service should do. */
    REQUEST(new RequestParamProcessor(), true), 

    /** The VERSION parameter manages version negotiation */
    VERSION(new VersionParamProcessor());

    private String[] fields;
    
    private final SsapParamProcessor processor;
    
    private final boolean required;

    /**
     * Create a new enum instance.
     * @param processor The processing class for this param type.
     * @param fields The fields which the processor should use.
     */
    private SsapParam(SsapParamProcessor processor, String... fields)
    {
        this(processor, false, fields);
    }

    /**
     * Create a new enum instance.
     * @param processor The processing class for this param type.
     * @param required Is this parameter required for all SSAP requests. 
     * @param fields The fields which the processor should use.
     */
    private SsapParam(SsapParamProcessor processor, boolean required, String... fields)
    {
        this.processor = processor;
        this.required = required;
        this.fields = fields;
    }

    /**
     * Retrieve the name of the field at the supplied index. 
     * @param fieldIndex The 0 based index of the field.
     * @return The field name, or null if no field exists at that index.
     */
    public String getField(int fieldIndex)
    {
        if (fieldIndex < 0 || fieldIndex>= fields.length)
        {
            return null;
        }
        return fields[fieldIndex];
    }

    public SsapParamProcessor getProcessor()
    {
        return processor;
    }

    public boolean isRequired()
    {
        return required;
    }
    
}
