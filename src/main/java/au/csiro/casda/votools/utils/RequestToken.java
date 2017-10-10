package au.csiro.casda.votools.utils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 
 * RequestToken shared between DataLink and AccessData. 
 * This class represents the token shared between these 2 layers and provides
 * encrypted and decrypted forms of itself.
 *  * 
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 *
 */
public class RequestToken
{
    /** Constant for a cutout request*/
    public static final String CUTOUT = "CUTOUT";
    /** Constant for a cutout request*/
    public static final String GENERATED_SPECTRUM = "GENERATED_SPECTRUM";
    /** Constant for a web download request*/
    public static final String WEB_DOWNLOAD = "WEB";
    /** Constant for a download to internal account request*/
    public static final String INTERNAL_DOWNLOAD = "INTERNAL";
    
    private static final Pattern TOKEN_FORMAT = Pattern.compile("^(.+)\\|(.+)\\|(.+)\\|(.+)\\|(\\d+)$");
    private static final int TOKEN_ID_GROUP_INDEX = 1;
    private static final int TOKEN_USERID_GROUP_INDEX = 2;
    private static final int TOKEN_LOGIN_SYSTEM_INDEX = 3;
    private static final int TOKEN_DOWNLOAD_MODE_INDEX = 4;
    private static final int TOKEN_DATE_GROUP_INDEX = 5;
    
    private String id;
    private String userId;
    private String loginSystem;
    private Date accessDate;
    private String downloadMode;
    private String key;   

    /**
     * Constructor used to create a new RequestToken for based on the given id, userId, and accessDate
     * 
     * @param id
     *            The Id if the siap data product
     * @param userId
     *            The requesting user id
     * @param loginSystem
     *            The requesting user's login system
     * @param accessDate
     *            The date and time of the access request
     * @param key
     *            the secret shared key
     */
    public RequestToken(String id, String userId, String loginSystem, Date accessDate, String key)
    {
        this.id = id;
        this.userId = userId;
        this.loginSystem = loginSystem;
        this.accessDate = accessDate;
        this.key = key;
    }

    /**
     * Constructor used to create a new RequestToken from its encrypted form
     * 
     * @param encryptedToken
     *            The Encrypted token
     * @param key
     *            the secret shared key
     */
    public RequestToken(String encryptedToken, String key)
    {
        String token;
        try
        {
            token = Utils.decryptAesUrlSafe(encryptedToken, key);
        }
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException("Invalid token '" + encryptedToken + "'");
        }
        Matcher matcher = TOKEN_FORMAT.matcher(token);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Invalid token '" + encryptedToken + "'");
        }
        this.id = matcher.group(TOKEN_ID_GROUP_INDEX);
        this.userId = matcher.group(TOKEN_USERID_GROUP_INDEX);
        this.loginSystem = matcher.group(TOKEN_LOGIN_SYSTEM_INDEX);
        this.downloadMode = matcher.group(TOKEN_DOWNLOAD_MODE_INDEX);
        try
        {
            this.accessDate = new Date(Long.parseLong(matcher.group(TOKEN_DATE_GROUP_INDEX)));

        }
        catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Invalid token '" + encryptedToken + "'");
        }
        this.key = key;
    }

    /**
     * Return the the object as an encrypted token
     * 
     * @return Url safe encrypted token that represents RequestToken object
     */
    public String toEncryptedString()
    {
        return Utils.encryptAesUrlSafe(toString(), key);
    }

    /**
     * return plain string representation of the RequestToken
     * 
     * @return String represents RequestToken object
     */
    @Override
    public String toString()
    {
        return id + "|" + userId + "|" + loginSystem + "|" + downloadMode + "|" + String.valueOf(accessDate.getTime());
    }

    public String getId()
    {
        return id;
    }

    public String getUserId()
    {
        return userId;
    }

    public Date getAccessDate()
    {
        return accessDate;
    }

    public String getLoginSystem()
    {
        return loginSystem;
    }

    public void setLoginSystem(String loginSystem)
    {
        this.loginSystem = loginSystem;
    }

    public String getDownloadMode()
    {
        return downloadMode;
    }

    public void setDownloadMode(String downloadMode)
    {
        this.downloadMode = downloadMode;
    }
}
