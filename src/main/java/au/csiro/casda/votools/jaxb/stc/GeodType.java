//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.12 at 12:39:04 PM EST 
//


package au.csiro.casda.votools.jaxb.stc;

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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * The Geodetic reference frame; semi-major axis and inverse flattening may be provided to define the reference spheroid; the default is the IAU 1976 reference spheroid
 * 
 * <p>Java class for geodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="geodType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}icrsType">
 *       &lt;attribute name="radius" type="{http://www.w3.org/2001/XMLSchema}double" default="6378140" />
 *       &lt;attribute name="inv_flattening" type="{http://www.w3.org/2001/XMLSchema}double" default="298.257" />
 *       &lt;attribute name="unit" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}posUnitType" default="m" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geodType")
public class GeodType
    extends IcrsType
{

    @XmlAttribute(name = "radius")
    protected Double radius;
    @XmlAttribute(name = "inv_flattening")
    protected Double invFlattening;
    @XmlAttribute(name = "unit")
    protected String unit;

    /**
     * Gets the value of the radius property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getRadius() {
        if (radius == null) {
            return  6378140.0D;
        } else {
            return radius;
        }
    }

    /**
     * Sets the value of the radius property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRadius(Double value) {
        this.radius = value;
    }

    /**
     * Gets the value of the invFlattening property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getInvFlattening() {
        if (invFlattening == null) {
            return  298.257D;
        } else {
            return invFlattening;
        }
    }

    /**
     * Sets the value of the invFlattening property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setInvFlattening(Double value) {
        this.invFlattening = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        if (unit == null) {
            return "m";
        } else {
            return unit;
        }
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

}
