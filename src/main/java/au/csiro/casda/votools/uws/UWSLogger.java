package au.csiro.casda.votools.uws;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uws.job.UWSJob;
import uws.job.user.JobOwner;
import uws.service.log.UWSLog;

/**
 * Logger to send UWS log entries to SLF4J.
 * <p>
 * Copyright 2023, CSIRO Australia. All rights reserved.
 */
public class UWSLogger implements UWSLog
{

    private static Logger logger = LoggerFactory.getLogger(UWSLogger.class);

    @Override
    public void debug(String arg0)
    {
        logger.debug(arg0);
    }

    @Override
    public void debug(Throwable arg0)
    {
        logger.debug("", arg0);
    }

    @Override
    public void debug(String arg0, Throwable arg1)
    {
        logger.debug(arg0, arg1);
    }

    @Override
    public void error(String arg0)
    {
        logger.error(arg0);
    }

    @Override
    public void error(Throwable arg0)
    {
        logger.error("", arg0);
    }

    @Override
    public void error(String arg0, Throwable arg1)
    {
        logger.error(arg0, arg1);
    }

    @Override
    public void info(String arg0)
    {
        logger.info(arg0);
    }

    private void info(String arg0, Throwable arg1)
    {
        logger.info(arg0, arg1);
    }

    @Override
    public void warning(String arg0)
    {
        logger.warn(arg0);
    }

    private void warning(String arg0, Throwable arg1)
    {
        logger.warn(arg0, arg1);
    }

    @Override
    public void log(final LogLevel level, final String context, final String message, final Throwable error)
    {
        log(level, context, null, null, message, error);
    }

    private void log(LogLevel level, String context, String event, final String ID, String message, Throwable error)
    {
        StringBuilder buf = new StringBuilder();
        // Print the context of the error (uws, thread, job, http):
        buf.append((context == null) ? "" : context).append('\t');
        // Print the context event:
        buf.append((event == null) ? "" : event).append('\t');
        // Print an ID (jobID, requestID):
        buf.append((ID == null) ? "" : ID).append('\t');
        // Print the message:
        buf.append((message == null) ? "" : message).append('\t');

        switch (level)
        {
        case DEBUG:
            debug(buf.toString(), error);
            break;
        case WARNING:
            warning(buf.toString(), error);
            break;
        case ERROR:
            error(buf.toString(), error);
            break;

        default:
            info(buf.toString(), error);
        }
    }

    @Override
    public void logHttp(LogLevel level, HttpServletRequest request, String requestId, String message, Throwable error)
    {
        // IF A REQUEST IS PROVIDED, write its details after the message in a new column:
        if (request != null)
        {
            StringBuffer str = new StringBuffer();

            // Write the message (if any):
            if (message != null)
            {
                str.append(message);
            }
            str.append('\t');

            // Write the request type and the URL:
            str.append(request.getMethod()).append(" at ").append(request.getRequestURL());

            // Write the IP address:
            str.append(" from ").append(request.getRemoteAddr());

            // Write the user agent:
            str.append(" using ").append(request.getHeader("User-Agent"));

            // Write the posted parameters:
            str.append(" with parameters (");
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements())
            {
                String param = paramNames.nextElement();
                String paramValue = request.getParameter(param);
                if (paramValue != null)
                {
                    paramValue = paramValue.replaceAll("[\t\n\r]", " ");
                }
                else
                {
                    paramValue = "";
                }
                str.append(param).append('=').append(paramValue);
                if (paramNames.hasMoreElements())
                {
                    str.append('&');
                }
            }
            str.append(')');

            // Send the log message to the log file:
            log(level, "HTTP", "REQUEST_RECEIVED", requestId, str.toString(), error);
        }
        // OTHERWISE, just write the given message:
        else
        {
            log(level, "HTTP", "REQUEST_RECEIVED", requestId, message, error);
        }
    }

    @Override
    public void logHttp(LogLevel level, HttpServletResponse response, String requestId, JobOwner user, String message,
            Throwable error)
    {
        if (response != null)
        {
            StringBuffer str = new StringBuffer();

            // Write the message (if any):
            if (message != null)
            {
                str.append(message);
            }
            str.append('\t');

            // Write the response status code:
            str.append("HTTP-").append(response.getStatus());

            // Write the user to whom the response is sent:
            str.append(" to the user ");
            if (user != null)
            {
                str.append("(id:").append(user.getID());
                if (user.getPseudo() != null)
                {
                    str.append(";pseudo:").append(user.getPseudo());
                }
                str.append(')');
            }
            else
            {
                str.append("ANONYMOUS");
            }

            // Write the response's MIME type:
            if (response.getContentType() != null)
            {
                str.append(" as ").append(response.getContentType());
            }

            // Send the log message to the log file:
            log(level, "HTTP", "RESPONSE_SENT", requestId, str.toString(), error);
        }
        // OTHERWISE, just write the given message:
        else
        {
            log(level, "HTTP", "RESPONSE_SENT", requestId, message, error);
        }
    }

    @Override
    public void logJob(LogLevel level, UWSJob job, String event, String message, Throwable error)
    {
        log(level, "JOB", event, (job == null) ? null : job.getJobId(), message, error);
    }

    @Override
    public void logThread(LogLevel level, Thread thread, String event, String message, Throwable error)
    {
        if (thread != null)
        {
            StringBuffer str = new StringBuffer();

            // Write the message (if any):
            if (message != null)
            {
                str.append(message);
            }
            str.append('\t');

            // Write the thread name and ID:
            str.append(thread.getName()).append(" (thread ID: ").append(thread.getId()).append(")");

            // Write the thread state:
            str.append(" is ").append(thread.getState());

            // Write its thread group name:
            str.append(" in the group " + thread.getThreadGroup().getName());

            // Write the number of active threads:
            str.append(" where ").append(thread.getThreadGroup().activeCount()).append(" threads are active");

            log(level, "THREAD", event, thread.getName(), str.toString(), error);

        }
        else
        {
            log(level, "THREAD", event, null, message, error);
        }
    }

    @Override
    public void logUWS(LogLevel level, Object obj, String event, String message, Throwable error)
    {
        log(level, "UWS", event, null, message, error);
    }

}
