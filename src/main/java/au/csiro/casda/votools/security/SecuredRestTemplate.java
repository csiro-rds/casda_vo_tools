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


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;


/**
 * 
 * RestTemplate to access secured rest services (SSL enabled)
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class SecuredRestTemplate extends RestTemplate
{
    /**
     * Constructor
     * 
     * @param userName
     *            The username to be used for basic authentication.
     * @param password
     *            The password to be used for basic authentication.
     */
    public SecuredRestTemplate(String userName, String password)
    {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        // Hostname verification is turned off in NoopHostnameVerifier so this can work on all our environments
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        requestFactory.setHttpClient(httpClient);

        //If basic auth credentials are not provided don't intercept to add Authorization header
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password))
        {
            setRequestFactory(requestFactory);
        }
        else
        {
            List<ClientHttpRequestInterceptor> interceptors = Collections
                    .<ClientHttpRequestInterceptor> singletonList(new BasicAuthorizationInterceptor(userName, password));
            setRequestFactory(new InterceptingClientHttpRequestFactory(requestFactory, interceptors));
        } 
    }

    private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor
    {
        private final String username;
        private final String password;

        public BasicAuthorizationInterceptor(String username, String password)
        {
            this.username = username;
            this.password = (password == null ? "" : password);
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException
        {
            byte[] token = Base64.encode((this.username + ":" + this.password).getBytes(Charsets.UTF_8));           
            request.getHeaders().add("Authorization", "Basic " + new String(token, Charsets.UTF_8));
            return execution.execute(request, body);
        }
    }    
}
