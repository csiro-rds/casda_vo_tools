package au.csiro.casda.votools.security;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import au.csiro.casda.votools.utils.Utils;

/**
 * 
 * The Spring Web Security Config class
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests().antMatchers("/configure/**").hasRole(Utils.ADMIN_ROLE).and().formLogin()
                .loginPage("/login").defaultSuccessUrl("/configure/home").permitAll().and().logout()
                .invalidateHttpSession(true).logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/logoutMessage").permitAll();
        http.formLogin();
        http.headers().cacheControl().disable();
        http.csrf().disable();
    }

    /**
     * Security in-memory user configuration
     * 
     * @param auth
     *            the AuthenticationManagerBuilder
     * @throws Exception
     *             thrown when errors during init
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.authenticationProvider(new VoToolsAuthenticationProvider());
    }
}
