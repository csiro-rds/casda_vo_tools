package au.csiro.casda.votools.config;

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
 * Base class for configuration aware classes Should be an interface, but this does not work with Spring.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public abstract class Configurable
{

    /**
     * Configuration is provided via this call. Configuration aware objects must perform actions required to change
     * their configuration related parameters if necessary.
     * 
     * @param config
     *            configuration object
     */
    abstract public void setConfiguration(Configuration config);

    /**
     * Some configurable objects need to use other configurable objects to change configuration, but those may not have
     * been configured at the time. In this case their isReady() method must return false.
     * 
     * @return false if this object is not ready to perform its functions yet due to no valid configuration or being in
     *         process of configuration change and waiting for other objects.
     * @throws ConfigurationException
     *             if configuration/initialisation actions triggered by this call have failed.
     */
    abstract public boolean isReady() throws ConfigurationException;

    /**
     * Invalidate old configuration. To avoid a situation when in process of configuration switch some objects use old
     * and some use new configuration.
     * 
     */
    abstract public void invalidate();

}
