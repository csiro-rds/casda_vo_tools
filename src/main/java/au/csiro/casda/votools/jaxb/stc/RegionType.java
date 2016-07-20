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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Abstract region type; a Region is a Shape or the result of a Region Operation involving one or more Regions
 * 
 * <p>Java class for regionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="regionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}spatialIntervalType">
 *       &lt;sequence>
 *         &lt;element name="Area" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}regionAreaType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="note" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="coord_system_id" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "regionType", propOrder = {
    "area"
})
@XmlSeeAlso({
    UnionType.class,
    DiffType.class,
    IntersectionType.class,
    NegationType.class,
    ShapeType.class
})
public class RegionType
    extends SpatialIntervalType
{

    @XmlElement(name = "Area")
    protected RegionAreaType area;
    @XmlAttribute(name = "note")
    protected String note;
    @XmlAttribute(name = "coord_system_id")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object coordSystemId;

    /**
     * Gets the value of the area property.
     * 
     * @return
     *     possible object is
     *     {@link RegionAreaType }
     *     
     */
    public RegionAreaType getArea() {
        return area;
    }

    /**
     * Sets the value of the area property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegionAreaType }
     *     
     */
    public void setArea(RegionAreaType value) {
        this.area = value;
    }

    /**
     * Gets the value of the note property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the value of the note property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNote(String value) {
        this.note = value;
    }

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

}