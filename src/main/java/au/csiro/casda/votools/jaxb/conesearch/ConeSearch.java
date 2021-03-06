//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.10 at 11:54:14 AM EST 
//


package au.csiro.casda.votools.jaxb.conesearch;

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


import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             The capabilities of a Cone Search implementation.  
 *          
 * 
 * <p>Java class for ConeSearch complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConeSearch">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/ConeSearch/v1.0}CSCapRestriction">
 *       &lt;sequence>
 *         &lt;element name="maxSR" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="maxRecords" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="verbosity" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="testQuery" type="{http://www.ivoa.net/xml/ConeSearch/v1.0}Query" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConeSearch", propOrder = {
    "maxSR",
    "maxRecords",
    "verbosity",
    "testQuery"
})
public class ConeSearch
    extends CSCapRestriction
{

    protected Float maxSR;
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger maxRecords;
    protected boolean verbosity;
    protected Query testQuery;

    /**
     * Gets the value of the maxSR property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMaxSR() {
        return maxSR;
    }

    /**
     * Sets the value of the maxSR property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMaxSR(Float value) {
        this.maxSR = value;
    }

    /**
     * Gets the value of the maxRecords property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxRecords() {
        return maxRecords;
    }

    /**
     * Sets the value of the maxRecords property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxRecords(BigInteger value) {
        this.maxRecords = value;
    }

    /**
     * Gets the value of the verbosity property.
     * 
     */
    public boolean isVerbosity() {
        return verbosity;
    }

    /**
     * Sets the value of the verbosity property.
     * 
     */
    public void setVerbosity(boolean value) {
        this.verbosity = value;
    }

    /**
     * Gets the value of the testQuery property.
     * 
     * @return
     *     possible object is
     *     {@link Query }
     *     
     */
    public Query getTestQuery() {
        return testQuery;
    }

    /**
     * Sets the value of the testQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link Query }
     *     
     */
    public void setTestQuery(Query value) {
        this.testQuery = value;
    }

}
