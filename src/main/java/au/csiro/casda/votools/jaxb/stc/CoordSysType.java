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


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Coordinate system definition: a collection of coordinate frames
 * 
 * A CoordSys consists of at least one coordinate frames; unfortunately, schema inheritance and polymorphism doesn't allow us to specify this in the most genarl way
 * 
 * <p>Java class for coordSysType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="coordSysType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}stcBaseType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}CoordFrame" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "coordSysType", propOrder = {
    "coordFrame"
})
@XmlSeeAlso({
    AstroCoordSystemType.class,
    PixelCoordSystemType.class
})
public class CoordSysType
    extends StcBaseType
{

    @XmlElement(name = "CoordFrame", nillable = true)
    protected List<GenericCoordFrameType> coordFrame;

    /**
     * Gets the value of the coordFrame property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the coordFrame property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCoordFrame().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericCoordFrameType }
     * 
     * 
     */
    public List<GenericCoordFrameType> getCoordFrame() {
        if (coordFrame == null) {
            coordFrame = new ArrayList<GenericCoordFrameType>();
        }
        return this.coordFrame;
    }

}
