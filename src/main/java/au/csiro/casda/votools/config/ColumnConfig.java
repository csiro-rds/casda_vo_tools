package au.csiro.casda.votools.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

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
 * Column configuration
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class ColumnConfig extends Options
{
    /** name of the name field */
    public static final String NAME = "name";
    /** name of the database column name field */
    public static final String DB_COLUMN_NAME = "db_column_name";
    /** name of the size field */
    public static final String SIZE = "size";
    /** name of the description field */
    public static final String DESCRIPTION = "description";
    /** name of the unit field */
    public static final String UNIT = "unit";
    /** name of the ucd field */
    public static final String UCD = "ucd";
    /** name of the utype field */
    public static final String UTYPE = "utype";
    /** name of the principal field */
    public static final String PRINCIPAL = "principal";
    /** name of the indexed field */
    public static final String INDEXED = "indexed";
    /** name of the std field */
    public static final String STD = "std";
    /** name of the SCS verbosity field */
    public static final String SCS_VERBOSITY = "scs_verbosity";
    /** name of the column order field */
    public static final String ORDER = "column_order";
    /** name of the TAP visibility field */
    public static final String TAP_VISIBILITY = "tap_visibility";

    /** column name, normally null, used as a temporary container */
    private String name;
    
    /** real db column name that is used in sql */
    private String dbColumnName;

    /** column type definition */
    private String type;

    /** default value or null */
    private String defaultvalue;

    /** 'true' if not null */
    private String notnull;

    /** 'true' if unique key */
    private String unique;

    /** constraints where this field is source field */
    private Set<ConstraintConfig> constraintsOut;

    /** constraints where this field is destination field */
    private Set<ConstraintConfig> constraintsIn;

    /** indices indexing this field */
    private Set<IndexConfig> indices;

    /** table this column belongs to */
    private TableConfig table;

    /**
     * Parameterless constructor
     */
    public ColumnConfig()
    {
        super();
        constraintsOut = new HashSet<ConstraintConfig>();
        constraintsIn = new HashSet<ConstraintConfig>();
        indices = new HashSet<IndexConfig>();
    }

    /**
     * Equivalence test
     * 
     * @param object
     *            object to compare to
     * @return true if they are equivalent, else false
     */
    @Override
    public boolean equals(Object object)
    {
        return equalsXOptions(object) && super.equals(object);
    }

    /**
     * Equivalence test, ignoring options
     * 
     * @param object
     *            object to compare to
     * @return true if they are equivalent, else false
     */
    public boolean equalsXOptions(Object object)
    {
        if (!(object instanceof ColumnConfig))
        {
            return false;
        }
        ColumnConfig other = (ColumnConfig) object;
        boolean nameEq = StringUtils.equals(name, other.name);
        boolean typeEq = StringUtils.equals(type, other.type);
        boolean defaultvalueEq = StringUtils.equals(defaultvalue, other.defaultvalue);
        boolean notnullEq = StringUtils.equals(notnull, other.notnull);
        boolean uniqueEq = StringUtils.equals(unique, other.unique);
        boolean constraintsOutEq = SetUtils.isEqualSet(constraintsOut, other.constraintsOut);
        boolean constraintsInEq = SetUtils.isEqualSet(constraintsIn, other.constraintsIn);
        boolean indicesEq = SetUtils.isEqualSet(indices, other.indices);

        return nameEq && typeEq && defaultvalueEq && notnullEq && uniqueEq && constraintsOutEq && constraintsInEq
                && indicesEq;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.votools.config.Options#hashCode()
     */
    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();
        hashCode = name == null ? hashCode : hashCode + name.hashCode();
        hashCode = dbColumnName == null ? hashCode : hashCode + dbColumnName.hashCode();
        hashCode = type == null ? hashCode : hashCode + type.hashCode();
        hashCode = defaultvalue == null ? hashCode : hashCode + defaultvalue.hashCode();
        hashCode = notnull == null ? hashCode : hashCode + notnull.hashCode();
        hashCode = unique == null ? hashCode : hashCode + unique.hashCode();
        hashCode = constraintsOut == null ? hashCode : hashCode + constraintsOut.hashCode();
        hashCode = constraintsIn == null ? hashCode : hashCode + constraintsIn.hashCode();
        hashCode = indices == null ? hashCode : hashCode + indices.hashCode();
        return hashCode;
    }

    /**
     * Drops this column
     * 
     * @throws ConfigurationException
     *             if configuration does not allow to drop a column
     */
    public void delete() throws ConfigurationException
    {
        this.gtTable().gtConfig().initDao().dropColumn(this);
    }

    /**
     * Creates this column
     * 
     * @throws ConfigurationException
     *             if change is not allowed
     */
    public void create() throws ConfigurationException
    {
        this.gtTable().gtConfig().initDao().createColumn(this);
    }

    /**
     * Updates this column in this table
     * 
     * @param updated
     *            updated column configuration
     * @throws ConfigurationException
     *             if dropping column is needed but not allowed
     */
    public void update(ColumnConfig updated) throws ConfigurationException
    {
        this.gtTable().gtConfig().initDao().updateColumn(this, updated);
    }

    /**
     * Replaces getName getter to make the name ignored when exporting to YAML.
     * 
     * @return column name
     */
    public String gtName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getDefaultvalue()
    {
        return defaultvalue;
    }

    public void setDefaultvalue(String defaultvalue)
    {
        this.defaultvalue = defaultvalue;
    }

    public String getNotnull()
    {
        return notnull;
    }

    public void setNotnull(String notnull)
    {
        this.notnull = notnull;
    }

    public String getUnique()
    {
        return unique;
    }

    public void setUnique(String unique)
    {
        this.unique = unique;
    }

    /**
     * Replaces getConstraintsOut to make constraintsOut ignored when exporting to YAML.
     * 
     * @return constraintsOut
     */
    public Set<ConstraintConfig> gtConstraintsOut()
    {
        return constraintsOut;
    }

    public void setConstraintsOut(Set<ConstraintConfig> constraintsOut)
    {
        this.constraintsOut = constraintsOut;
    }

    /**
     * Replaces getConstraintsIn to make constraintsIn ignored when exporting to YAML.
     * 
     * @return constraintsIn
     */
    public Set<ConstraintConfig> gtConstraintsIn()
    {
        return constraintsIn;
    }

    public void setConstraintsIn(Set<ConstraintConfig> constraintsIn)
    {
        this.constraintsIn = constraintsIn;
    }

    /**
     * Replaces getIndices to make indices ignored when exporting to YAML.
     * 
     * @return indices
     */
    public Set<IndexConfig> gtIndices()
    {
        return indices;
    }

    public void setIndices(Set<IndexConfig> indices)
    {
        this.indices = indices;
    }

    /**
     * Replaces getTable to make table ignored when exporting to YAML.
     * 
     * @return table
     */
    public TableConfig gtTable()
    {
        return table;
    }

    public void setTable(TableConfig table)
    {
        this.table = table;
    }

	public String getDbColumnName() 
	{
		return dbColumnName;
	}

	public void setDbColumnName(String dbColumnName) 
	{
		this.dbColumnName = dbColumnName;
	}

}
