package au.csiro.casda.votools.surveys;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * CSIRO VO Tools
 * Copyright (C) 2010 - 2022 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 *
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 */

/**
 * Survey model used to encapsulate a single SIA1 accessible survey. Consumed by surveys.jsp
 * 
 * Copyright 2022, CSIRO Australia All rights reserved.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Survey")
public class SiapSurvey
{

    private String id;

    @XmlElement(name = "Code")
    private String code;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "WhereClause")
    private String whereClause;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Endpoint")
    private String endpoint;

    @XmlElement(name = "Group")
    private String group;

    /**
     * Default constructor.
     */
    public SiapSurvey()
    {
    }

    /**
     * Build survey from map.
     * 
     * @param survey
     *            The Map of survey properties and values.
     */
    public SiapSurvey(Map<String, String> survey)
    {
        this();
        if (survey != null)
        {
            this.code = survey.get(SiapSurveysService.SurveyKeys.CODE.getKey());
            this.name = survey.get(SiapSurveysService.SurveyKeys.NAME.getKey());
            this.whereClause = survey.get(SiapSurveysService.SurveyKeys.WHERE_CLAUSE.getKey());
            this.description = survey.get(SiapSurveysService.SurveyKeys.DESCRIPTION.getKey());
            this.group = survey.get(SiapSurveysService.SurveyKeys.GROUP.getKey());
        }
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getWhereClause()
    {
        return whereClause;
    }

    public void setWhereClause(String whereClause)
    {
        this.whereClause = whereClause;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }
}
