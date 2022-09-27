package au.csiro.casda.votools.surveys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Transient;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.input.XmlStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Container class for SIAP1 Surveys (SiapSurvey.java)
 * 
 * Copyright 2022, CSIRO Australia All rights reserved.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Surveys")
public class SiapSurveys
{
    @XmlElement(name = "Survey", type = SiapSurvey.class)
    private List<SiapSurvey> siapSurveys;

    private static Logger logger = LoggerFactory.getLogger(SiapSurveys.class);

    /**
     * Default
     */
    public SiapSurveys()
    {
        this.siapSurveys = new ArrayList<>();
    }

    /**
     * Create surveys from Set of maps.
     * 
     * @param surveys
     *            The surveys.
     */
    public SiapSurveys(Collection<Map<String, String>> surveys)
    {
        this();
        if (surveys != null)
        {
            for (Map<String, String> m : surveys)
            {
                this.siapSurveys.add(new SiapSurvey(m));
            }
        }
    }

    public List<SiapSurvey> getSiapSurveys()
    {
        return siapSurveys;
    }

    /**
     * Set surveys.
     * 
     * @param siapSurveys
     *            The SIA1 surveys to set.
     */
    public void setSiapSurveys(List<SiapSurvey> siapSurveys)
    {
        this.siapSurveys = siapSurveys;
    }

    /**
     * Check if this Container for surveys has surveys.
     * 
     * @return True if surveys exist, false, otherwise.
     */
    public boolean hasSurveys()
    {
        if (this.siapSurveys == null)
        {
            return false;
        }
        return this.siapSurveys.size() > 0;
    }

    /**
     * Load Surveys from xml file.
     * 
     * @param file
     *            File location of the SIAP1 Surveys configuration xml
     * @return True if the operation succeeded, otherwise, false.
     */
    public boolean loadFromXmlConfig(File file)
    {
        JAXBContext context;

        if (!file.exists())
        {
            try
            {
                logger.warn("SIAP1 Surveys config surveys file does not exist:" + file.getCanonicalPath());
            }
            catch (IOException e)
            {
                logger.error("Unable to get canonical name for " + file, e);
            }
            return false;
        }

        try
        {
            logger.info("Reading SIAP1 Surveys config from " + file.getCanonicalPath());
            context = JAXBContext.newInstance(SiapSurveys.class);
            Unmarshaller um = context.createUnmarshaller();
            try (XmlStreamReader reader = new XmlStreamReader(file))
            {
                SiapSurveys siapSurveys = (SiapSurveys) um.unmarshal(reader);
                logger.info("Read surveys " + siapSurveys.getSiapSurveys().stream().map(s -> s.getName())
                        .collect(Collectors.joining(", ")));
                setSiapSurveys(siapSurveys.getSiapSurveys());
            }

            return true;
        }
        catch (Exception e)
        {
            logger.warn("Error reading SIAP1 Surveys from xml file: " + file, e);
        }
        return false;
    }

    /**
     * Retrieve the survey matching the supplied code.
     * 
     * @param code
     *            The code of the survey to be retrieved
     * @return The matching survey, or null if none could be found.
     */
    @Transient
    SiapSurvey getSurvey(String code)
    {
        if (hasSurveys())
        {
            for (SiapSurvey survey : siapSurveys)
            {
                if (survey.getCode().equals(code))
                {
                    return survey;
                }
            }
        }
        return null;

    }
}
