package au.csiro.casda.votools.scs;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import au.csiro.casda.votools.jpa.TapColumn;
import au.csiro.casda.votools.jpa.TapTable;

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
 * A container object for the cone search configuration for a database table.
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 */
public class ConeSearchTable
{
    /**
     * A cone search verbosity level, which determines how many columns are returned. The lower the verbosity, the fewer
     * fields will be in the query response.
     */
    public enum Verbosity
    {
        /** Minimum verbosity. */
        LEVEL_1("1"), 
        /** Moderate verbosity. */
        LEVEL_2("2"), 
        /** Maximum verbosity. */
        LEVEL_3("3");
        
        private String key;

        private Verbosity(String key)
        {
            this.key = key;
        }
        
        /**
         * Find the verbosity level matching the verb parameter value (1, 2, or 3). LEVEL_2 will be returned if none
         * match.
         * 
         * @param verbParam
         *            The verb parameter value, expected to be a single ascii number.
         * @return The matching verbosity, or the default of LEVEL_2.
         */
        public static Verbosity findLevelForKey(String verbParam)
        {
            for (Verbosity verb : Verbosity.values())
            {
                if (verb.key.equals(verbParam))
                {
                    return verb;
                }
            }
            
            return LEVEL_2;
        }
    }
    
    private final TapTable table;
    
    private Map<Verbosity, Map<String, Integer>> selectColumns;

    private TapColumn idColumn;

    private TapColumn raColumn;

    private TapColumn decColumn;
    
    private final Map<String, String> votableFieldMap;
    
    /**
     * Create a new ConeSearchTable for a specified table.
     * @param table The table to be held.
     */
    public ConeSearchTable(TapTable table)
    {
        this.table = table;
        selectColumns = new HashMap<>();
        votableFieldMap = new HashMap<>();
    }

    /**
     * Add a new field to the list of fields output at the particular verbosity.
     * 
     * @param verbosity
     *            The verbosity to be updated.
     * @param column
     *            The database column name.
     * @param columnOrder
     *            The database column order.
     */
    public void addColumn(Verbosity verbosity, String column, int columnOrder)
    {
        Map<String, Integer> colList = selectColumns.get(verbosity);
        if (colList == null)
        {
            colList = new LinkedHashMap<String, Integer>();
            selectColumns.put(verbosity, colList);
        }
        colList.put(column, columnOrder);
    }

    /**
     * Add a new field to the map of VOTable field definitions. 
     * @param key The lookup key, normally tablename|columnname
     * @param fieldDef The XML field definition for the field.
     */
    public void putVoTableColumnDef(String key, String fieldDef)
    {
        getVotableFieldMap().put(key, fieldDef);
    }

    public Map<String, String> getVotableFieldMap()
    {
        return votableFieldMap;
    }

    /**
     * Retrieve the select fields (comma delimited list of database fields) for a specific verbosity level for this
     * table.
     * 
     * @param verbosity
     *            The verbosity level required.
     * @return The select fields.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String getSelectFields(Verbosity verbosity)
    {
        Map<String, Integer> sortedMap = new TreeMap();
        EnumSet.range(Verbosity.LEVEL_1, verbosity).stream().forEach(verb -> {
            Map<String, Integer> colList = selectColumns.get(verb);
            if (MapUtils.isNotEmpty(colList))
            {
                sortedMap.putAll(colList);
            }
        });
        return sortedMap.entrySet().stream().sorted(Map.Entry.<String, Integer> comparingByValue())
                .collect(Collectors.mapping(Map.Entry::getKey, Collectors.joining(",")));
    }

    public TapTable getTable()
    {
        return table;
    }

    public void setIdColumn(TapColumn idColumn)
    {
        this.idColumn = idColumn;
        
    }

    public TapColumn getIdColumn()
    {
        return idColumn;
    }

    public TapColumn getRaColumn()
    {
        return raColumn;
    }

    public void setRaColumn(TapColumn raColumn)
    {
        this.raColumn = raColumn;
    }

    public TapColumn getDecColumn()
    {
        return decColumn;
    }

    public void setDecColumn(TapColumn decColumn)
    {
        this.decColumn = decColumn;
    }
}
