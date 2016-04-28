package au.csiro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * A base class to assist testing JSON serialization (marshalling/unmarshalling).
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public abstract class AbstractMarshallingTest
{

    /**
     * @return An object to be tested for serializability.
     */
    protected abstract Serializable getTestObject();

    /**
     * Allow the implementing test to run some verification of the deserialised object
     * 
     * @param testObj The original object used in the test
     * @param deserObj The object created from the json serialisation of the original object.
     */
    protected void validateTestObject(Object testObj, Object deserObj)
    {
        // No generic tests
    }

    /**
     * Verifies JSON Serialization of the target object.
     * 
     * @throws Exception
     *             If the serialization fails.
     */
    @Test
    public void testJsonSerialization() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Object testObj = getTestObject();
        assertTrue("Should be able to serialise the class.", mapper.canSerialize(testObj.getClass()));

        String json = getJsonFromObject(mapper, testObj);

        Object deserObj = getObjectFromJson(mapper, testObj.getClass(), json);
        validateTestObject(testObj, deserObj);

        String roundRobinJson = getJsonFromObject(mapper, deserObj);
        assertEquals("Json for original and serialized objects should match", json, roundRobinJson);
    }

    protected Object getObjectFromJson(ObjectMapper mapper, Class<? extends Object> objClass, String json)
            throws IOException, JsonParseException, JsonMappingException
    {
        StringReader reader = new StringReader(json);
        Object deserObj = mapper.readValue(reader, objClass);
        return deserObj;
    }

    protected String getJsonFromObject(ObjectMapper mapper, Object testObj) throws IOException,
            JsonGenerationException, JsonMappingException
    {
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, testObj);
        String json = sw.toString();
        return json;
    }

    /**
     * Test Java serialization of the DataCollection object.
     * 
     * @throws Exception
     *             If the serialization fails.
     */
    @Test
    public void testJavaSerialisation() throws Exception
    {
        Serializable testObj = getTestObject();
        String string = toString(testObj);
        Object deserObj = fromString(string);
        assertReflectionEquals("Original and reconstituted objects should match", testObj, deserObj);
    }

    /** Read the object from Base64 string. */
    /**
     * Read in the object using Java Serialization.
     * 
     * @param s
     *            The string representation of the object.
     * @return The object that was serialized
     * @throws IOException
     *             If the stream cannot be read
     * @throws ClassNotFoundException
     *             If the object's class cannot be found.
     */
    private static Object fromString(String s) throws IOException, ClassNotFoundException
    {
        byte[] data = Base64Coder.decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write out the object using Java Serialization.
     * 
     * @param o
     *            The object to be serialized
     * @return The string representation of the object.
     * @throws IOException
     *             If the stream cannot be written.
     */
    private static String toString(Serializable o) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(Base64Coder.encode(baos.toByteArray()));
    }

}
