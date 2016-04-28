package au.csiro.casda.votools.siap2;

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
 * The supported SIA v2 parameters. Each parameter is mapped to its type.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public enum Siap2Param
{
    /** The POS parameter defines the positional region(s) to be searched for data. */
    POS(Siap2ParamType.POSITION), 
    
    /** The BAND parameter defines the energy interval(s) to be searched for data. */
    BAND(Siap2ParamType.NUMERIC, "em_min", "em_max"),
    
    /** The FOV parameter defines the range(s) of field of view (size) to be searched for data */
    FOV(Siap2ParamType.NUMERIC, "s_fov", "s_fov"),
    
    /** The TIME parameter defines the time interval(s) to be searched for data */
    TIME(Siap2ParamType.DATE, "t_min", "t_max"),
    
    /** The POL parameter defines the polarization state(s) to be searched for matching data.  */
    POL(Siap2ParamType.STATE, "pol_states"),
    
    /** The SPATRES parameter defines the range(s) of spatial resolution to be searched for data. */
    SPATRES(Siap2ParamType.NUMERIC, "s_resolution", "s_resolution"),
    
    /** The EXPTIME parameter defines the range(s) of exposure times to be searched for data.  */
    EXPTIME(Siap2ParamType.NUMERIC, "t_exptime", "t_exptime"),
    
    /** The ID parameter is a string-valued parameter that specifies the identifier of dataset(s).  */
    ID(Siap2ParamType.TEXT, "obs_publisher_did"),
    
    /** The COLLECTION parameter is a string-valued parameter that specifies the name of the data collection.  */
    COLLECTION(Siap2ParamType.TEXT, "obs_collection"),
    
    /** The FACILITY parameter is a string-valued parameter that specifies the name of the facility 
     * (usually telescope) where the data was acquired. 
     **/
    FACILITY(Siap2ParamType.TEXT, "facility_name"),
    
    /** The INSTRUMENT parameter is a string-valued parameter that specifies the
     * name of the instrument with which the data was acquired. 
     **/
    INSTRUMENT(Siap2ParamType.TEXT, "instrument_name"),
    
    /** The DPTYPE parameter is a string-valued parameter that specifies the type of data. **/
    DPTYPE(Siap2ParamType.TEXT, "dataproduct_type"),
    
    /** The CALIB parameter is a integer-valued parameter that specifies the calibration level of the data. **/
    CALIB(Siap2ParamType.NUMERIC, "calib_level", "calib_level"),
    
    /** The TARGET parameter is a string-valued parameter that specifies the name of 
     * the target (e.g. the intention of the original science program or observation). 
     **/
    TARGET(Siap2ParamType.TEXT, "target_name"),
    
    /** The TIMERES parameter define the range(s) of temporal resolution to be searched for data. **/
    TIMERES(Siap2ParamType.NUMERIC, "t_resolution", "t_resolution"),
    
    /** The SPECRP parameter define the range(s) of spectral resolving power to be searched for data. **/
    SPECRP(Siap2ParamType.NUMERIC, "em_res_power", "em_res_power"),
    
    /** The FORMAT parameter specifies the dataproduct file format(s) **/
    FORMAT(Siap2ParamType.TEXT, "access_format"),
    
    /** The retired REQUEST parameter - it was removed in the SIA2 spec process so we don't process this, but we 
     * should be lenient and ignore it 
     */
    REQUEST(Siap2ParamType.IGNORED, "");

    /** The type of this parameter. This defines the formats accepted and how it is processed. */
    private final Siap2ParamType paramType;
    private String[] fields;

    private Siap2Param(Siap2ParamType paramType, String... fields)
    {
        this.paramType = paramType;
        this.fields = fields;
    }

    public Siap2ParamType getParamType()
    {
        return paramType;
    }

    /**
     * Retrieve the name of the field at the supplied index. 
     * @param fieldIndex The 0 based index of the field.
     * @return The field name, or null if no field exists at that index.
     */
    public String getField(int fieldIndex)
    {
        if (fieldIndex < 0 || fieldIndex>= fields.length)
        {
            return null;
        }
        return fields[fieldIndex];
    }
}
