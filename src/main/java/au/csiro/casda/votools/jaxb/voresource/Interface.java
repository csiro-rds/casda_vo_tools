//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.08.11 at 02:55:10 PM EST 
//


package au.csiro.casda.votools.jaxb.voresource;

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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import au.csiro.casda.votools.jaxb.vodataservice.ParamHTTP;

/**
 * 
 *            Additional interface subtypes (beyond WebService and WebBrowser) are 
 *            defined in the VODataService schema.
 *          
 * 
 * <p>Java class for Interface complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Interface">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="accessURL" type="{http://www.ivoa.net/xml/VOResource/v1.0}AccessURL" maxOccurs="unbounded"/>
 *         &lt;element name="securityMethod" type="{http://www.ivoa.net/xml/VOResource/v1.0}SecurityMethod" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" default="1.0" />
 *       &lt;attribute name="role" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Interface", propOrder = {
    "accessURL",
    "securityMethod"
})
//Inserted code : Updated generated code with SeeAlso to ParamHTTP class otherwise failed to marshall correctly with type
@XmlSeeAlso({
    WebBrowser.class,
    WebService.class,
    ParamHTTP.class
})
public abstract class Interface {

    @XmlElement(required = true)
    protected List<AccessURL> accessURL;
    protected List<SecurityMethod> securityMethod;
    @XmlAttribute(name = "version")
    protected String version;
    @XmlAttribute(name = "role")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String role;

    /**
     * Gets the value of the accessURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccessURL }
     * 
     * 
     */
    public List<AccessURL> getAccessURL() {
        if (accessURL == null) {
            accessURL = new ArrayList<AccessURL>();
        }
        return this.accessURL;
    }

    /**
     * Gets the value of the securityMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securityMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecurityMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityMethod }
     * 
     * 
     */
    public List<SecurityMethod> getSecurityMethod() {
        if (securityMethod == null) {
            securityMethod = new ArrayList<SecurityMethod>();
        }
        return this.securityMethod;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

}
