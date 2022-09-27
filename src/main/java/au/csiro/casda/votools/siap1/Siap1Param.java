package au.csiro.casda.votools.siap1;

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
 * The supported SIA v2 parameters. Each parameter is mapped to its type.
 * <p>
 * Copyright 2022, CSIRO Australia All rights reserved.
 */
public enum Siap1Param
{
    /** The POS parameter defines the positional region(s) to be searched for data. */
    POS(new PosParamProcessor(), true), 

    /** The SIZE parameter defines the diameter of the search region in decimal degrees */
    SIZE(new SizeParamProcessor(), true), 
   
    /** The SURVEY parameter defines which set of images will be queried. */
    SURVEY(new SurveyParamProcessor(), true),

    /** The FORMAT parameter defines what type sof files should be returned. */
    FORMAT(new FormatParamProcessor()), 

    /** The MAXREC parameter defines how many records should be returned. */
    MAXREC(new MaxrecParamProcessor());

    private String[] fields;
    
    private final Siap1ParamProcessor processor;
    
    private final boolean required;

    /**
     * Create a new enum instance.
     * @param processor The processing class for this param type.
     * @param fields The fields which the processor should use.
     */
    private Siap1Param(Siap1ParamProcessor processor, String... fields)
    {
        this(processor, false, fields);
    }

    /**
     * Create a new enum instance.
     * @param processor The processing class for this param type.
     * @param required Is this parameter required for all SSAP requests. 
     * @param fields The fields which the processor should use.
     */
    private Siap1Param(Siap1ParamProcessor processor, boolean required, String... fields)
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

    public Siap1ParamProcessor getProcessor()
    {
        return processor;
    }

    public boolean isRequired()
    {
        return required;
    }
    
}
