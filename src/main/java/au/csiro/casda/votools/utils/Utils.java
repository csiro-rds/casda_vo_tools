package au.csiro.casda.votools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class Utils
{
    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    
    /**
     * Salt length in bits.
     */
    private static final int SALT_LENGTH = 32;

    private static final int HASH_KEY_LENGTH = 256;
    
    private static final int HASH_ITERATION_COUNT = 80932*6;
    
    private static final int PASSWORD_MIN_LENGTH = 8;
    /** Constant for the default password */
    public static final String DEFAULT_PASSWORD = "password";
    /** Constant for the vo tools admin user name */
    public static final String USERNAME = "voadmin";
    /** Constant for the file containing the encrypted password */
    public static final String AUTH_FILE_NAME = "authz";
    /** Constant for the vo tools admin role */
    public static final String ADMIN_ROLE = "VO_TOOLS_ADMIN";
    
    /** The number of bytes in a kilobyte. */
    public static final long ONE_KB_IN_BYTES = 1024; 


    /**
     * Prepares a string for SQL statement
     * 
     * @param value
     *            string to prepare
     * @return a string ready to insert into an SQL text
     */
    public static String sql(String value)
    {
        String result = value == null ? "null" : "'" + value.replaceAll("['\"\\\\]", "\\\\$0") + "'";
        return result;
    }

    /**
     * converts camel case into normal text e.g. 'thisIsExample' becomes 'This is Example'
     * 
     * @param s
     *            the camel case string
     * @return the human readable string
     */
    public static String convertCamelCase(String s)
    {
        // Don't convert text which is all uppercase and underscores
        if (s.matches("^[A-Z_]+$"))
        {
            return s;
        }
        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }

    /**
     * 
     * @param password1
     *            the first password
     * @param password2
     *            the confirmation password
     * @return true if they match and contain 1 uppercase char, one lowercase char, one number &amp; one punctuation
     */
    public static boolean validatePassword(String password1, String password2)
    {
        String lowerRegex = "^.*[A-Z].*$";
        String upperRegex = "^.*[a-z].*$";
        String numberRegex = "^.*[0-9].*$";
        String punctRegex = "^.*[\\p{Punct}].*$";

        return !(StringUtils.isBlank(password1) || StringUtils.isBlank(password2) || !password1.equals(password2)
                || password1.length() < PASSWORD_MIN_LENGTH || !password1.matches(lowerRegex)
                || !password1.matches(upperRegex) || !password1.matches(numberRegex) || !password1.matches(punctRegex));
    }

    /**
     * Write User name / password to authz file. 
     * 
     * @param authzFile
     *            The authz file to be updated.
     * @param details
     *            the details to write to a file.  Details are User name and password. 
     * @throws IOException
     *             thrown when an I/O exception of some sort has occurred
     */
    public static void writeAdminCredentialsToFile(File authzFile, String[] details) throws IOException
    {
        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(authzFile, false),
                StandardCharsets.UTF_8.newEncoder());
        String userName = details[0];
        String password = details[1];
        output.write(userName + " " + password);
        output.close();
    }

    /**
     * Generate a salt value used for password hashing.
     * 
     * @return A randomly generate salt.
     */
    public static byte[] generateSalt()
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Hash plain text password using the specified salt. Salt value is stored with the password.
     * 
     * @param password
     *            The plain text password
     * @param salt
     *            Salt
     * @return Hashed password and salt.
     */
    public static String hashPassword(String password, byte[] salt)
    {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 
                HASH_ITERATION_COUNT, HASH_KEY_LENGTH);
        SecretKeyFactory factory;
        byte[] hash;
        
        try
        {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = factory.generateSecret(spec).getEncoded();
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            return password;
        }
        
        return Base64.encodeBase64String(salt) + '$' + Base64.encodeBase64String(hash);
    }

    /**
     * Authenticate a raw password against the stored hashed version.
     * 
     * @param rawPassword
     *            The raw password
     * @param storedPassword
     *            The hashed password
     * @return True if passwords match, false otherwise.
     */
    public static boolean authenticate(String rawPassword, String storedPassword)
    {
        String[] saltAndPassword = storedPassword.split("\\$");

        if (saltAndPassword.length != 2)
        {
            // default password still being used. ignore.
            if(rawPassword.equals(storedPassword) && DEFAULT_PASSWORD.equals(storedPassword)) 
            {
                return true;
            }
            return false;
        }

        if (StringUtils.isEmpty(saltAndPassword[0]) || StringUtils.isEmpty(saltAndPassword[1]))
        {
            return false;
        }

        byte[] salt = Base64.decodeBase64(saltAndPassword[0]);

        String hashedPassword = hashPassword(rawPassword, salt);
        if (hashedPassword.equals(storedPassword))
        {
            return true;
        }
        return false;
    }

    /**
     * @param authzFile
     *             The authz file holding the current credentials. 
     * @throws IOException
     *             thrown when an I/O exception of some sort has occurred
     * @return details
     */
    public static String[] retrieveAdminCredentials(File authzFile) throws IOException
    {
        String[] details = new String[] { USERNAME, DEFAULT_PASSWORD };

        // creates file if it does not exist, this will be for initial deploy + the admin can reset the password simply
        // by deleting the file (as password will be encrypted)
        FileUtils.mkDir(authzFile.getParentFile());
        if (authzFile.createNewFile())
        {
            writeAdminCredentialsToFile(authzFile, details);
        }

        // if still no file falls back to default details
        if (authzFile != null)
        {
            BufferedReader fileContent =
                    new BufferedReader(new InputStreamReader(new FileInputStream(authzFile), StandardCharsets.UTF_8));

            String firstLine = fileContent.readLine();
            if (firstLine != null)
            {
                details = firstLine.split(" ");
            }
            fileContent.close();
        }

        return details;
    }

    /**
     * Creates a new map from the input map, but ensuring that all keys are lowercase.
     * 
     * @param requestParams
     *            The map of parameters from the web request.
     * @return The map of parameters with lower case keys.
     */
    public static Map<String, String[]> buildParamsMap(Map<String, String[]> requestParams)
    {
        Map<String, String[]> paramsMap = new HashMap<>();
        for (Entry<String, String[]> entry : requestParams.entrySet())
        {
            String key = entry.getKey().trim().toLowerCase();
            if (paramsMap.containsKey(key))
            {
                paramsMap.put(key, ArrayUtils.addAll(paramsMap.get(key), entry.getValue()));
            }
            else
            {
                paramsMap.put(key, entry.getValue());
            }
        }
        return paramsMap;
    }

    /**
     * Adds param strings as an array, and will merge values if required.
     * 
     * @param paramsMap
     *            the map of parameters to update
     * @param paramsToAdd
     *            the params to add
     */
    public static void addParamsAsStringArray(Map<String, String[]> paramsMap, Map<String, String> paramsToAdd)
    {
        for (Entry<String, String> entry : paramsToAdd.entrySet())
        {
            String key = entry.getKey().trim().toLowerCase();
            if (paramsMap.containsKey(key))
            {
                paramsMap.put(key, ArrayUtils.addAll(paramsMap.get(key), entry.getValue()));
            }
            else
            {
                paramsMap.put(key, new String[] { entry.getValue() });
            }
        }
    }

    /**
     * Retrieves authorisation parameters from the request header if the auth header can be trusted
     * 
     * @param request
     *            the request with auth info in the header
     * @param trustAuthHeader
     *            true if the auth ifo in the header can be trusted
     * @return map of auth info
     */
    public static Map<String, String> getAuthParams(HttpServletRequest request, boolean trustAuthHeader)
    {
        Map<String, String> paramsMap = new HashMap<>();
        String userId = VoKeys.ANONYMOUS_USER;
        String userName = VoKeys.ANONYMOUS_USER;
        String loginSystem = StringUtils.EMPTY;
        String userProjects = StringUtils.EMPTY;
        String casdaLargeWebDownload = "false";
        if (StringUtils.isNotBlank(request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID)))
        {
            if (trustAuthHeader)
            {
                userId = request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID);
                userName = request.getHeader(VoKeys.VO_AUTH_HEADER_USER_NAME);
                loginSystem = request.getHeader(VoKeys.VO_AUTH_HEADER_LOGIN_SYSTEM);
                userProjects = StringUtils.defaultString(request.getHeader(VoKeys.VO_AUTH_HEADER_USER_PROJECTS),
                        StringUtils.EMPTY);
                casdaLargeWebDownload = request.getHeader(VoKeys.VO_HEADER_LARGE_WEB_DOWNLOAD);
            }
            else
            {
                logger.warn("Rejecting auth header from {} for {}.", request.getRemoteAddr(),
                        request.getHeader(VoKeys.VO_AUTH_HEADER_USER_ID));
            }
        }
        paramsMap.put(VoKeys.USER_NAME, userName);
        paramsMap.put(VoKeys.USER_ID, userId);
        paramsMap.put(VoKeys.LOGIN_SYSTEM, loginSystem);
        paramsMap.put(VoKeys.USER_PROJECTS, userProjects);
        paramsMap.put(VoKeys.LARGE_WEB_DOWNLOAD, casdaLargeWebDownload);
        return paramsMap;
    }

    /**
     * Where clause fragments for the same field are appended together with an OR.
     * 
     * @param fieldSelect
     *            The selection criteria for the field.
     * @param fragment
     *            The fragment to be added.
     */
    public static void appendFragment(StringBuilder fieldSelect, String fragment)
    {
        if (fieldSelect.length() > 0)
        {
            fieldSelect.append(" OR ");
        }
        fieldSelect.append("(");
        fieldSelect.append(fragment);
        fieldSelect.append(")");
    }

    /**
     * Encrypts the given text with the provided secret
     * 
     * @param text
     *            String to be encrypted
     * @param secret
     *            The secret key to encrypt with
     * @return encrypted String
     */
    public static String encryptAesUrlSafe(String text, String secret)
    {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(secret))
        {
            return "";
        }

        // Create key and cipher
        Cipher cipher;
        try
        {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret.getBytes("UTF-8"), "AES"));
            return Base64.encodeBase64URLSafeString(cipher.doFinal(text.getBytes("UTF-8")));
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to decrypt the given encrypted text with the provided secret
     * 
     * @param encryptedString
     *            the encrypted String
     * @param secret
     *            The secret key to decrypt with
     * @return decrypted Strings
     */
    public static String decryptAesUrlSafe(String encryptedString, String secret)
    {
        if (StringUtils.isBlank(encryptedString) || StringUtils.isBlank(secret))
        {
            return "";
        }
        try
        {
            byte[] bytes = secret.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedString)), "UTF-8");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException
                | IllegalBlockSizeException | BadPaddingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Load all properties from a properties file in resources/ on the classpath. The properties are loaded into the
     * properties instance parameter.
     * 
     * @param propertiesFileName
     *            The name of the properties file to be loaded.
     * @return the properties object containing properties extracted from the fiven file
     * @throws IOException if version file cannot be found/opened/read
     */
    public static Properties loadProperties(String propertiesFileName) throws IOException
    {
        Properties appProps = new Properties();
        ClassLoader cl = Utils.class.getClassLoader();
        URL url = cl.getResource(propertiesFileName);
        if (url == null)
        {
            url = cl.getResource("resources/" + propertiesFileName);
        }
        if (url != null)
        {
            InputStream rf = url.openStream();
            if (rf != null)
            {
                appProps.load(rf);
            }
        }
        return appProps;
    }


    /**
     * Log the current amount of used memory (total-free).
     * @param logger The logger to be used.
     */
    public static void reportMemory(Logger logger)
    {
        final long BYTE_IN_GB = ONE_KB_IN_BYTES*ONE_KB_IN_BYTES*ONE_KB_IN_BYTES; 
        Runtime runtime = Runtime.getRuntime();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        float memGb = memory / (float) BYTE_IN_GB;
        
        logger.info("" + String.format(">>Used memory: %.3f GB", memGb));
    }
    
}
