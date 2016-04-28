package au.csiro.casda.votools.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.BadSqlGrammarException;

import com.google.common.base.Objects;

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
 * Table configuration
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class TableConfig extends Options
{
    /** Description key */
    public static final String DESCRIPTION = "description";

    /** Description Long key */
    public static final String DESCRIPTION_LONG = "description_long";

    /** Utype key */
    public static final String UTYPE = "utype";

    /** Type key */
    public static final String TYPE = "type";

    /** SCS enabled key */
    public static final String SCS_ENABLED = "scs_enabled";

    /** Release required key */
    public static final String RELEASE_REQUIRED = "release_required";

    /** Params key */
    public static final String PARAMS = "params";

    /** Schema name key */
    public static final String SCHEMA_NAME = "schema_name";

    /** Table name key */
    public static final String TABLE_NAME = "table_name";

    /** TAP schema name key */
    public static final String TAP_SCHEMA_NAME = "tap.schema.name";

    /** TAP table name key */
    public static final String TAP_TABLE_NAME = "tap.table.name";

    /** Full db table name */
    private String fullDbTableName;

    /** Table columns */
    private Map<String, ColumnConfig> columns;

    /** Table index definitions */
    private Map<String, String> indexDefs;

    /** Constraints involving table fields */
    private Map<String, String> constraints;

    /** Primary keys involving table fields */
    private Map<String, String> keys;

    /** Map from index name to index config object */
    private Map<String, IndexConfig> indexConfigs;

    /** Map from constraint name to constraint config object */
    private Map<String, ConstraintConfig> constraintConfigs;

    /** Map from key name to key config object */
    private Map<String, KeyConfig> keyConfigs;

    private Configuration config;

    /**
     * Parameterless constructor
     */
    public TableConfig()
    {
        super();
        init();
    }

    /**
     * A constructor
     * 
     * @param fullDbTableName
     *            full table name in form schema.table
     */
    public TableConfig(String fullDbTableName)
    {
        this();
        this.fullDbTableName = fullDbTableName;
    }

    /**
     * A constructor for TAP DAO needs
     * 
     * @param fullDbTableName
     *            full db table name in form schema.table
     * @param dao
     *            DAO to use to fill in critical parts used by TAP DAO
     */
    public TableConfig(String fullDbTableName, ConfigurationDAO dao)
    {
        this(fullDbTableName);
        columns = dao.exportColumns(fullDbTableName);
        indexDefs = dao.exportIndexDefs(fullDbTableName);
        constraints = dao.exportConstraints(fullDbTableName, ConfigurationDAOImpl.FOREIGN_KEYS);
        keys = dao.exportConstraints(fullDbTableName, ConfigurationDAOImpl.PRIMARY_KEYS);
    }

    /**
     * Creates needed maps
     */
    public void init()
    {
        columns = new HashMap<String, ColumnConfig>();
        indexDefs = new HashMap<String, String>();
        constraints = new HashMap<String, String>();
        keys = new HashMap<String, String>();
        indexConfigs = new HashMap<String, IndexConfig>();
        constraintConfigs = new HashMap<String, ConstraintConfig>();
        keyConfigs = new HashMap<String, KeyConfig>();
    }

    /**
     * Check equivalence
     * 
     * @param object
     *            TableConfig to compare to
     * @return true if equivalent
     */
    public boolean equals(Object object)
    {
        return equalsXOptions(object) && super.equals(object);
    }

    /**
     * Check equivalence ignoring options
     * 
     * @param object
     *            TableConfig to compare to
     * @return true if equivalent
     */
    public boolean equalsXOptions(Object object)
    {
        if (!(object instanceof TableConfig))
        {
            return false;
        }
        TableConfig other = (TableConfig) object;
        boolean columnsEq = Objects.equal(this.columns, other.columns);
        boolean indexDefsEq = Objects.equal(this.indexDefs, other.indexDefs);
        boolean constraintsEq = Objects.equal(this.constraints, other.constraints);
        boolean keysEq = Objects.equal(this.keys, other.keys);
        return columnsEq && indexDefsEq && constraintsEq && keysEq ;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     * @return hash code
     */
    public int hashCode()
    {
        return 1 + super.hashCode();
    }

    /**
     * Exports database structure
     * 
     * @param config
     *            input configuration
     * @param fullDbTableName
     *            table name in table.name form
     * @throws ConfigurationException
     *             if there wer connection problems
     */
    public void export(Configuration config, String fullDbTableName) throws ConfigurationException
    {
        ConfigurationDAO dao = config.initDao();
        try
        {
            boolean tableExists = dao.updateTableFromTap(fullDbTableName, this);
            if (!tableExists)
            {
                return;
            }
        }
        catch (BadSqlGrammarException e) // TAP tables may not exist yet
        {
            String msg = e.getMessage();
            if (!msg.contains("relation") || !msg.contains("not exist"))
            {
                throw new ConfigurationException(e);
            }
            return;
        }
        columns = dao.exportColumns(fullDbTableName);
        indexDefs = dao.exportIndexDefs(fullDbTableName);
        constraints = dao.exportConstraints(fullDbTableName, ConfigurationDAOImpl.FOREIGN_KEYS);
        keys = dao.exportConstraints(fullDbTableName, ConfigurationDAOImpl.PRIMARY_KEYS);
        dao.updateColumnsFromTap(fullDbTableName, columns);

        addPlaceholder(DESCRIPTION);
        addPlaceholder(DESCRIPTION_LONG);
        addPlaceholder(UTYPE);
        addPlaceholder(SCS_ENABLED);
        addPlaceholder(RELEASE_REQUIRED);
        addPlaceholder(PARAMS);
        boolean usedByTap = config.isUsedByTap(fullDbTableName);
        boolean usedByScs = config.isUsedByScs(fullDbTableName);
        // Provide placeholders for all columns in the table
        for (ColumnConfig column : columns.values())
        {
            column.addPlaceholder(ColumnConfig.DESCRIPTION);
            column.addPlaceholder(ColumnConfig.UNIT);
            column.addPlaceholder(ColumnConfig.UCD);
            column.addPlaceholder(ColumnConfig.UTYPE);
            column.addPlaceholder(ColumnConfig.SIZE, "as integer>");
            column.addPlaceholder(ColumnConfig.PRINCIPAL, "as 0 or 1>");
            column.addPlaceholder(ColumnConfig.INDEXED, "as 0 or 1>");
            column.addPlaceholder(ColumnConfig.STD, "as 0 or 1>");
            column.addPlaceholder(ColumnConfig.ORDER, "as integer>");
            if (usedByScs)
            {
                column.addPlaceholder(ColumnConfig.SCS_VERBOSITY, "as integer from 1 to 3>");
            }
            if (usedByTap)
            {
                column.addPlaceholder(ColumnConfig.TAP_VISIBILITY, "as 0 or 1>");
            }
        }
    }

    /**
     * Add information from other table configuration columns options because everything else is being read from the
     * database.
     * 
     * @param other
     *            other table configuration
     */
    public void addOptions(TableConfig other)
    {
        if (other != null)
        {
            for (String key : columns.keySet())
            {
                ColumnConfig thisCc = columns.get(key);
                ColumnConfig otherCc = other.columns.get(key);
                thisCc.addOptions(otherCc);
            }
            super.addOptions(other);
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
        for (ColumnConfig column : columns.values())
        {
            column.stripPlaceholders();
        }
        super.stripPlaceholders();
    }

    /**
     * Identifies elements that are missing in this object compared to the passed TableConfig
     * 
     * @param tableConfig
     *            table configuration to compare to
     * @return a TableConfig object that contains found missing entries
     */
    public TableConfig missing(TableConfig tableConfig)
    {
        TableConfig missingList = new TableConfig();

        for (String column : tableConfig.columns.keySet())
        {
            if (!columns.containsKey(column))
            {
                missingList.columns.put(column, tableConfig.columns.get(column));
            }
        }
        for (String index : tableConfig.indexDefs.keySet())
        {
            if (!indexDefs.containsKey(index))
            {
                missingList.indexDefs.put(index, tableConfig.indexDefs.get(index));
            }
        }
        for (String key : tableConfig.constraints.keySet())
        {
            if (!constraints.containsKey(key))
            {
                missingList.constraints.put(key, tableConfig.constraints.get(key));
            }
        }
        for (String key : tableConfig.keys.keySet())
        {
            if (!keys.containsKey(key))
            {
                missingList.keys.put(key, tableConfig.keys.get(key));
            }
        }
        return missingList.columns.size() > 0 || missingList.indexDefs.size() > 0 || missingList.constraints.size() > 0
                || missingList.keys.size() > 0 ? missingList : null;
    }

    /**
     * Update table elements. Create missing ones. Delete ones that are missing in updates
     * 
     * @param updates
     *            table configuration object containing updates information
     * @throws ConfigurationException
     *             if the configuration does not allow dropping tables
     */
    public void update(TableConfig updates) throws ConfigurationException
    {
        for (String columnName : updates.columns.keySet())
        {
            if (!columns.containsKey(columnName))
            {
                updates.columns.get(columnName).create();
            }
            else
            {
                if (!updates.columns.get(columnName).equalsXOptions(columns.get(columnName)))
                {
                    columns.get(columnName).update(updates.columns.get(columnName));
                }
            }
        }
        for (String indexName : updates.indexConfigs.keySet())
        {
            if (!indexConfigs.containsKey(indexName))
            {
                updates.indexConfigs.get(indexName).create();
            }
            else
            {
                if (!updates.indexConfigs.get(indexName).equalsXOptions(indexConfigs.get(indexName)))
                {
                    updates.indexConfigs.get(indexName).update();
                }
            }
        }

        for (String constraintName : updates.constraintConfigs.keySet())
        {
            if (!constraintConfigs.containsKey(constraintName))
            {
                updates.constraintConfigs.get(constraintName).create();
            }
            else
            {
                if (!updates.constraintConfigs.get(constraintName)
                        .equalsXOptions(constraintConfigs.get(constraintName)))
                {
                    updates.constraintConfigs.get(constraintName).update();
                }
            }
        }

        for (String keyName : updates.keyConfigs.keySet())
        {
            if (!keyConfigs.containsKey(keyName))
            {
                updates.keyConfigs.get(keyName).create();
            }
            else
            {
                if (!updates.keyConfigs.get(keyName).equalsXOptions(keyConfigs.get(keyName)))
                {
                    updates.keyConfigs.get(keyName).update();
                }
            }
        }

        TableConfig toDelete = updates.missing(this);
        if (toDelete != null)
        {
            for (String key : toDelete.columns.keySet())
            {
                ColumnConfig cc = columns.get(key);
                cc.delete();
            }
            for (String key : toDelete.constraints.keySet())
            {
                ConstraintConfig cc = constraintConfigs.get(key);
                cc.delete();
            }
            for (String key : toDelete.keys.keySet())
            {
                KeyConfig cc = keyConfigs.get(key);
                cc.delete();
            }
            for (String key : toDelete.indexDefs.keySet())
            {
                IndexConfig ic = indexConfigs.get(key);
                ic.delete();
            }
        }
        // Check if the options are different
        if (!this.getOptions().equals(updates.getOptions()))
        {
            updates.config.initDao().updateTapTable(updates, false);
        }
    }

    /**
     * Create (this) new table in the database
     * 
     * @throws ConfigurationException
     *             of the change is not allowed or a DB problem occurred
     */
    public void create() throws ConfigurationException
    {
        // Create table and primary keys
        config.initDao().createTable(this);
        // Create indices
        for (IndexConfig idx : indexConfigs.values())
        {
            idx.create();
        }
        // Create constraints
        for (ConstraintConfig cfg : constraintConfigs.values())
        {
            cfg.create();
        }
    }

    /**
     * Get table name
     * 
     * @return table name part of full table name
     */
    public String gtFullDbTableName()
    {
        return fullDbTableName;
    }

    /**
     * Get short name
     * 
     * @return table name without schema
     */
    public String gtShortName()
    {
        return fullDbTableName.substring(fullDbTableName.indexOf('.') + 1);
    }

    /**
     * Get schema name
     * 
     * @return schema part of the table name
     */
    public String gtSchema()
    {
        return fullDbTableName.substring(0, fullDbTableName.indexOf('.'));
    }

    public Map<String, ColumnConfig> getColumns()
    {
        return columns;
    }

    public Map<String, String> getIndexDefs()
    {
        return indexDefs;
    }

    public Map<String, String> getConstraints()
    {
        return constraints;
    }

    public void setFullDbTableName(String name)
    {
        this.fullDbTableName = name;
    }

    public void setColumns(Map<String, ColumnConfig> columns)
    {
        this.columns = columns;
    }

    public void setIndexDefs(Map<String, String> indexDefs)
    {
        this.indexDefs = indexDefs;
    }

    public void setConstraints(Map<String, String> constraints)
    {
        this.constraints = constraints;
    }

    /**
     * Getter replacement, to make YAML parser ignore indexConfigs
     * 
     * @return indexConfigs
     */
    public Map<String, IndexConfig> gtIndexConfigs()
    {
        return indexConfigs;
    }

    public void setIndexConfigs(Map<String, IndexConfig> indexConfigs)
    {
        this.indexConfigs = indexConfigs;
    }

    /**
     * Getter replacement, to make YAML parser ignore constraintConfigs
     * 
     * @return constraintConfigs
     */
    public Map<String, ConstraintConfig> gtConstraintConfigs()
    {
        return constraintConfigs;
    }

    public void setConstraintConfigs(Map<String, ConstraintConfig> constraintConfigs)
    {
        this.constraintConfigs = constraintConfigs;
    }

    public Map<String, String> getKeys()
    {
        return keys;
    }

    public void setKeys(Map<String, String> keys)
    {
        this.keys = keys;
    }

    /**
     * Getter replacement, to make YAML parser ignore keyConfigs
     * 
     * @return keyConfigs
     */
    public Map<String, KeyConfig> gtKeyConfigs()
    {
        return keyConfigs;
    }

    public void setKeyConfigs(Map<String, KeyConfig> keyConfigs)
    {
        this.keyConfigs = keyConfigs;
    }

    /**
     * Checks that this table is empty (does not have any columns)
     * 
     * @return true if the table is empty
     */
    public boolean isEmpty()
    {
        return columns == null || columns.isEmpty();
    }

    /**
     * Returns type of the table, defaulting to "table"
     * 
     * @return table type
     */
    public String getType()
    {
        String type = get(TYPE);
        return type != null ? type : "table";
    }

    /**
     * Getter replacement, to make YAML parser ignore config
     * 
     * @return Configuration this table belongs to
     */
    public Configuration gtConfig()
    {
        return config;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    /**
     * Get TAP schema name of this table
     * 
     * @return TAP schema name of this table
     */
    public String gtTapSchemaName()
    {
        return get(TAP_SCHEMA_NAME, gtSchema());
    }

    /**
     * Get full TAP table name of this table
     * 
     * @return full TAP table name of this table
     */
    public String gtFullTapTableName()
    {
        return String.format("%s.%s", gtTapSchemaName(), gtTapTableName());
    }

    /**
     * Get TAP table name of this table
     * 
     * @return TAP schema name of this table
     */
    public String gtTapTableName()
    {
        return get(TAP_TABLE_NAME, gtShortName());
    }

}
