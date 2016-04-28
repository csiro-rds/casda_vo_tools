package au.csiro.casda.votools.siap2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Builder style class that allows an AQDL query to be built up for a single table. A new instance should be created for
 * each query.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class AdqlQueryBuilder
{
    private List<String> selectCriteria = new ArrayList<String>();
    private String tablename;
    private Integer maxRec = null;

    /**
     * Create a new AdqlQueryBuilder instance.
     * 
     * @param tablename
     *            The ADQL table name as used in queries.
     */
    public AdqlQueryBuilder(String tablename)
    {
        this.tablename = tablename;
    }

    @Override
    public String toString()
    {
        StringBuffer selectClause = new StringBuffer();
        for (String criterion : selectCriteria)
        {
            if (selectClause.length() > 0)
            {
                selectClause.append(" AND ");
            }
            selectClause.append("(");
            selectClause.append(criterion);
            selectClause.append(")");
        }

        String query = "SELECT"+ (maxRec == null ? "" : " TOP " + maxRec.toString()) +" * FROM " + tablename;
        if (selectClause.length() > 0)
        {
            query += " WHERE " + selectClause.toString();
        }
        return query;
    }

    /**
     * Convert a set of numeric field values into a select clause for the field pair and add it to the ADQL query being
     * built.
     * 
     * @param minColName
     *            The name of the column holding the minimum value.
     * @param maxColName
     *            The name of the column holding the maximum value.
     * @param criteria
     *            The set of parameters that have been supplied for the field.
     * @return The current AdqlQueryBuilder instance
     */
    public AdqlQueryBuilder withDoubleRange(String minColName, String maxColName, String[] criteria)
    {
        if (criteria == null || criteria.length == 0)
        {
            return this;
        }

        StringBuilder fieldSelect = new StringBuilder();
        for (String criterion : criteria)
        {
            if (StringUtils.isBlank(criterion))
            {
                continue;
            }
            criterion = StringUtils.trim(criterion);
            String template;
            String value = "";
            if (criterion.equals("/"))
            {
                // Neither em_min is not null AND em_max is not null
                template = "%s IS NOT NULL AND %s IS NOT NULL";
            }
            else if (criterion.indexOf("/") < 0)
            {
                // Exact only: em_min <= value AND em_max >= value
                value = criterion;
                template = "%1$s <= %3$s AND %2$s >= %3$s";
            }
            else if (criterion.endsWith("/"))
            {
                // Min: em_max >= min
                value = criterion.substring(0, criterion.length() - 1);
                template = "%2$s >= %3$s";
            }
            else if (criterion.startsWith("/"))
            {
                // Max: em_min <= min
                value = criterion.substring(1);
                template = "%1$s <= %3$s";
            }
            else
            {
                // Both: em_min <= max AND em_max >= min
                String[] range = criterion.split("/");
                template = "%1$s <= " + range[1] + " AND %2$s >= " + range[0];
            }

            appendFragment(fieldSelect, String.format(template, minColName, maxColName, value));
        }

        if (fieldSelect.length() > 0)
        {
            selectCriteria.add(fieldSelect.toString());
        }
        return this;
    }

    /**
     * Add a predefined select clause to the ADQL query being built.
     * 
     * @param selectClause
     *            The predefined select clause.
     * @return The current AdqlQueryBuilder instance
     */
    public AdqlQueryBuilder withSpecificClause(String selectClause)
    {
        if (StringUtils.isNotBlank(selectClause))
        {
            selectCriteria.add(selectClause);
        }

        return this;
    }

    /**
     * Fragments for the same field are appended together with an OR.
     * 
     * @param fieldSelect
     *            The selection criteria for the field.
     * @param fragment
     *            The fragment to be added.
     */
    private void appendFragment(StringBuilder fieldSelect, String fragment)
    {
        if (fieldSelect.length() > 0)
        {
            fieldSelect.append(" OR ");
        }
        fieldSelect.append("(");
        fieldSelect.append(fragment);
        fieldSelect.append(")");
    }

    /**
     * @param maxRec the maxrec values passed by the user
     */
    public void setMaxRec(String maxRec[])
    {
        if(maxRec != null && StringUtils.isNumeric(maxRec[0]))
        {
            this.maxRec = Integer.parseInt(maxRec[0]); 
        }
    }
}
