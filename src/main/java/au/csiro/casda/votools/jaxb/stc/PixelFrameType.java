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


import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * A pixel coordinate frame (which may be 1-D, 2-D, or 3-D) consists of a coordinate frame, a reference position, a flavor, a reference pixel array and the order in which the pixel axes appear in the pixel array
 * 
 * <p>Java class for pixelFrameType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pixelFrameType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}genericCoordFrameType">
 *       &lt;sequence>
 *         &lt;element name="ReferencePixel" type="{http://www.ivoa.net/xml/STC/stc-v1.30.xsd}pixelType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="axis1_order" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="axis2_order" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="axis3_order" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="ref_frame_id" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pixelFrameType", propOrder = {
    "referencePixel"
})
public class PixelFrameType
    extends GenericCoordFrameType
{

    @XmlElementRef(name = "ReferencePixel", namespace = "http://www.ivoa.net/xml/STC/stc-v1.30.xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<PixelType> referencePixel;
    @XmlAttribute(name = "axis1_order", required = true)
    protected BigInteger axis1Order;
    @XmlAttribute(name = "axis2_order")
    protected BigInteger axis2Order;
    @XmlAttribute(name = "axis3_order")
    protected BigInteger axis3Order;
    @XmlAttribute(name = "ref_frame_id")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object refFrameId;

    /**
     * Gets the value of the referencePixel property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PixelType }{@code >}
     *     
     */
    public JAXBElement<PixelType> getReferencePixel() {
        return referencePixel;
    }

    /**
     * Sets the value of the referencePixel property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PixelType }{@code >}
     *     
     */
    public void setReferencePixel(JAXBElement<PixelType> value) {
        this.referencePixel = value;
    }

    /**
     * Gets the value of the axis1Order property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAxis1Order() {
        return axis1Order;
    }

    /**
     * Sets the value of the axis1Order property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAxis1Order(BigInteger value) {
        this.axis1Order = value;
    }

    /**
     * Gets the value of the axis2Order property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAxis2Order() {
        return axis2Order;
    }

    /**
     * Sets the value of the axis2Order property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAxis2Order(BigInteger value) {
        this.axis2Order = value;
    }

    /**
     * Gets the value of the axis3Order property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAxis3Order() {
        return axis3Order;
    }

    /**
     * Sets the value of the axis3Order property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAxis3Order(BigInteger value) {
        this.axis3Order = value;
    }

    /**
     * Gets the value of the refFrameId property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getRefFrameId() {
        return refFrameId;
    }

    /**
     * Sets the value of the refFrameId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRefFrameId(Object value) {
        this.refFrameId = value;
    }

}
