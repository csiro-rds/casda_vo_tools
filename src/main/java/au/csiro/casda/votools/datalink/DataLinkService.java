package au.csiro.casda.votools.datalink;

import java.io.IOException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.votools.config.ConfigValueKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.jpa.repository.VoTableRepositoryService;
import au.csiro.casda.votools.logging.CasdaVoToolsEvents;
import au.csiro.casda.votools.result.VotableError;
import au.csiro.casda.votools.siap2.SiapParamProcessor;
import au.csiro.casda.votools.utils.RequestToken;
import au.csiro.casda.votools.utils.Utils;
import au.csiro.casda.votools.utils.VoKeys;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2014 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * A service to retrieve links to data access services for CASDA data products.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataLinkService extends Configurable
{
    /**
     * Name to use in generated VOTable results
     */
    private static final String CASDA_DATALINK_RESULT_NAME = "CASDA Data Link";
    
    private static final int KB_IN_GB= 1024 * 1024;

    private static Logger logger = LoggerFactory.getLogger(DataLinkService.class);

    private List<String> authTrustedIp;

    private Configuration config;

    private VoTableRepositoryService voTableRepositoryService;

    private boolean ready;
    private String linksUrl;
    private String syncServiceNameWeb;
    private String syncServiceNameInternal;
    private String syncServiceUrl;
    private String syncServiceUrlInternal;
    private String asyncServiceNameWeb;
    private String asyncServiceNameInternal;
    private String asyncServiceUrl;
    private String cutoutServiceName;
    private String cutoutServiceUrl;
    private String baseUrl;
    private String dataLinkBaseUrl;
    private String dataLinkAccessEncriptionSecretKey;
    private String cutoutUiServiceUrl;
    private String cutoutUiServiceName;
    private long datalinkDownloadLimitHttp;

    /**
     * Constructor
     * 
     * @param configRegistry
     *            The configuration registry
     * @param voTableRepositoryService
     *            The VoTableRepositoryService Service helpful in fetching project related information
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @Autowired
    public DataLinkService(ConfigurationRegistry configRegistry, VoTableRepositoryService voTableRepositoryService)
            throws ConfigurationException
    {
        // Register for callbacks when configuration changes.
        configRegistry.register(this);
        linksUrl = config.get(ConfigValueKeys.DATALINK_LINKS_URL);
        syncServiceNameWeb = config.get(ConfigValueKeys.DATALINK_SYNC_SERVICE_NAME_WEB);
        syncServiceNameInternal = config.get(ConfigValueKeys.DATALINK_SYNC_SERVICE_NAME_INTERNAL);
        syncServiceUrl = config.get(ConfigValueKeys.DATALINK_SYNC_SERVICE_URL);
        syncServiceUrlInternal = config.get(ConfigValueKeys.DATALINK_SYNC_SERVICE_URL_INTERNAL);
        asyncServiceNameWeb = config.get(ConfigValueKeys.DATALINK_ASYNC_SERVICE_NAME_WEB);
        asyncServiceNameInternal = config.get(ConfigValueKeys.DATALINK_ASYNC_SERVICE_NAME_INTERNAL);
        asyncServiceUrl = config.get(ConfigValueKeys.DATALINK_ASYNC_SERVICE_URL);
        dataLinkBaseUrl = config.get(ConfigValueKeys.DATALINK_BASE_URL);
        dataLinkAccessEncriptionSecretKey = config.get(ConfigValueKeys.DATA_LINK_ACCESS_SECRET_KEY);
        cutoutServiceUrl = config.get(ConfigValueKeys.DATALINK_CUTOUT_URL);
        cutoutServiceName = config.get(ConfigValueKeys.DATALINK_CUTOUT_SERVICE_NAME);
        
        datalinkDownloadLimitHttp = convertLimit(ConfigValueKeys.DATALINK_DOWNLOAD_LIMIT_HTTP);

        this.voTableRepositoryService = voTableRepositoryService;
    }

    private long convertLimit(String limit)
    {
        try
        {
            String value =  config.get(limit);
            //converts limit from GB to KB
            return Long.parseLong(value) * KB_IN_GB;
        }
        catch(NumberFormatException e)
        {
            logger.info("Value for {} could not be retrieved, property may not exist or is not a valid number", limit);
            return 0;
        }
    }

    /**
     * Checks whether the request is from a trusted ip address, and so we can trust the authorisation information in the
     * header
     * 
     * @param request
     *            the user's request
     * @return true if the the source of the request is a trusted ip address
     */
    public boolean trustAuthHeader(HttpServletRequest request)
    {
        boolean trustAuthHeader = this.authTrustedIp.contains(request.getRemoteAddr());
        logger.info("Request from {}, is from trusted ip address {}", request.getRemoteAddr(), trustAuthHeader);
        return trustAuthHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#setConfiguration(au.csiro.casda.votools.config.Configuration)
     */
    @Override
    public void setConfiguration(Configuration config)
    {
        ready = false;
        this.config = config;
    }

    /**
     * Return configuration
     * 
     * @return Objects configuration
     */
    public Configuration getConfiguration()
    {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#isReady()
     */
    @Override
    public synchronized boolean isReady() throws ConfigurationException
    {
        if (!ready && config != null)
        {
            authTrustedIp = config.getList("auth.trusted.ip");
            baseUrl = config.get(ConfigValueKeys.APP_BASE_URL);
            ready = true;
        }
        return ready;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Configurable#invalidate()
     */
    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    /**
     * Process a request to provide links to zero or more files as identified by their ids in the param map. The result
     * will be written to the supplied writer. If an error is encountered the error will be written in VOTABLE format to
     * the writer.
     *
     * @param writer
     *            The destination for the query output.
     * @param requestedIds
     *            The requestedIds to be processed.
     * @param userId
     *            The user Id.
     * @param loginSystem
     *            the user's login system
     * @param projectCodes
     *            Project codes list
     * @param casdaAdmin
     *            Boolean whether is casda admin
     * @param accessTime
     *            The date and time when access was requested.
     * @return true if the query was successful, false if an error occurred
     * @throws InterruptedException
     *             if the job is interrupted
     * @throws IOException
     *             if an error occurs using writer
     */
    public boolean processQuery(Writer writer, String[] requestedIds, String userId, String loginSystem,
            List<String> projectCodes, boolean casdaAdmin, Date accessTime) throws InterruptedException, IOException
    {
        ZonedDateTime start = ZonedDateTime.now();
        List<Long> projectIds = null;
        if (!casdaAdmin && !CollectionUtils.isEmpty(projectCodes))
        {
            projectIds = voTableRepositoryService.fetchProjectIdsFromCodes(projectCodes, config.gtDao().getSchema());
        }

        boolean authenticated = !VoKeys.ANONYMOUS_USER.equals(userId);
        // Create VO table response
        DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(authenticated ? dataLinkBaseUrl : baseUrl);

        try
        {
            if (ArrayUtils.isEmpty(requestedIds))
            {
                // If no ids, add default response about service
                buildServiceResponse(builder);
            }
            else
            {
                builder.withResultsTable();
                // For each id, add row with pointer to data access
                for (String id : requestedIds)
                {
                    buildDataAccessForId(builder, id, userId, loginSystem, projectIds, casdaAdmin, accessTime);
                }
            }

            writer.append(builder.getXml());
        }
        catch (Exception e)
        {
            logger.error(CasdaVoToolsEvents.E150.messageBuilder()
                    .addAll(Arrays.asList(Arrays.toString(requestedIds),
                            CasdaFormatter.formatDateTimeForLog(Date.from(start.toInstant())),
                            "failed to build AccessData URI's: " + e.getMessage(), userId))
                    .toString(), e);
            this.reportDataLinkError(writer, String.format(SiapParamProcessor.TRANSIENT_FAULT_MSG));
            return false;
        }

        return true;
    }

    private void buildServiceResponse(DataLinkVoTableBuilder builder)
    {
        builder.withResultsTable().withServiceMeta(linksUrl);
    }

    private void buildDataAccessForId(DataLinkVoTableBuilder builder, String id, String userId, String loginSystem,
            List<Long> projectIds, boolean casdaAdmin, Date accessTime) throws Exception
    {
        String contentType;
        String table= "";
        
        if(StringUtils.isBlank(id))
        {
            builder.withErrorResult(id,
                    "UsageFault: Invalid id " + StringEscapeUtils.escapeXml10(id));
            return;
        }
        else if(id.toLowerCase().matches("^cube-[0-9]+$"))
        {
        	table = "casda.image_cube";
        }
        else if(id.toLowerCase().matches("^visibility-[0-9]+$"))
        {
        	table = "casda.measurement_set";
        }
        else if(id.toLowerCase().matches("^spectrum-[0-9]+$"))
        {
        	table = "casda.spectrum";
        }
        else if(id.toLowerCase().matches("^moment_map-[0-9]+$"))
        {
        	table = "casda.moment_map";
        }
        else
        {
            builder.withErrorResult(id,
                    "UsageFault: Invalid id " + StringEscapeUtils.escapeXml10(id));
            return;
        }
        Long dataProductId = Long.parseLong(id.split("-")[1]);
        
        long contentLengthKb = getContentLength(table, dataProductId);
        
        if (contentLengthKb > 0)
        {
            if (StringUtils.isNotBlank(syncServiceUrl) || StringUtils.isNotBlank(asyncServiceUrl)
                    || StringUtils.isNotBlank(cutoutServiceUrl) || StringUtils.isNotBlank(cutoutUiServiceUrl))
            {
                // show access data link to casdaAdmins or project members
                if (isAccessAllowed(casdaAdmin, userId, projectIds, table, dataProductId))
                {
                    if (id.toLowerCase().startsWith("cube") || id.toLowerCase().startsWith("spectrum") 
                    		|| id.toLowerCase().startsWith("moment_map"))
                    {
                        contentType = "application/fits";
                    }
                    else
                    {
                        contentType = "application/tar";
                    }

                    RequestToken requestToken =
                            new RequestToken(id, userId, loginSystem, accessTime, dataLinkAccessEncriptionSecretKey);
                    
                    if (StringUtils.isNotBlank(syncServiceUrl))
                    {
                        //web download
                        requestToken.setDownloadMode(RequestToken.WEB_DOWNLOAD);
                        //if less than limit or there is not limit
                        if(contentLengthKb <= datalinkDownloadLimitHttp || datalinkDownloadLimitHttp == 0)
                        {
                            builder.withAccessUrlResult(id, syncServiceUrl + requestToken.toEncryptedString(),
                                    syncServiceNameWeb, contentType, (long) contentLengthKb * Utils.ONE_KB_IN_BYTES); 
                        }
                        else
                        {
                            String message = "DefaultFault: This option is unavailable due to the file size exceeding " 
                                    + datalinkDownloadLimitHttp / KB_IN_GB + " GB in size";
                            builder.withErrorResult(id, syncServiceNameWeb, message);
                        }
                        
                        //to internal account, this can be disabled by removing property.
                        if (StringUtils.isNotBlank(syncServiceUrlInternal))
                        {
                            requestToken.setDownloadMode(RequestToken.INTERNAL_DOWNLOAD);
                            builder.withAccessUrlResult(id, syncServiceUrlInternal + requestToken.toEncryptedString(),
                                  syncServiceNameInternal, contentType, (long) contentLengthKb * Utils.ONE_KB_IN_BYTES);
                        }

                    }

                    if (StringUtils.isNotBlank(asyncServiceUrl))
                    {
                        //web download
                        requestToken.setDownloadMode(RequestToken.WEB_DOWNLOAD);
                        //if lesss than limit or there is not limit
                        if(contentLengthKb <= datalinkDownloadLimitHttp || datalinkDownloadLimitHttp == 0)
                        {
                            builder.withServiceDefResult(id, "async_service", asyncServiceNameWeb, contentType,
                                    (long) contentLengthKb * Utils.ONE_KB_IN_BYTES, requestToken.toEncryptedString());
                        }
                        else
                        {
                            String message = "DefaultFault: This option is unavailable due to the file size exceeding " 
                                    + datalinkDownloadLimitHttp / KB_IN_GB + " GB in size";
                            builder.withErrorResult(id, asyncServiceNameWeb, message);
                            
                        }

                        //to internal account, this can be disabled by removing property.
                        if (StringUtils.isNotBlank(asyncServiceNameInternal))
                        {
                            requestToken.setDownloadMode(RequestToken.INTERNAL_DOWNLOAD);
                            builder.withServiceDefResult(id, "async_service", asyncServiceNameInternal, contentType,
                                    (long) contentLengthKb * Utils.ONE_KB_IN_BYTES, requestToken.toEncryptedString());
                        }
                        
                        builder.withServiceDefinition("async_service", "ivo://ivoa.net/std/SODA#async-1.0",
                                asyncServiceUrl);
                    }
                    
                    /*
                     *  Cutout services have no maximum limit as the size of the cutout is not known at this point.
                     */
                    if(id.toLowerCase().startsWith("cube") && StringUtils.isNotBlank(cutoutServiceUrl))
                    {
                        requestToken.setDownloadMode(RequestToken.CUTOUT);
                        builder.withServiceDefResult(id, "cutout_service", cutoutServiceName, "#cutout", contentType,
                                null, requestToken.toEncryptedString());
                        builder.withServiceDefinition("cutout_service", "ivo://ivoa.net/std/SODA#async-1.0",
                                cutoutServiceUrl, true);
                    }
                }
                else if(!VoKeys.ANONYMOUS_USER.equals(userId))
                {
                    builder.withErrorResult(userId,
                            "DefaultFault: Sorry, but you do not have permission to access this data product");
                }
            }

            if (VoKeys.ANONYMOUS_USER.equals(userId))
            {
                String proxiedDatalinkUrl = dataLinkBaseUrl + "datalink/links?ID=" + id;
                builder.withAccessUrlResult(id, proxiedDatalinkUrl, "Authenticated Data Link",
                        "application/x-votable+xml;content=datalink", null, "#this");
            }
        }
        else
        {
            builder.withErrorResult(id,
                    "NotFoundFault: " + StringEscapeUtils.escapeXml10(id) + " cannot be found");
        }
    }
    
    /**
     * Process a request to provide a link to a file as identified by its id in the param map. The resulting url
     * will be returned or in the case of an error, the error will be written in VOTABLE format to
     * the writer.
     *
     * @param requestedId
     *            The requestedId to be processed.
     * @param userId
     *            The user Id.
     * @param loginSystem
     *            the user's login system
     * @param projectCodes
     *            Project codes list
     * @param casdaAdmin
     *            Boolean whether is casda admin
     * @param accessTime
     *            The date and time when access was requested.
     * @return the link if file exists and is accessible, else a votable containing the error
     */
    public Object processDownload(String requestedId, String userId, String loginSystem,
            List<String> projectCodes, boolean casdaAdmin, Date accessTime)
    {
    	List<Long> projectIds = null;
        if (!casdaAdmin && !CollectionUtils.isEmpty(projectCodes))
        {
            projectIds = voTableRepositoryService.fetchProjectIdsFromCodes(projectCodes, config.gtDao().getSchema());
        }
        
        boolean authenticated = !VoKeys.ANONYMOUS_USER.equals(userId);
		DataLinkVoTableBuilder builder = new DataLinkVoTableBuilder(authenticated ? dataLinkBaseUrl : baseUrl);
		builder.withResultsTable();

        if (StringUtils.isEmpty(requestedId) || !(requestedId.matches("^spectrum-[0-9]+$") 
        		|| requestedId.matches("^moment_map-[0-9]+$")))
        {
            builder.withErrorResult(requestedId,
                    "UsageFault: Invalid id " + StringEscapeUtils.escapeXml10(requestedId));
        }
        else
        {
            String table = requestedId.toLowerCase().startsWith("spectrum") ? "casda.spectrum" : "casda.moment_map";
            
            Long dataProductId = Long.parseLong(requestedId.split("-")[1]);

            boolean allowAccessDataLink = isAccessAllowed(casdaAdmin, userId, projectIds, table, dataProductId);
            long contentLengthKb = getContentLength(table, dataProductId);
            
            if(allowAccessDataLink && contentLengthKb > 0)
        	{
                RequestToken requestToken =
                        new RequestToken(requestedId, userId, loginSystem, accessTime, dataLinkAccessEncriptionSecretKey);
                requestToken.setDownloadMode(RequestToken.WEB_DOWNLOAD);
                
            	return syncServiceUrl +  requestToken.toEncryptedString();
        	}
        	else
        	{
                builder.withErrorResult(requestedId,
                        "NotFoundFault: " + StringEscapeUtils.escapeXml10(requestedId) + " cannot be found");
        	}
        }
    	return builder;
    }
    
    private long getContentLength(String table, Long dataProductId)
    {

        // TODO take out hard-coded DB check and allow for entry of tables into properties so people can
        // use with their custom databases.
        // will also have to allow for mime-types
    	long contentLengthKb = 0;
    	String sql = "select filesize, released_date from " + table + " where id = ?";
        try
        {
            Map<String, Object> result = config.gtDao().getTemplate().queryForMap(sql, dataProductId);
            if (result != null && !result.isEmpty())
            {
                contentLengthKb = (Long) result.get("filesize");
            }
            return contentLengthKb;
        }
        catch (EmptyResultDataAccessException e)
        {
            // not too worried here as this just means there is not matching file for this id in the database
            return 0;
        }
    }
    
    private boolean isAccessAllowed
    		(boolean casdaAdmin, String userId, List<Long> projectIds, String table, Long dataProductId)
    {
        // perform project wise access check if not a casdaAdmin
        // Note: File access is restricted to authenticated users, even for released data products.
        boolean authenticated = !VoKeys.ANONYMOUS_USER.equals(userId);
        if(casdaAdmin)
        {
        	return true;
        }
        else if (authenticated)
        {
            if (CollectionUtils.isEmpty(projectIds))
            {
                projectIds = new ArrayList<>();
                projectIds.add(-1L);
            }

            String query = "select id from " + table + " where id = ?"
                    + " and (released_date is not null or project_id in ("
                    /*
                     * adding the ids as a string here is ok because ids are Longs and data is from a trusted
                     * source
                     */
                    + projectIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + "))";

            String result;
            try
            {
                result = config.gtDao().getTemplate().queryForObject(query, new Object[] { dataProductId },
                        String.class);
            }
            catch (EmptyResultDataAccessException e)
            {
                // No row means that the user does not have access to this data product
                result = null;
            }

            return result != null;
        }
        else
        {
        	return casdaAdmin;
        }
    }

    /**
     * Reports an error in a DataLink request by outputting a VOTABLE to the writer.
     *
     * @param writer
     *            The writer to send the error info to.
     * @param errorMsg
     *            The description of the error.
     * @throws IOException
     *             If the error cannot be written.
     */
    public void reportDataLinkError(Writer writer, String errorMsg) throws IOException
    {
        writer.append(VotableError.reportError(CASDA_DATALINK_RESULT_NAME, errorMsg));
    }

}
