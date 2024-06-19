package au.csiro.casda.votools.siap2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import au.csiro.casda.votools.utils.Utils;

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
 * A processor for the spatial position SIAP parameter type. These may be either a single value or a range, as defined
 * in section 2.1 of the IVOA Simple Image Access Version 2.0 Recommendation. Multiple sets of values are also
 * supported. All values are in degrees.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class PositionParamProcessor implements SiapParamProcessor
{

    private static final String SIGNED_DECIMAL_NUMBER_PATTERN = "[\\+\\-]?\\d+(\\.\\d+)?";

    private static final String NEGATIVE_INFINTY = "-Inf";
    private static final String POSITIVE_INFINTY = "+Inf";

    private static final String OPTIONAL_MIN_NUMERIC_PATTERN =
            "(" + NEGATIVE_INFINTY + ")|(" + SIGNED_DECIMAL_NUMBER_PATTERN + ")";

    private static final String OPTIONAL_MAX_NUMERIC_PATTERN =
            "(\\" + POSITIVE_INFINTY + ")|(" + SIGNED_DECIMAL_NUMBER_PATTERN + ")";

    private static final String NUMERIC_RANGE_PATTERN =
            "(" + OPTIONAL_MIN_NUMERIC_PATTERN + ") +(" + OPTIONAL_MAX_NUMERIC_PATTERN + ")";

    /** CIRCLE takes a centre latitude and longitude and a radius. */
    private static final String CIRCLE_PATTERN = "CIRCLE( +" + SIGNED_DECIMAL_NUMBER_PATTERN + "){3}";

    /** RANGE takes a top left latitude and longitude and a bottom right latitude and longitude. */
    private static final String RANGE_PATTERN = "RANGE( +(" + NUMERIC_RANGE_PATTERN + ")){2}";

    /** POLYGON takes a top left latitude and longitude and a bottom right latitude and longitude. */
    private static final String POLYGON_PATTERN =
            "POLYGON( +" + SIGNED_DECIMAL_NUMBER_PATTERN + " +" + SIGNED_DECIMAL_NUMBER_PATTERN + "){3,}";

    /** The index of the first right ascension value in a range. */
    private static final int FIRST_RA_INDEX = 1;

    /** The index of the first declination value in a range. */
    private static final int FIRST_DEC_INDEX = 3;

    /** The index of the radius parameter in a CIRCLE position definition. */
    private static final int RADIUS_INDEX = 3;

    @Override
    public Siap2ParamType getSupportedParamType()
    {
        return Siap2ParamType.POSITION;
    }

    @Override
    public List<String> validate(String paramName, String[] values)
    {
        List<String> errorList = new ArrayList<String>();

        for (String param : values)
        {
            String testParam = StringUtils.trimToEmpty(param);
            if (testParam.matches(CIRCLE_PATTERN))
            {
                String[] parts = testParam.split(" ");
                String error = verifyLongitude(parts[1], false);
                if (StringUtils.isNotBlank(error))
                {
                    errorList.add(error);
                    continue;
                }
                error = verifyLatitude(parts[2], false);
                if (StringUtils.isNotBlank(error))
                {
                    errorList.add(error);
                    continue;
                }
                error = verifyRadius(parts[RADIUS_INDEX]);
                if (StringUtils.isNotBlank(error))
                {
                    errorList.add(error);
                    continue;
                }
            }
            else if (testParam.matches(RANGE_PATTERN))
            {
                String[] parts = testParam.split(" ");
                for (int i = FIRST_RA_INDEX; i <= FIRST_RA_INDEX + 1; i++)
                {
                    String error = verifyLongitude(parts[i], true);
                    if (StringUtils.isNotBlank(error))
                    {
                        errorList.add(error);
                        continue;
                    }
                }
                for (int i = FIRST_DEC_INDEX; i <= FIRST_DEC_INDEX + 1; i++)
                {
                    String error = verifyLatitude(parts[i], true);
                    if (StringUtils.isNotBlank(error))
                    {
                        errorList.add(error);
                        continue;
                    }
                }
            }
            else if (testParam.matches(POLYGON_PATTERN))
            {
                String[] parts = testParam.split(" ");
                for (int i = 1; i < parts.length; i += 2)
                {
                    String error = verifyLongitude(parts[i], false);
                    if (StringUtils.isNotBlank(error))
                    {
                        errorList.add(error);
                        continue;
                    }
                    error = verifyLatitude(parts[i + 1], false);
                    if (StringUtils.isNotBlank(error))
                    {
                        errorList.add(error);
                        continue;
                    }
                }
            }
            else
            {                
                errorList.add(String.format(USAGE_FAULT_MSG, "Invalid POS value "  + testParam));
                continue;
            }
        }
        return errorList;
    }

    /**
     * Validate the longitude (Right Ascension).
     * 
     * @param value
     *            The value (in degrees) to be validated.
     * @param optional
     *            Is a missing value (e.g. NaN) allowed?
     * @return An error message, or null if the value is valid.
     */
    private String verifyLongitude(String value, boolean optional)
    {
        if (optional && (NEGATIVE_INFINTY.equals(value) || POSITIVE_INFINTY.equals(value)))
        {
            return null;
        }

        final String errMsg = String.format(USAGE_FAULT_MSG, "Invalid longitude value. Valid range is [0,360]");
        final double maxRa = 360d;

        try
        {
            double ra = Double.parseDouble(value);
            if (ra < 0d || ra > maxRa)
            {
                return errMsg;
            }
        }
        catch (NumberFormatException e)
        {
            return errMsg;
        }
        return null;
    }

    /**
     * Validate the latitude (Declination).
     * 
     * @param value
     *            The value (in degrees) to be validated.
     * @param optional
     *            Is a missing value (e.g. NaN) allowed?
     * @return An error message, or null if the value is valid.
     */
    private String verifyLatitude(String value, boolean optional)
    {
        if (optional && (NEGATIVE_INFINTY.equals(value) || POSITIVE_INFINTY.equals(value)))
        {
            return null;
        }

        final String errMsg = String.format(USAGE_FAULT_MSG, "Invalid latitude value. Valid range is [-90,90]");
        final double minDec = -90d;
        final double maxDec = 90d;
        try
        {
            double dec = Double.parseDouble(value);
            if (dec < minDec || dec > maxDec)
            {
                return errMsg;
            }
        }
        catch (NumberFormatException e)
        {
            return errMsg;
        }
        return null;
    }

    /**
     * Validate the search radius.
     * 
     * @param value
     *            The value (in degrees) to be validated.
     * @return An error message, or null if the value is valid.
     */
    private String verifyRadius(String value)
    {
        final String errMsg = String.format(USAGE_FAULT_MSG, "Invalid radius value. Valid range is [0,10]");
        final double maxRadius = 10d;
        try
        {
            double radius = Double.parseDouble(value);
            if (radius < 0 || radius > maxRadius)
            {
                return errMsg;
            }
        }
        catch (NumberFormatException e)
        {
            return errMsg;
        }
        return null;
    }

    /**
     * Convert a set of numeric field values (all in degrees) into a select clause for the field pair and add it to the
     * ADQL query being built.
     * 
     * @param minColName
     *            The name of the column holding the minimum value.
     * @param maxColName
     *            The name of the column holding the maximum value.
     * @param criteria
     *            The set of parameters that have been supplied for the field.
     * @return The current AdqlQueryBuilder instance
     */
    @Override
    public String buildQuery(String minColName, String maxColName, String[] criteria)
    {
        if (criteria == null || criteria.length == 0)
        {
            return "";
        }

        StringBuilder fieldSelect = new StringBuilder();
        for (String crit : criteria)
        {
            if (StringUtils.isBlank(crit))
            {
                continue;
            }
            String criterion = StringUtils.trimToEmpty(crit);
            String[] criterionParts = criterion.split(" +");
            String value = "";

            if (criterion.matches(CIRCLE_PATTERN))
            {
                // All values are required, so we can just drop them in.
                final int decIndex = 2;
                value = String.format("INTERSECTS(CIRCLE('ICRS GEOCENTER', %s, %s, %s),s_region)=1", criterionParts[1],
                        criterionParts[decIndex], criterionParts[RADIUS_INDEX]);
            }
            else if (criterion.matches(RANGE_PATTERN))
            {
                // Need to replace missing values with the default bounds
                String[] defaultCoords = new String[] { "0", "360", "90", "-90" };
                for (int i = 0; i < defaultCoords.length; i++)
                {
                    if (NEGATIVE_INFINTY.equals(criterionParts[i + 1])
                            || POSITIVE_INFINTY.equals(criterionParts[i + 1]))
                    {
                        criterionParts[i + 1] = defaultCoords[i];
                    }
                }
                BigDecimal firstRa = new BigDecimal(criterionParts[FIRST_RA_INDEX]);
                BigDecimal secondRa = new BigDecimal(criterionParts[FIRST_RA_INDEX + 1]);
                final int halfDivisor = 2;
                final int coordPrecision = 6;
                BigDecimal centreRa =
                        firstRa.add(secondRa).divide(new BigDecimal(halfDivisor), coordPrecision, RoundingMode.HALF_UP);
                BigDecimal widthRa = firstRa.subtract(secondRa).abs();

                BigDecimal firstDec = new BigDecimal(criterionParts[FIRST_DEC_INDEX]);
                BigDecimal secondDec = new BigDecimal(criterionParts[FIRST_DEC_INDEX + 1]);
                BigDecimal centreDec = firstDec.add(secondDec).divide(new BigDecimal(halfDivisor), coordPrecision,
                        RoundingMode.HALF_UP);
                BigDecimal heightDec = firstDec.subtract(secondDec).abs();
                value = String.format("INTERSECTS(BOX('ICRS GEOCENTER', %s, %s, %s, %s),s_region)=1", centreRa,
                        centreDec, widthRa, heightDec);
            }
            else if (criterion.matches(POLYGON_PATTERN))
            {
                StringBuilder builder = new StringBuilder("INTERSECTS(POLYGON('ICRS GEOCENTER'");
                for (int i = 1; i < criterionParts.length; i++)
                {
                    builder.append(", ");
                    builder.append(criterionParts[i]);
                }
                builder.append("),s_region)=1");
                value = builder.toString();
            }

            Utils.appendFragment(fieldSelect, value);
        }

        return fieldSelect.toString();
    }

    /**
     * Build the formula for the distance of the image centre to the target position.
     * 
     * @param criteria
     *            The set of parameters that have been supplied for the field.
     * @return A distance formula, or an empty string if the POS is not a single CIRCLE criterion.
     */
    public String buildDistanceFunction(String[] criteria)
    {
        if (ArrayUtils.isEmpty(criteria) || criteria.length > 1 || !criteria[0].matches(CIRCLE_PATTERN))
        {
            // We only produce a distance when there is a single CIRCLE position criteria
            return "";
        }

        String[] criterionParts = StringUtils.trimToEmpty(criteria[0]).split(" +");
        final int decIndex = 2;
        String distance =
                String.format("DISTANCE(POINT('ICRS GEOCENTER',s_ra,s_dec),POINT('ICRS GEOCENTER',%s, %s))",
                        criterionParts[1], criterionParts[decIndex]);
        return distance;
    }
}
