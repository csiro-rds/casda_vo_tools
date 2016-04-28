package au.csiro.casda.votools.siap2;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for the allowable data product types
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public enum DataProductType
{
    /** the 2d image type */
    IMAGE("image"), 
    /** the 3d image type */
    CUBE("cube");

    private final String type;

    private DataProductType(String type)
    {
        this.type = type;
    }

    public String getDataProductType()
    {
        return type;
    }

    /**
     * Checks to see if a value exists
     * 
     * @param value
     *            the value to check
     * @return true if this value exists, else return false
     */
    public static boolean contains(String value)
    {

        for (DataProductType dpt : DataProductType.values())
        {
            if (dpt.getDataProductType().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * creates a list of values with correct punctuation.
     * 
     * @param and
     *            true for and list false for or list
     * @return the list is string format
     */
    public static String createList(boolean and)
    {
        StringBuffer list = new StringBuffer();
        List<DataProductType> types = Arrays.asList(DataProductType.values());

        for (int i = 0; i < DataProductType.values().length; i++)
        {
            String option = types.get(i).toString().toLowerCase();

            if (i == types.size() - 1)
            {
                list.append((and ? " and " : " or ") + option);
            }
            else if (i == 0)
            {
                list.append(option);
            }
            else
            {
                list.append(", " + option);
            }
        }
        return list.toString();
    }
}