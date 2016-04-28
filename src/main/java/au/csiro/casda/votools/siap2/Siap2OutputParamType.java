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
 * The type of output parameters.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public enum Siap2OutputParamType
{
    /** The MAXREC parameter is allows the client to limit the number or records in the response */
    MAXREC,
    /** RESPONSEFORMAT parameter is output other formats, default format is VOTable */
    RESPONSEFORMAT
}
