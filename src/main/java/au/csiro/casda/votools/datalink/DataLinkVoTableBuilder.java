package au.csiro.casda.votools.datalink;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

import net.ivoa.vo.Data;
import net.ivoa.vo.DataType;
import net.ivoa.vo.Field;
import net.ivoa.vo.Group;
import net.ivoa.vo.ObjectFactory;
import net.ivoa.vo.Param;
import net.ivoa.vo.Resource;
import net.ivoa.vo.Table;
import net.ivoa.vo.TableData;
import net.ivoa.vo.Td;
import net.ivoa.vo.Tr;
import net.ivoa.vo.VoTable;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2010 - 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Creates a VOTable xml strings matching the DataLink specification. Each instance can only be used to produce a single
 * VOTABLE string.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class DataLinkVoTableBuilder
{

    private VoTable votable;

    private Table resultsTable;

    private ObjectFactory objectFactory;

    private String baseUrl;

    private Field authenticatedIdTokenField;

    /**
     * Create a new DataLinkVoTableBuilder instance
     * 
     * @param baseUrl
     *            The application's base URL.
     */
    public DataLinkVoTableBuilder(String baseUrl)
    {
        this.baseUrl = baseUrl;
        votable = new VoTable();
        votable.setVersion("1.3");
        objectFactory = new ObjectFactory();
    }

    /**
     * Add a results table to the VOTABLE. Only one results table is allowed so multiple calls to this method are
     * ignored.
     * 
     * @return The DataLinkVoTableBuilder instance
     */
    public DataLinkVoTableBuilder withResultsTable()
    {
        if (resultsTable != null)
        {
            // Only on results table, so don't recreate it if it is already there.
            return this;
        }
        Resource resultsResource = new Resource();
        resultsResource.setType("results");
        resultsResource.setName("CASDA Datalink Result");
        
        resultsTable = new Table();
        List<JAXBElement<?>> content = resultsTable.getContent();
        content.add(buildStringField("ID", "meta.id;meta.main"));
        content.add(buildStringField("access_url", "meta.ref.url"));
        content.add(buildStringField("service_def", "meta.ref"));
        content.add(buildStringField("error_message", "meta.code.error"));
        content.add(buildStringField("description", "meta.note"));
        content.add(buildStringField("semantics", "meta.code"));
        content.add(buildStringField("content_type", "meta.code.mime"));
        content.add(buildNumericField("content_length", DataType.LONG, "byte", "phys.size;meta.file"));
        JAXBElement<Field> authenticatedIdTokenField =
                buildStringField("authenticated_id_token", "meta.id", "authenticatedIdToken");
        content.add(authenticatedIdTokenField);
        this.authenticatedIdTokenField = authenticatedIdTokenField.getValue();

        resultsResource.getLINKAndTABLEOrRESOURCE().add(resultsTable);
        votable.getRESOURCE().add(resultsResource);
        return this;
    }

    /**
     * Adds a RESOURCE to the VOTABLE that describes a service (typically referred to in a service_def field using the
     * xmlId).
     * 
     * @param xmlId
     *            the XML ID of the service definition
     * @param standardId
     *            the 'standardID' of the service (eg: ivo://ivoa.net/std/SODA#async-1.0)
     * @param accessUrl
     *            the URL to access the service
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withServiceDefinition(String xmlId, String standardId, String accessUrl)
    {
        return withServiceDefinition(xmlId, standardId, accessUrl, false);
    }

    /**
     * Adds a RESOURCE to the VOTABLE that describes a service (typically referred to in a service_def field using the
     * xmlId).
     * 
     * @param xmlId
     *            the XML ID of the service definition
     * @param standardId
     *            the 'standardID' of the service (eg: ivo://ivoa.net/std/SODA#async-1.0)
     * @param accessUrl
     *            the URL to access the service
     * @param isGeneratedFile
     *            is a isGeneratedFile service (cutout or generated spectrum)
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withServiceDefinition(String xmlId, String standardId, String accessUrl,
            boolean isGeneratedFile)
    {
        Resource serviceResource = new Resource();

        serviceResource.setType("meta");
        serviceResource.setUtype("adhoc:service");
        serviceResource.setID(xmlId);

        Param standardIdParam = new Param();
        standardIdParam.setName("standardID");
        standardIdParam.setDatatype(DataType.CHAR);
        standardIdParam.setArraysize("*");
        standardIdParam.setValue(standardId);
        serviceResource.getCOOSYSOrGROUPOrPARAM().add(standardIdParam);

        Param accessUrlParam = new Param();
        accessUrlParam.setName("accessURL");
        accessUrlParam.setDatatype(DataType.CHAR);
        accessUrlParam.setArraysize("*");
        accessUrlParam.setValue(accessUrl);
        serviceResource.getCOOSYSOrGROUPOrPARAM().add(accessUrlParam);

        Group inputParamsGroup = new Group();
        inputParamsGroup.setName("inputParams");
        serviceResource.getCOOSYSOrGROUPOrPARAM().add(inputParamsGroup);

        inputParamsGroup.getFIELDrefOrPARAMrefOrPARAM().addAll(createParamsList(CutoutParam.ID));

        if (isGeneratedFile)
        {
            inputParamsGroup.getFIELDrefOrPARAMrefOrPARAM().addAll(createParamsList(CutoutParam.POS));
            inputParamsGroup.getFIELDrefOrPARAMrefOrPARAM().addAll(createParamsList(CutoutParam.BAND));
            inputParamsGroup.getFIELDrefOrPARAMrefOrPARAM().addAll(createParamsList(CutoutParam.POL));
            inputParamsGroup.getFIELDrefOrPARAMrefOrPARAM().addAll(createParamsList(CutoutParam.COORD));
        }

        votable.getRESOURCE().add(serviceResource);
        return this;
    }

    private Param createParam(String name, DataType type, String arraySize, String xtype, String value, String unit,
            Object ref)
    {
        Param inputParam = new Param();
        inputParam.setName(name);
        inputParam.setDatatype(type);
        inputParam.setArraysize(arraySize);
        inputParam.setXtype(xtype);
        inputParam.setValue(value);
        inputParam.setUnit(unit);
        inputParam.setRef(ref);

        return inputParam;
    }

    private List<Param> createParamsList(CutoutParam cutoutParam)
    {
        List<Param> inputParams = new ArrayList<>();

        if (cutoutParam != null)
        {
            switch (cutoutParam)
            {
            case ID:
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", null, "", null,
                        this.authenticatedIdTokenField));
                break;

            case POS:
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", "circle", null, null, null));
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", "range", null, null, null));
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", "polygon", null, null, null));
                break;

            case BAND:
                inputParams.add(createParam(cutoutParam.name(), DataType.DOUBLE, "*", "interval", null, "m", null));
                break;

            case POL:
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", null, null, null, null));
                break;
                
            case COORD:
                inputParams.add(createParam(cutoutParam.name(), DataType.CHAR, "*", null, null, null, null));
                break;
                
            default:
                break;
            }

        }
        return inputParams;
    }

    /**
     * Add a service meta resource element for the data link service to the VOTABLE.
     * 
     * @param url
     *            The URL of the service.
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withServiceMeta(String url)
    {
        Resource metaResource = new Resource();
        metaResource.setType("meta");
        metaResource.setUtype("adhoc:service");
        metaResource.setName("this");

        List<Object> list = metaResource.getCOOSYSOrGROUPOrPARAM();
        list.add(buildStringParam("standardID", "ivo://ivoa.net/std/DataLink#links-1.0"));
        list.add(buildStringParam("accessUrl", url));

        Group inputParams = new Group();
        inputParams.setName("inputParams");
        Param idParam = buildStringParam("ID", "");
        // idParam.setRef("");
        inputParams.getFIELDrefOrPARAMrefOrPARAM().add(idParam);
        list.add(inputParams);

        votable.getRESOURCE().add(metaResource);

        return this;
    }

    /**
     * Adds a result row to the results table for access via an access_url. Must not be called until withResultsTable()
     * has been called.
     * 
     * @param id
     *            the id of the item
     * @param accessUrl
     *            a URL that allows for direct download
     * @param description
     *            a description of the service (optional)
     * @param mimeType
     *            the mime-type of the item (optional)
     * @param size
     *            the size of the item (optional)
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withAccessUrlResult(String id, String accessUrl, String description, String mimeType,
            Long size)
    {
        return withAccessUrlResult(StringEscapeUtils.escapeXml10(id), accessUrl, description, mimeType, size, "#this");
    }

    /**
     * Adds a result row to the results table for access via an access_url. Must not be called until withResultsTable()
     * has been called.
     * 
     * @param id
     *            the id of the item
     * @param accessUrl
     *            a URL that allows for direct download
     * @param description
     *            a description of the service (optional)
     * @param mimeType
     *            the mime-type of the item (optional)
     * @param size
     *            the size of the item (optional)
     * @param semantics
     *            the RDF type of the data product being linked to.
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withAccessUrlResult(String id, String accessUrl, String description, String mimeType,
            Long size, String semantics)
    {
        if (resultsTable == null)
        {
            throw new IllegalStateException("A results table must be made before a result row can be added.");
        }
        addRow(StringEscapeUtils.escapeXml10(id), accessUrl, null, null, description, semantics, mimeType, size, null);
        return this;
    }

    /**
     * Adds a result row to the results table for access via a service defined in a service definition. Must not be
     * called until withResultsTable() has been called.
     * 
     * @param id
     *            the id of the item
     * @param serviceDefXmlId
     *            the XML id of the service definition RESOURCE
     * @param description
     *            a description of the service (optional)
     * @param mimeType
     *            the mime-type of the item (optional)
     * @param size
     *            the size of the item (optional)
     * @param authenticatedIdToken
     *            an opaque version of the id that can be used to access the item in an authenticated way
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withServiceDefResult(String id, String serviceDefXmlId, String description,
            String mimeType, Long size, String authenticatedIdToken)
    {
        return withServiceDefResult(StringEscapeUtils.escapeXml10(id), serviceDefXmlId, description, "#this", 
                mimeType, size, authenticatedIdToken);
    }

    /**
     * Adds a result row to the results table for access via a service defined in a service definition. Must not be
     * called until withResultsTable() has been called.
     * 
     * @param id
     *            the id of the item
     * @param serviceDefXmlId
     *            the XML id of the service definition RESOURCE
     * @param description
     *            a description of the service (optional)
     * @param semantics
     *            a semantics of the service (optional)
     * @param mimeType
     *            the mime-type of the item (optional)
     * @param size
     *            the size of the item (optional)
     * @param authenticatedIdToken
     *            an opaque version of the id that can be used to access the item in an authenticated way
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withServiceDefResult(String id, String serviceDefXmlId, String description,
            String semantics, String mimeType, Long size, String authenticatedIdToken)
    {
        if (resultsTable == null)
        {
            throw new IllegalStateException("A results table must be made before a result row can be added.");
        }
        addRow(StringEscapeUtils.escapeXml10(id), null, serviceDefXmlId, null, description, semantics, mimeType, 
                size, authenticatedIdToken);
        return this;
    }

    /**
     * Adds a result row to the results table that represents an error associated with getting access data information
     * for the given item. Must not be called until withResultsTable() has been called.
     * 
     * @param id
     *            the id of the item
     * @param error
     *            a description of the error
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withErrorResult(String id, String error)
    {
        if (resultsTable == null)
        {
            throw new IllegalStateException("A results table must be made before a result row can be added.");
        }
        addRow(StringEscapeUtils.escapeXml10(id), null, null, error, null, "#error", null, null, null);
        return this;
    }
    
    /**
     * Adds a result row to the results table that represents an error associated with getting access data information
     * for the given item. Must not be called until withResultsTable() has been called.
     * 
     * @param id
     *            the id of the item
     * @param description
     *             a description of the service (optional)
     * @param error
     *            a description of the error
     * @return this builder instance
     */
    public DataLinkVoTableBuilder withErrorResult(String id, String description, String error)
    {
        if (resultsTable == null)
        {
            throw new IllegalStateException("A results table must be made before a result row can be added.");
        }
        addRow(StringEscapeUtils.escapeXml10(id), null, null, error, description, "#error", null, null, null);
        return this;
    }

    private void addRow(String id, String accessUrl, String serviceDef, String errorMessage, String description,
            String semantics, String contentType, Long contentLength, String authenticatedIdToken)
    {
        // checks to make sure only one of these three fields is non-null. not too worried though as to cause this error
        // changed would probably have to be made to code of calling methods.
        if (!(accessUrl != null ^ serviceDef != null ^ errorMessage != null)
                || (accessUrl != null && serviceDef != null && errorMessage != null))
        {
            throw new IllegalStateException("Only one of 'access_url', 'service_def', 'error_message' fields can be "
                    + "non-null in rows of the datalink results xml");
        }

        TableData tableData = getResultTableData();

        Tr row = new Tr();
        List<Td> tdList = row.getTD();
        tdList.add(buildTd(StringEscapeUtils.escapeXml10(id)));
        tdList.add(buildTd(accessUrl));
        tdList.add(buildTd(serviceDef));
        tdList.add(buildTd(errorMessage));
        tdList.add(buildTd(description));
        tdList.add(buildTd(semantics));
        tdList.add(buildTd(contentType));
        tdList.add(buildTd(contentLength == null ? null : Long.toString(contentLength)));
        tdList.add(buildTd(authenticatedIdToken));

        tableData.getTR().add(row);
    }

    /**
     * Retrieve the XML string for the VOTable object that has been built up.
     * 
     * @return The xml string
     * @throws JAXBException
     *             If the VOTable could not be converted to XML.
     */
    public String getXml() throws JAXBException
    {
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();

        JAXBContext jaxbContext = JAXBContext.newInstance(VoTable.class);
        Marshaller m = jaxbContext.createMarshaller();

        // IE required namespace prefix in xml for XSL transform.
        // append namespace prefix "VOT" into all elements
        m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapper()
        {
            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
            {
                return "";
            }
        });

        m.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                "<?xml-stylesheet href='" + baseUrl + "votable.xsl' type='text/xsl' ?>\n");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(votable, xmlStream);

        try
        {
            return xmlStream.toString(CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return
     */
    private Td buildTd(String value)
    {
        Td cell = new Td();
        cell.setValue(value);
        return cell;
    }

    private JAXBElement<Field> buildNumericField(String name, DataType dataType, String unit, String ucd, String xmlId)
    {
        return buildField(name, dataType, null, unit, ucd, null);
    }

    private JAXBElement<Field> buildNumericField(String name, DataType dataType, String unit, String ucd)
    {
        return this.buildNumericField(name, dataType, unit, ucd, null);
    }

    private JAXBElement<Field> buildStringField(String name, String ucd, String xmlId)
    {
        return buildField(name, DataType.CHAR, "*", null, ucd, xmlId);
    }

    private JAXBElement<Field> buildStringField(String name, String ucd)
    {
        return this.buildStringField(name, ucd, null);
    }

    private JAXBElement<Field> buildField(String name, DataType dataType, String arraysize, String unit, String ucd,
            String xmlId)
    {
        Field field = new Field();
        field.setName(name);
        field.setDatatype(dataType);
        if (StringUtils.isNotEmpty(arraysize))
        {
            field.setArraysize(arraysize);
        }
        if (StringUtils.isNotEmpty(unit))
        {
            field.setUnit(unit);
        }
        field.setUcd(ucd);
        if (StringUtils.isNotBlank(xmlId))
        {
            field.setID(xmlId);
        }
        return objectFactory.createTableFIELD(field);
    }

    private Param buildStringParam(String name, String value)
    {
        Param param = new Param();
        param.setName(name);
        param.setDatatype(DataType.CHAR);
        param.setArraysize("*");
        param.setValue(value);
        return param;
    }

    private TableData getResultTableData()
    {
        Data data = null;
        for (JAXBElement<?> jaxbElement : resultsTable.getContent())
        {
            if ("DATA".equals(jaxbElement.getName().getLocalPart()))
            {
                data = (Data) jaxbElement.getValue();
            }
        }

        if (data == null)
        {
            data = new Data();
            resultsTable.getContent().add(objectFactory.createTableDATA(data));
        }
        if (data.getTABLEDATA() == null)
        {
            TableData tableData = new TableData();
            data.setTABLEDATA(tableData);
        }
        TableData tableData = data.getTABLEDATA();
        return tableData;
    }

}
