package au.csiro.casda.votools;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import au.csiro.casda.services.dto.MessageDTO;

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
 * Default exception handler
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@ControllerAdvice
class ExceptionHandlers
{

    /**
     * Default view for errors will match associated page *.jsp
     */
    public static final String DEFAULT_ERROR_VIEW = "error";

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    /**
     * Exception handler for ServletException that responds with an appropriate status code and details in a
     * JSON-serialised MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @param response
     *            the http response
     * @return a ResponseEntity
     * @throws Exception when handling the exception fails.
     */
    @ExceptionHandler({ ServletException.class })
    public Object handleServletException(ServletException ex, WebRequest request, HttpServletResponse response)
        throws Exception
    {
        logger.error("There was a {} processing request: {}", ex.getClass().getName(), request, ex);
        /*
         * Delegating to a ResponseEntityExceptionHandler because it does all the hard work of translating Spring's
         * internal exceptions into the right response status codes and messages.
         */
        ResponseEntityExceptionHandler handler = new ResponseEntityExceptionHandler()
        {
        };
        ResponseEntity<Object> defaultResponse = handler.handleException(ex, request);
        return handleExceptionResponse(ex, request, response, defaultResponse.getStatusCode());
    }

    /**
     * Exception handler for Exception that responds with an appropriate status code and details in a JSON-serialised
     * MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @param response
     *            the servlet response
     * @return a ResponseEntity
     */
    @ExceptionHandler({ Exception.class, RuntimeException.class })
    public Object handleRemainingExceptions(Throwable ex, WebRequest request, HttpServletResponse response)
    {
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof HttpStatusCodeException)
        {
            returnStatus = ((HttpStatusCodeException) ex).getStatusCode();
        }
        else
        {
            ResponseStatus status = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
            if (status != null && status.value() != null && !status.value().is2xxSuccessful())
            {
                returnStatus = status.value();
            }
        }
        return handleExceptionResponse(ex, request, response, returnStatus);
    }

    /**
     * Directs to the error page if the response expects html, otherwise returns json
     * @param ex the exception
     * @param request the web request
     * @param response the response
     * @param returnStatus the return status for the error
     * @return the error page model and view for an html response, or json response
     */
    private Object handleExceptionResponse(Throwable ex, WebRequest request, HttpServletResponse response,
            HttpStatus returnStatus)
    {
        logger.error("There was a {} processing request: {}", ex.getClass().getName(), request, ex);

        if (StringUtils.contains(request.getHeader("accept"), "text/html"))
        {
            response.setStatus(returnStatus.value());
            // setup and send the user to a default error-view.
            ModelAndView mav = new ModelAndView();
            mav.addObject("exception", ex);
            mav.addObject("url", request.toString());
            mav.setViewName(DEFAULT_ERROR_VIEW);
            return mav;
        }
        else
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<MessageDTO>(new MessageDTO(MessageDTO.MessageCode.FAILURE, ex.getMessage()),
                    headers, returnStatus);
        }
    }

}
