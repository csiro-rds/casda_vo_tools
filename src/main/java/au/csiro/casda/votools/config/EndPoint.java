package au.csiro.casda.votools.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

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
 * 
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
@Component
public class EndPoint extends Options
{
    /**
     * Type of end point
     * 
     */
    public static enum Type
    {
        /** Simple Cone Search protocol */
        SCS,

        /** Table Access Protocol */
        TAP,

        /** Simple Image Access Protocol */
        SIAP,

        /** Simple Spectral Access Protocol */
        SSAP,
        /** Placeholder */
        PROTOCOL_NAME
    }

    /** Type of this end pint */
    private Type type;

    /** Schemas and names of tables used by the endpoint in format schema_name.table_name */
    private Set<String> tables;

    private static EndPoint helpPoint = helpEndPoint();

    private Configuration config;

    /**
     * Parameterless constructor
     */
    public EndPoint()
    {
        super();
        tables = new HashSet<String>();
    }

    /**
     * A factory method for a template end point
     * 
     * @return EndPoint object with help information
     */
    public static EndPoint helpEndPoint()
    {
        EndPoint point = new EndPoint();
        point.tables.add("List of DB tables used by the service in form of <schema_name>.<table_name>");
        point.tables.add("my_schema.my_table1");
        point.tables.add("my_schema.my_table2");
        point.tables.add("...");
        return point;
    }

    /**
     * Adds missing endPoints and/or settings using values injected by Spring from properties files
     * 
     * @param config
     *            configuration object of the endpoints
     * @param registry
     *            configuration registry holding current configuration and properties values
     */
    static void addDefaults(Configuration config, ConfigurationRegistry registry)
    {
        EndPoint tap = config.getEndPoint("TAP") == null ? new EndPoint() : config.getEndPoint("TAP");
        EndPoint scs = config.getEndPoint("SCS") == null ? new EndPoint() : config.getEndPoint("SCS");
        tap.setConfig(config);
        scs.setConfig(config);
        config.getEndPoints().put("TAP", tap);
        config.getEndPoints().put("SCS", scs);
        tap.putDefault("tap.job.name.prefix", registry.getTapJobNamePrefix());
        tap.putDefault("tap.data.access.url", registry.getTapDataAccessUrl());
        tap.putDefault("tap.max.running.jobs", String.valueOf(registry.getTapMaxRunningJobs()));
        tap.putDefault("tap.max.records", String.valueOf(registry.getTapMaxRecords()));
        tap.putDefault("tap.async.base.url", registry.getTapAsyncBaseUrl());
        tap.putDefault("tap.async.description", registry.getTapAsyncDescription());
        tap.putDefault("tap.async.job.list.name", registry.getTapAsyncJobListName());
        tap.putDefault("tap.language.name", registry.getTapLanguageName());
        tap.putDefault("tap.language.version", registry.getTapLanguageVersion());
        tap.putDefault("tap.language.description", registry.getTapLanguageDescription());
        tap.putDefault("tap.output.format.mime", registry.getTapOutputFormatMime());
        tap.putDefault("tap.output.format.alias", registry.getTapOutputFormatAlias());
        tap.putDefault("tap.retention.period.default", String.valueOf(registry.getTapRetentionPeriodDefault()));
        tap.putDefault("tap.retention.period.hard", String.valueOf(registry.getTapRetentionPeriodHard()));
        tap.putDefault("tap.execution.duration.default", String.valueOf(registry.getTapExecutionDurationDefault()));
        tap.putDefault("tap.execution.duration.hard", String.valueOf(registry.getTapExecutionDurationHard()));
        tap.putDefault("tap.sync.timeout", String.valueOf(registry.getTapSyncTimeout()));
        tap.putDefault("tap.async.timeout", String.valueOf(registry.getTapAsyncTimeout()));
        tap.putDefault("tap.output.limit.hard", String.valueOf(registry.getTapOutputLimitHard()));
        tap.putDefault("log.timezone", registry.getLogTimezone());

        scs.putDefault("scs.output.format.mime", registry.getScsOutputFormatMime());
        scs.putDefault("scs.outputFormat.alias", registry.getScsOutputFormatAlias());
        scs.putDefault("scs.max.radius", String.valueOf(registry.getScsMaxRadius()));
        scs.putDefault("scs.max.records", String.valueOf(registry.getScsMaxRecords()));
        scs.putDefault("scs.test.ra", String.valueOf(registry.getScsTestRa()));
        scs.putDefault("scs.test.dec", String.valueOf(registry.getScsTestDec()));
        scs.putDefault("scs.test.schema", registry.getScsTestSchema());
        scs.putDefault("scs.test.catalog", registry.getScsTestCatalog());
        scs.putDefault("scs.test.verbose", String.valueOf(registry.getScsTestVerbose()));
        scs.putDefault("scs.test.extras", registry.getScsTestExtras());
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof EndPoint))
        {
            return false;
        }
        EndPoint other = (EndPoint) object;
        return super.equals(other) && type == other.type && SetUtils.isEqualSet(tables, other.tables);
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#hashCode()
     */
    @Override
    public int hashCode()
    {
        int code = 0;
        for (String table : tables)
        {
            code += table.hashCode();
        }
        return code;
    }

    /**
     * Export information about this endpoint from available sources.
     */
    public void export()
    {
        if (type == null || tables.isEmpty())
        {
            EndPoint help = helpEndPoint();
            if (type == null)
            {
                type = help.type;
            }
            if (tables == null)
            {
                tables = help.tables;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#stripPlaceholders()
     */
    @Override
    public void stripPlaceholders()
    {
        if (type != null && type.equals(helpPoint.type))
        {
            type = null;
        }
        if (tables != null && tables.equals(helpPoint.tables))
        {
            tables = new HashSet<String>();
        }
    }

    /**
     * Replacement for type getter to hide type from YAML parsers
     * 
     * @return type
     */
    public Type gtType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Set<String> getTables()
    {
        return tables;
    }

    public void setTables(Set<String> tables)
    {
        this.tables = tables;
    }

    /**
     * Add a table to the tables set
     * 
     * @param table
     *            table name
     */
    public void addTable(String table)
    {
        tables.add(table);
    }

    /**
     * Replacement for getter to hide from YAML parser
     * 
     * @return config Configuration object of this end point
     */
    public Configuration gtConfig()
    {
        return config;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

}
