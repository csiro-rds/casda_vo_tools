package au.csiro.casda.votools.tap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import adql.parser.ParseException;
import au.csiro.casda.votools.config.ConfigKeys;
import au.csiro.casda.votools.config.Configurable;
import au.csiro.casda.votools.config.Configuration;
import au.csiro.casda.votools.config.ConfigurationException;
import au.csiro.casda.votools.config.ConfigurationRegistry;
import au.csiro.casda.votools.config.EndPoint;
import au.csiro.casda.votools.utils.Utils;

/**
 * Handle the parsing and processing of the UPLOAD parameter as defined in TAP 1.0, TAP 1.1 and DALI 1.1.
 * <p>
 * Copyright 2018, CSIRO Australia. All rights reserved.
 */
@Component
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UploadParamProcessor extends Configurable
{
    private static final String TABLE_NAME_PATTERN = "[a-zA-z][a-zA-Z0-9_]+"; 
    private static final String WEB_REF_PATTERN = "https?://.+\\..+"; 
    private static final String INLINE_PATTERN = "param:[a-zA-Z][a-zA-Z0-9_-]+"; 
    private static final String DALI_ADDRESS_PATTERN = "("+ WEB_REF_PATTERN + "|" + INLINE_PATTERN + ")";
    private static final String DALI_PARAM_PATTERN = TABLE_NAME_PATTERN+"\\s*,\\s*"+ DALI_ADDRESS_PATTERN;
    private static final String TAP_10_PARAM_PATTERN = DALI_PARAM_PATTERN + "(;" + DALI_PARAM_PATTERN + ")+";
    private Configuration config;
    private boolean ready;
    private Boolean uploadEnabled ;
    private ConfigurationRegistry configRegistry;

    /**
     * Constructor
     * 
     * @param registry
     *            the configuration registry
     */
    @Autowired
    public UploadParamProcessor(ConfigurationRegistry registry)
    {
        this.configRegistry = registry;
    }

    /**
     * This method will set up any objects that depend on values injected from spring.
     * 
     * @throws ConfigurationException
     *             if there was a configuration problem
     */
    @PostConstruct
    public void init() throws ConfigurationException
    {
        configRegistry.register(this);
        ready = false;
    }

    @Override
    public void setConfiguration(Configuration config)
    {
        this.config = config;
        ready = false;
    }

    @Override
    public boolean isReady() throws ConfigurationException
    {
        if (config != null && !ready)
        {
            EndPoint tapEndPoint = config.getEndPoint("TAP");
            if (tapEndPoint == null)
            {
                return false;
            }
            String setting = tapEndPoint.get(ConfigKeys.TAP_UPLOAD_ENABLED.getKey());
            uploadEnabled =
                    "1".equalsIgnoreCase(setting) || "true".equalsIgnoreCase(setting) || "Y".equalsIgnoreCase(setting);
            ready = true;
        }
        return ready;
    }

    @Override
    public void invalidate()
    {
        ready = false;
        config = null;
    }

    /**
     * Process the table upload parameters in the supplied request. The returned list will include references to the
     * tables loaded into the database.
     * 
     * @param request The web request. 
     * @return The list of uploaded tables.
     * @throws ParseException If the upload parameters were invalid.
     */
    public List<UploadedTable> processUploadParams(HttpServletRequest request) throws ParseException
    {
        // Extract params from request
        Map<String, String[]> paramsMap = Utils.buildParamsMap(request.getParameterMap());
        String[] uploadParams = paramsMap.get("upload");
        if (!uploadEnabled || uploadParams == null)
        {
            return new ArrayList<>();
        }
        
        // Parse and validate the params
        List<String> errorList = new ArrayList<>();  
        Map<String, UploadedTable> uploadTables = parseParams(uploadParams, errorList);
        if (!errorList.isEmpty())
        {
            throw new ParseException(StringUtils.join(errorList, ", "));
        }
        
        // Connect to and load the tables
        
        return new ArrayList<>(uploadTables.values());
        
    }
    
    /**
     * Check the values of the upload parameters on the TAP request. 
     * 
     * @param values
     *            The parameter values to be validated.
     * @return The error messages, or an empty list if all values are valid.
     */
    List<String> validate(String[] values)
    {
        List<String> errorList = new ArrayList<>();
        for (String paramString : values)
        {
            if (!paramString.matches(DALI_PARAM_PATTERN) && !paramString.matches(TAP_10_PARAM_PATTERN))
            {
                errorList.add("UsageFault: Invalid UPLOAD parameter format: " + paramString);
            }
            else if (paramString.matches(TAP_10_PARAM_PATTERN))
            {
                for (String singleParam : paramString.split(";"))
                {
                    if (!singleParam.matches(DALI_PARAM_PATTERN))
                    {
                        errorList.add("UsageFault: Invalid UPLOAD parameter format: " + singleParam);

                    }
                }
            }
        }
        
        return errorList;
        
    }

    /**
     * Parse the upload parameters into a map of uploaded tables. 
     * @param uploadParams The UPLOAD parameter strings passed into the web request.
     * @param errorList A list to be populated with any errors encountered.
     * @return The map of uploaded tables, null if there were any errors.
     */
    Map<String, UploadedTable> parseParams(String[] uploadParams, List<String> errorList)
    {
        List<String> validationErrors = validate(uploadParams);
        if (!validationErrors.isEmpty())
        {
            errorList.addAll(validationErrors);
            return null;
        }
        
        Map<String, UploadedTable> uploadTables = new HashMap<>();

        for (String paramString : uploadParams)
        {
            if (paramString.matches(TAP_10_PARAM_PATTERN))
            {
                for (String singleParam : paramString.split(";"))
                {
                    UploadedTable table = buildUploadTable(singleParam);
                    uploadTables.put(table.getName(), table);
                }
            }
            else
            {
                UploadedTable table = buildUploadTable(paramString);
                uploadTables.put(table.getName(), table);
            }
        }
        
        return uploadTables;
    }

    private UploadedTable buildUploadTable(String uploadParam)
    {
        int splitPos = uploadParam.indexOf(",");
        String tableName = uploadParam.substring(0,  splitPos).trim();
        String url = uploadParam.substring(splitPos+1).trim();

        return new UploadedTable(tableName, url);
    }
}
