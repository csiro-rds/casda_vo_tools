package au.csiro.casda.votools.security;

import java.io.File;
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

import au.csiro.casda.votools.VoToolsApplication.ConfigLocation;
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
    
    private ConfigLocation configLocation;
    
    /** Constant for teh vo tools admin role */
    public static final String ADMIN_ROLE = "VO_TOOLS_ADMIN";
    
    /**
     * Create a new instance of VoToolsAuthenticationProvider
     * @param configLocation The ConfigLocation container.
     */
    public VoToolsAuthenticationProvider(ConfigLocation configLocation) 
    {
		this.configLocation = configLocation;
	}
    
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
            File authzFile = configLocation.getConfigFile(Utils.AUTH_FILE_NAME, false);
        	
            String[] details = Utils.retrieveAdminCredentials(authzFile);
            if(username.equals(details[0]) && Utils.authenticate(password, details[1]))
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
