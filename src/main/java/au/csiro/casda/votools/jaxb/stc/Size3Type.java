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


import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * Consists of a Size (3 doubles) and optional position angle element
 * 
 * <p>Java class for size3Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="size3Type">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}double3Type">
 *       &lt;sequence>
 *         &lt;element name="PosAngle1" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}posAngleType" minOccurs="0"/>
 *         &lt;element name="PosAngle2" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}posAngleType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "size3Type", propOrder = {
    "posAngle1",
    "posAngle2"
})
public class Size3Type
    extends Double3Type
{

    @XmlElementRef(name = "PosAngle1", namespace = "http://www.ivoa.net/xml/STC/stc-v1.30.xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<PosAngleType> posAngle1;
    @XmlElementRef(name = "PosAngle2", namespace = "http://www.ivoa.net/xml/STC/stc-v1.30.xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<PosAngleType> posAngle2;

    /**
     * Gets the value of the posAngle1 property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PosAngleType }{@code >}
     *     
     */
    public JAXBElement<PosAngleType> getPosAngle1() {
        return posAngle1;
    }

    /**
     * Sets the value of the posAngle1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PosAngleType }{@code >}
     *     
     */
    public void setPosAngle1(JAXBElement<PosAngleType> value) {
        this.posAngle1 = value;
    }

    /**
     * Gets the value of the posAngle2 property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PosAngleType }{@code >}
     *     
     */
    public JAXBElement<PosAngleType> getPosAngle2() {
        return posAngle2;
    }

    /**
     * Sets the value of the posAngle2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PosAngleType }{@code >}
     *     
     */
    public void setPosAngle2(JAXBElement<PosAngleType> value) {
        this.posAngle2 = value;
    }

}
