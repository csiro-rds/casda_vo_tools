package au.csiro.casda.logging;

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


import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 
 * Generates messages for all CasdaEventTypes with sample data.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class LogGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(LogGenerator.class);

    /**
     * Run through the list of known events, and log them.
     * 
     * @param args
     *            command line args
     */
    public static void main(String[] args)
    {
        Random generator = new Random();

        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings("CasdaCommons", "src/test/logLocation/log4j2.xml");
        
        loggingSettings.addGeneralLoggingSettings();

        while (true)
        {
            for (LogEvent event : LogEvent.values())
            {
                if (generator.nextInt(30) % 9 == 0)
                {

                    String message = CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event)
                            .addTimeTaken(generator.nextInt(5000))
                            .addCustomMessage("this is what happened for " + generator.nextInt(5000)).toString();
                    logger.info(message);
                }
            }

            for (CasdaCommonEvents event : CasdaCommonEvents.values())
            {
                MDC.put("user", event.name().replace("E", "User"));
                MDC.put("instanceid", event.name().replace("E", "101-"));

                List<Object> logArgs = generateArgs(event.getRequiredArgs());
                CasdaMessageBuilder<?> builder = CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event);
                for (Object logArg : logArgs)
                {
                    builder.add(logArg);
                }
                if (generator.nextInt(10) % 7 == 0)
                {
                    builder.addCustomMessage("extra comment information");
                }
                String eventMessage = builder.toString();

                if (generator.nextInt(10) % 9 == 0)
                {
                    switch (event.getLevel())
                    {
                    case INFO:
                        logger.info(eventMessage);
                        break;
                    case WARN:
                        logger.warn(eventMessage);
                        break;
                    case ERROR:
                        logger.error(eventMessage, new RuntimeException(event.name() + " sample exception"));
                        break;
                    default:
                        logger.debug(eventMessage);
                    }
                }

            }

            for (LogEvent event : LogEvent.values())
            {

                if (generator.nextInt(20) % 9 == 0)
                {
                    logger.info(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event)

                    .addTimeTaken(generator.nextInt(5000))
                            .addCustomMessage("this is what happened for " + generator.nextInt(5000)).toString());
                }

            }
            for (LogEvent event : LogEvent.values())
            {
                if (generator.nextInt(10) % 9 == 0)
                {
                    logger.info(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(event)

                    .addTimeTaken(generator.nextInt(5000))
                            .addCustomMessage("this is what happened for " + generator.nextInt(5000)).toString());
                }
            }

        }

    }

    /**
     * Generates random arguments that are required for the format strings of events.
     * 
     * @param requiredArgs
     *            the argument class types required by the formatter
     * @return a list of arguments of the required types
     */
    public static List<Object> generateArgs(Class<?>[] requiredArgs)
    {
        List<Object> args = new ArrayList<>();
        int count = 0;
        for (Class<?> requiredArg : requiredArgs)
        {
            if (int.class.equals(requiredArg))
            {
                args.add(12340 + count);
            }
            else
            {
                if (String.class.equals(requiredArg))
                {
                    args.add("string" + count);
                }
                else
                {
                    if (List.class.equals(requiredArg))
                    {
                        List<Path> pathList = new ArrayList<>();
                        for (int j = 0; j < 10 + count; j++)
                        {
                            pathList.add(new File("something" + j + ".txt").toPath());
                        }
                        args.add(pathList);
                    }
                    else
                    {
                        if (Date.class.equals(requiredArg))
                        {
                            args.add(new Date());
                        }
                        else
                        {
                            if (Path.class.equals(requiredArg))
                            {
                                args.add(new File("requestedFile" + count + ".xml").toPath());
                            }
                        }
                    }
                }
            }
            count++;
        }
        return args;
    }

}
