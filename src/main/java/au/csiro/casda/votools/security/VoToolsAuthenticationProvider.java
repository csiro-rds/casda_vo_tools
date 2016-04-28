package au.csiro.casda.votools.security;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

import au.csiro.casda.votools.utils.Utils;

/**
 * 
 * Custom authentication provider which compares login details to password file.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class VoToolsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
{
    private static final Logger logger = LoggerFactory.getLogger(VoToolsAuthenticationProvider.class);
    
    /** Constant for teh vo tools admin role */
    public static final String ADMIN_ROLE = "VO_TOOLS_ADMIN";
    
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        // do nothing       
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException
    {
        String password = (String) authentication.getCredentials();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
        {
            throw new BadCredentialsException("Empty username or password");
        }

        logger.info("Retrieving user {}", username);
        try 
        {
            String[] details = Utils.retrieveFromFile();
            if(username.equals(details[0]) && password.equals(details[1]))
            {
                ArrayList<VoAuthority> roles = new ArrayList<VoAuthority>();
                roles.add(new VoAuthority());
                return new User(username, password, roles);
            }     
            throw new Exception();
        }
        catch (Throwable e)
        {
            throw new AuthenticationServiceException(username, e);
        }
    }

    private class VoAuthority implements GrantedAuthority
    {
        //Always return this role as there is only one section which requires logging in and its admins only
        @Override
        public String getAuthority()
        {
            return "ROLE_VO_TOOLS_ADMIN";
        }
        
    }
}
