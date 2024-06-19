package au.csiro.casda;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

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
 * Log4j2 Appender for use when testing logging.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public final class Log4JTestAppender implements Appender
{
    /**
     * Creates a Log4JTestAppender and configures the logging system so that it will be used for any au.csiro-rooted
     * logging events.
     * 
     * @return a Log4JTestAppender
     */
    public static Log4JTestAppender createAppender()
    {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        Log4JTestAppender appender = new Log4JTestAppender();
        LoggerConfig loggerConfig = config.getLoggerConfig("au.csiro");
        for(String key : loggerConfig.getAppenders().keySet())
        {
            loggerConfig.removeAppender(key);
        }
        loggerConfig.addAppender(appender, Level.INFO, null);
        return appender;
    }

    private List<LogEvent> events;

    private Log4JTestAppender()
    {
        this.events = new ArrayList<>();
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public boolean isStarted()
    {
        return true;
    }

    @Override
    public boolean isStopped()
    {
        return false;
    }

    @Override
    public void append(LogEvent event)
    {
        this.events.add(event);
    }

    @Override
    public String getName()
    {
        return "TestAppender";
    }

    @Override
    public Layout<? extends Serializable> getLayout()
    {
        return PatternLayout.createDefaultLayout();
    }

    @Override
    public boolean ignoreExceptions()
    {
        return false;
    }

    @Override
    public ErrorHandler getHandler()
    {
        return null;
    }

    @Override
    public void setHandler(ErrorHandler handler)
    {
    }

    /**
     * Verifies that this appender received a LogEvent with the given level and messageFragment.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     */
    public void verifyLogMessage(Level level, String messageFragment)
    {
        this.verifyLogMessage(level, containsString(messageFragment),
                (Matcher<Throwable>) sameInstance((Throwable) null));
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, messageFragment and exact Exception.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     * @param exception
     *            the Exception associated with the LogEvent
     */
    public void verifyLogMessage(Level level, String messageFragment, Throwable throwable)
    {
        this.verifyLogMessage(level, containsString(messageFragment), (Matcher<Throwable>) sameInstance(throwable));
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, a message matching the messageMatcgher and
     * the exact Exception.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageMatcher
     *            a matcher used to match the LogEvent's message
     * @param exception
     *            the Exception associated with the LogEvent
     */
    public void verifyLogMessage(Level level, Matcher<String> messageMatcher, Throwable throwable)
    {
        this.verifyLogMessage(level, messageMatcher, (Matcher<Throwable>) sameInstance(throwable));
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, messageFragment and exception whose class
     * is the given exceptionClass and whose message matches the exceptionMessageFragment.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     * @param exceptionClass
     *            the class of the Exception associated with the LogEvent
     * @param exceptionMessageFragment
     *            a fragment of the message of the Exception associated with the LogEvent
     */
    public void verifyLogMessage(Level level, String messageFragment, Class<? extends Exception> exceptionClass,
            String exceptionMessageFragment)
    {
        this.verifyLogMessage(level, messageFragment, exceptionClass, containsString(exceptionMessageFragment));
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, messageFragment and exception whose class
     * is the given exceptionClass and whose message is matched by the exceptionMessageMatcher.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     * @param exceptionClass
     *            the class of the Exception associated with the LogEvent
     * @param exceptionMessageMatcher
     *            a matcher used to check the exception message
     */
    public void verifyLogMessage(Level level, String messageFragment, final Class<? extends Exception> exceptionClass,
            final Matcher<String> exceptionMessageMatcher)
    {
        this.verifyLogMessage(level, containsString(messageFragment), exceptionClass, exceptionMessageMatcher);
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, with a message that matchers the given
     * messageMatcher and exception whose class is the given exceptionClass and whose message is matched by the
     * exceptionMessageMatcher.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     * @param exceptionClass
     *            the class of the Exception associated with the LogEvent
     * @param exceptionMessageMatcher
     *            a matcher used to check the exception message
     */
    public void verifyLogMessage(Level level, Matcher<String> messageMatcher,
            final Class<? extends Exception> exceptionClass, final Matcher<String> exceptionMessageMatcher)
    {
        this.verifyLogMessage(level, messageMatcher, new CustomTypeSafeMatcher<Throwable>(
                String.format("Throwable is %s with message", exceptionClass))
        {
            @Override
            public boolean matchesSafely(Throwable ex)
            {
                boolean exceptionClassSame = ex.getClass().equals(exceptionClass);
                boolean messageAsExpected = exceptionMessageMatcher.matches(((Throwable) ex).getMessage());
                return exceptionClassSame && messageAsExpected;
            }
            
            @Override
            public void describeMismatchSafely(Throwable ex, Description description)
            {
                if (!ex.getClass().equals(exceptionClass))
                {
                    description.appendText("was a ").appendText(ex.getClass().getName()).appendText(" (")
                            .appendValue(ex).appendText(")");
                }
                else if (!exceptionMessageMatcher.matches(((Throwable) ex).getMessage()))
                {
                    exceptionMessageMatcher.describeMismatch(ex, description);
                }
            }
        });
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, messageFragment and an exception that
     * matches the given matcher.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageFragment
     *            a fragment of the message in the LogEvent
     * @param exceptionMatcher
     *            a matcher used to match the Exception associated with the LogEvent
     */
    public <T extends Throwable> void verifyLogMessage(Level level, String messageFragment, Matcher<T> exceptionMatcher)
    {
        this.verifyLogMessage(level, containsString(messageFragment), exceptionMatcher);
    }

    /**
     * Verifies that this appender received a LogEvent with the given level, with a message that matches the given
     * messageMatcher, and an exception that matches the given exceptionMatcher.
     * 
     * @param level
     *            the Level of the LogEvent
     * @param messageMatcher
     *            a matcher used to match the LogEvent's message
     * @param exceptionMatcher
     *            a matcher used to match the Exception associated with the LogEvent
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> void verifyLogMessage(Level level, Matcher<String> messageMatcher,
            Matcher<T> exceptionMatcher)
    {
        LogEvent logEvent = this.events.remove(0);
        assertThat(logEvent, notNullValue());
        assertThat(logEvent.getLevel(), is(level));
        assertThat(logEvent.getMessage().getFormattedMessage(), messageMatcher);
        assertThat((T) logEvent.getThrown(), exceptionMatcher);
    }

    /**
     * Verifies that this appender receives no LogEvents.
     */
    public void verifyNoMessages()
    {
        assertThat(this.events, empty());
    }

    @Override
    public State getState()
    {
        return null;
    }

    @Override
    public void initialize()
    {
    }
}
