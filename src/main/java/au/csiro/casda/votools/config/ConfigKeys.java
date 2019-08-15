package au.csiro.casda.votools.config;

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
 * ConfigKeys is a enum of keys used in the config. Keys in this enum will be automatically read from both the 
 * application config and the yaml config.   
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public enum ConfigKeys
{
    /** Key for the table or view to be used for SSAP queries. */
    SSAP_TABLE("ssap.table"),
    
    /** Key for the service configured upper limit on maxrec, being the number of records returned. */ 
    SSAP_OUTPUT_LIMIT("ssap.outputLimit.hard"),

    /** Key for the service configured default maxrec value. */ 
    SSAP_DEFAULT_MAX_REC("ssap.max.records"),

    /** Key for the optional name of the file holding the SSAP metadata response. */ 
    SSAP_METADATA_RESPONSE("ssap.metadata.response"),

    /** Key for the full URL of the XSL stylesheet used to style VOTable responses. */ 
    TAP_VOTABLE_XSL("tap.votable.xsl"), 

    /** Key for the examples endpoint for TAP Queries. */ 
    TAP_EXAMPLES_URL("tap.examples.url"), 

    /** Flag to indicate whether TAP upload is allowed in this service. */ 
    TAP_UPLOAD_ENABLED("tap.upload.enabled"),
    
    /** Flag that specifies the maximum upload limit for TAP Uploads. */
    TAP_UPLOAD_LIMIT("tap.upload.limit.bytes");
    
    private final String key;

    private ConfigKeys(String key)
    {
        this.key = key;
        
    }

    public String getKey()
    {
        return key;
    }
}
