package au.csiro.casda.votools.utils;

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



/**
 * Common keys used in request headers and parameters.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class VoKeys
{

    /**
     * The catalogue / table name that is the subject of the SCS query
     */
    public static final String CATALOG = "catalog";
    
    /**
     * The user id who made the request, set to anonymous if there is no value
     */
    public static final String USER_ID = "user_id";
    
    /**
     * The login system of the user who made the request, set to "" if there is no value
     */
    public static final String LOGIN_SYSTEM = "login_system";
    
    /**
     * The user name who made the request, set to anonymous if there is no value
     */
    public static final String USER_NAME = "user_name";
    /**
     * The default user id for an anonymous user
     */
    public static final String ANONYMOUS_USER = "anonymous";
    /**
     * The list of project codes a user has access to, from the header 
     */
    public static final String USER_PROJECTS = "project_codes";
    /**
     * The original user's request query string
     */
    public static final String PARAM_QUERY_STRING = "paramQueryString";
    /**
     * The VO authorisation header key for user id
     */
    public static final String VO_AUTH_HEADER_USER_ID = "X-VOTools-userId";    
    /**
     * The VO authorisation header key for user name
     */
    public static final String VO_AUTH_HEADER_USER_NAME= "X-VOTools-userName";
    /**
     * The VO autorisation header key for user's login system
     */
    public static final String VO_AUTH_HEADER_LOGIN_SYSTEM = "X-VOTools-loginSystem";
    /**
     * The VO authorisation header key for user projects
     */
    public static final String VO_AUTH_HEADER_USER_PROJECTS = "X-VOTools-projects";
    /**
     * Key for requester's IP address
     */
    public static final String KEY_REQUESTER_IP_ADDRESS = "requester_ip_address";
    /**
     * Constant key for storing request start time in maps
     */
    public static final String SUBMITTED_TIME = "casda_submitted_instant";
    /**
     * Constant key for storing job execution mode (sync/async)
     */
    public static final String SUBMITTED_MODE = "casda_submitted_mode";
    /**
     * Key for capability url, if capabilities details proxied from another url
     */
    public static final String VO_HEADER_CAPABILITIES_URL = "X-VOTools-capabilities-url";

    /**
     * TAP request parameter map's key for the original ADQL submitted query
     */
    public static final String STR_KEY_ADQL_QUERY = "query";

    /**
     * TAP request parameter map's key for the original SIAP submitted query
     */
    public static final String STR_KEY_SIAP_QUERY = "siapQuery";

    /**
     * TAP request parameter map's key for the original SSAP submitted query
     */
    public static final String STR_KEY_SSAP_QUERY = "ssapQuery";
    
    /**
     * Parameter key to indicate the heading to use in a VO Table result.
     */
    public static final String VO_TABLE_HEADING = "voTableHeading";
    
    /**
     * TAP request parameter map's value for project_codes
     */
    public static final String STR_PROJECT_CODES_ALL = "all";
    
    /**
     * Projects database table name.
     */
    public static final String STR_PROJECT_TABLE_NAME = "Project";

    /**
     * TAP request parameter map's key for maximum records to retrieve
     */
    public static final String STR_KEY_MAXREC = "maxrec";

    /** SSAP service protocol info parameter key */
    public static final String STR_KEY_SERVICE_PROTOCOL = "SERVICE_PROTOCOL";
    
}
