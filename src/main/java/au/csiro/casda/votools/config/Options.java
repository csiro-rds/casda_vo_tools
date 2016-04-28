package au.csiro.casda.votools.config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import au.csiro.casda.votools.utils.Utils;

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
 * A map of named options with convenience functions for easy translation of options into non-String data types. Could
 * have subclassed a Map, but this breaks YAML parsers.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class Options
{
    /** Default placeholder text */
    public static final String DEFAULT_PLACEHOLDER = "<Please provide value>";

    /** All placeholders must start with this prefix */
    public static final String PLACEHOLDER_START = DEFAULT_PLACEHOLDER.substring(0, DEFAULT_PLACEHOLDER.length() - 1);

    /** Options container */
    private Map<String, String> options;

    /**
     * Parameterless constructor
     */
    public Options()
    {
        options = new LinkedHashMap<String, String>();
    }

    /**
     * Check equivalence
     * 
     * @param other
     *            Options to compare to
     * @return true if equivalent
     */
    public boolean equals(Object other)
    {
        Options opts = (Options) other;
        if (options.size() != opts.options.size())
        {
            return false;
        }
        for (String key : options.keySet())
        {
            String valueThis = options.get(key);
            String valueThat = opts.options.get(key);
            if (!StringUtils.equals(valueThis, valueThat))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     * @return hash code
     */
    public int hashCode()
    {
        return options.hashCode();
    }

    /**
     * Adds other options to this one
     * 
     * @param other
     *            other Options object
     */
    public void addOptions(Options other)
    {
        if (other != null)
        {
            for (String key : other.options.keySet())
            {
                if (options.get(key) == null)
                {
                    options.put(key, other.options.get(key));
                }

            }
        }
    }

    /**
     * Get String value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return String value or defaultValue if not found
     */
    public String get(String key, String defaultValue)
    {
        String val = options.get(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Get String value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return String value or null if not found
     */
    public String get(String key)
    {
        return get(key, null);
    }

    /**
     * Get non-null String value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return String value or "null" if not found
     */
    public String getStringNot(String key)
    {
        String value = options.get(key);
        return value == null ? "null" : value;
    }

    /**
     * Get SQL ready String value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return SQL encoded and enclosed in '' String value or "null" if not found
     */
    public String sql(String key)
    {
        return Utils.sql(options.get(key));
    }

    /**
     * Get int value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return int value or defaultValue if not found
     */
    public int getInt(String key, int defaultValue)
    {
        String val = options.get(key);
        return val == null ? defaultValue : Integer.parseInt(val);
    }

    /**
     * Get int value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return int value or -1 if not found
     */
    public int getInt(String key)
    {
        return getInt(key, -1);
    }

    /**
     * Get long value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return long value or defaultValue if not found
     */
    public long getLong(String key, long defaultValue)
    {
        String val = options.get(key);
        return val == null ? defaultValue : Long.parseLong(val);
    }

    /**
     * Get long value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return long value or null if not found
     */
    public long getLong(String key)
    {
        return getLong(key, -1L);
    }

    /**
     * Get double value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return double value or defaultValue if not found
     */
    public double getDouble(String key, double defaultValue)
    {
        String val = options.get(key);
        return val == null ? defaultValue : Double.parseDouble(val);
    }

    /**
     * Get double value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return double value or null if not found
     */
    public double getDouble(String key)
    {
        return getDouble(key, -1.);
    }

    /**
     * Get float value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return float value or defaultValue if not found
     */
    public float getFloat(String key, float defaultValue)
    {
        String val = options.get(key);
        return val == null ? defaultValue : Float.parseFloat(val);
    }

    /**
     * Get float value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return float value or null if not found
     */
    public float getFloat(String key)
    {
        return getFloat(key, -1f);
    }

    /**
     * Get boolean value of option with given key
     * 
     * @param key
     *            the key of the option
     * @param defaultValue
     *            the value to return if not found
     * @return boolean value or defaultValue if not found
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        String val = options.get(key);
        return val == null ? defaultValue : val.trim().equalsIgnoreCase("true") || val.trim().equalsIgnoreCase("on")
                || val.trim().equalsIgnoreCase("1") || val.trim().equalsIgnoreCase("yes");
    }

    /**
     * Get boolean value of option with given key
     * 
     * @param key
     *            the key of the option
     * @return int value or null if not found
     */
    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }
    
    /**
     * Gets a list of strings for the given key, ignores spaces and assumes the list is comma delimited.
     * 
     * @param key
     *            the key of the option
     * @return the list of values or an empty list if not found.
     */
    public List<String> getList(String key)
    {
        return Arrays.asList(get(key, "").replace(" ", "").split(","));
    }

    /**
     * Put value, key pair
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void put(String key, String value)
    {
        options.put(key, value);
    }

    /**
     * Put default key, value pair if there is no such key
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void putDefault(String key, String value)
    {
        if (options.get(key) == null)
        {
            options.put(key, value);
        }
    }

    public Map<String, String> getOptions()
    {
        return options;
    }

    public void setOptions(Map<String, String> options)
    {
        this.options = options;
    }

    /**
     * Add placeholder text to this option
     * 
     * @param key
     *            option to add placeholder to
     * @param placeholder
     *            placeholder text to add with standard prefix, or null for default text
     */
    public void addPlaceholder(String key, String placeholder)
    {
        if (options.get(key) == null)
        {
            String value = placeholder == null ? DEFAULT_PLACEHOLDER : PLACEHOLDER_START + " " + placeholder;
            options.put(key, value);
        }
    }

    /**
     * Add custom placeholder text to this option. Use with care because it does not insert the standard prefix and thus
     * the placeholder won't be recognised as such when stripping placeholders and will be missed.
     * 
     * @param key
     *            option to add placeholder to
     * @param placeholder
     *            placeholder text to add or null for default text
     */
    public void addCustomPlaceholder(String key, String placeholder)
    {
        if (options.get(key) == null)
        {
            options.put(key, placeholder);
        }
    }

    /**
     * Add default placeholder text to this option
     * 
     * @param key
     *            option to add placeholder to
     */
    public void addPlaceholder(String key)
    {
        addPlaceholder(key, null);
    }

    /**
     * Detect and remove options that contain placeholders as their values
     */
    public void stripPlaceholders()
    {
        for (Iterator<Map.Entry<String, String>> it = options.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, String> entry = it.next();
            if (entry.getValue() != null && entry.getValue().startsWith(PLACEHOLDER_START))
            {
                it.remove();
            }
        }
    }

}
