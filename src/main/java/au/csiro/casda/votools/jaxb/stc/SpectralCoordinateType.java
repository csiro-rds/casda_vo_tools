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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Spectral coordinate type
 * 
 * <p>Java class for spectralCoordinateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="spectralCoordinateType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}basicCoordinateType">
 *       &lt;attribute name="coord_system_id" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="unit" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}spectralUnitType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spectralCoordinateType")
public class SpectralCoordinateType
    extends BasicCoordinateType
{

    @XmlAttribute(name = "coord_system_id")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object coordSystemId;
    @XmlAttribute(name = "unit")
    protected SpectralUnitType unit;

    /**
     * Gets the value of the coordSystemId property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getCoordSystemId() {
        return coordSystemId;
    }

    /**
     * Sets the value of the coordSystemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setCoordSystemId(Object value) {
        this.coordSystemId = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link SpectralUnitType }
     *     
     */
    public SpectralUnitType getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpectralUnitType }
     *     
     */
    public void setUnit(SpectralUnitType value) {
        this.unit = value;
    }

}