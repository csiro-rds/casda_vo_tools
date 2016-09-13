package au.csiro.casda.votools.config;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * A list of keys for configuration values. In the future this should become an enum to enable looping over it
 * to gather all config values from the spring and yaml config files for use elsewhere in the application. 
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public abstract class ConfigValueKeys
{
    /** The key for the application's base URL. */
    public static final String APP_BASE_URL = "application.base.url";

    /** The key for the database connection URL. */
    public static final String CONNECTION_URL = "connection.url";
    
    /** Constant for the data link cut-out URL */
    public static final String DATALINK_CUTOUT_URL = "datalink.cutout.url";
    
    /** Constant for the cut-out service name */
    public static final String DATALINK_CUTOUT_SERVICE_NAME = "datalink.cutout.service.name";

    /** Constant for the sync service name */
    public static final String DATALINK_SYNC_SERVICE_NAME_WEB = "datalink.sync.service.name";
    
    /** Constant for the sync service name */
    public static final String DATALINK_SYNC_SERVICE_NAME_INTERNAL = "datalink.sync.service.name.internal";
    
    /** Constant for the data link sync URL */
    public static final String DATALINK_SYNC_SERVICE_URL = "datalink.sync.service.url";
    
    /** Constant for the data link sync internal URL */
    public static final String DATALINK_SYNC_SERVICE_URL_INTERNAL = "datalink.sync.service.url.internal";
    
    /** Constant for the web service name */
    public static final String DATALINK_ASYNC_SERVICE_NAME_WEB = "datalink.async.service.name";
    
    /** Constant for the web service name */
    public static final String DATALINK_ASYNC_SERVICE_NAME_INTERNAL = "datalink.async.service.name.internal";
    
    /** Constant for the data link async URL */
    public static final String DATALINK_ASYNC_SERVICE_URL = "datalink.async.service.url";
    
    /** Constant for the web service name */
    public static final String DATALINK_WEB_SERVICE_NAME = "datalink.web.service.name";
    
    /** Constant for the data link web service URL */
    public static final String DATALINK_WEB_SERVICE_URL = "datalink.web.service.url";
    
    /** Constant for the data link {links} URL */
    public static final String DATALINK_LINKS_URL = "datalink.links.url";
    
    /** Constant for the data link base URL */
    public static final String DATALINK_BASE_URL = "datalink.base.url";
    
    /** Constant for the web service name */
    public static final String DATA_LINK_ACCESS_SECRET_KEY = "siap.shared.secret.key";

    /** Constant for the cut-out user interface URL */
    public static final String DATALINK_CUTOUT_UI_URL = "datalink.cutout.ui.url";

    /** Constant for the cut-out user interface service name */
    public static final String DATALINK_CUTOUT_UI_SERVICE_NAME = "datalink.cutout.ui.service.name";
    
    /** Constant for the web download size limit */
    public static final String DATALINK_DOWNLOAD_LIMIT_HTTP = "datalink.download.limit.http";
    
    /** Constant for the build number */
    public static final String BUILD_NUMBER = "build.number";
    
    /** Constant for the server environment */
    public static final String ENVIRONMENT = "build.environment";
    
    /** Constant for the server environment */
    public static final String CSS = "stylesheet.address";
    
    /** Constant for the server environment */
    public static final String LOGO_URL = "logo.url";
}
