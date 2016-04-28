package au.csiro.casda.votools.tap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.csiro.casda.votools.result.OutputFormat;

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
 * Basic HTML UI Controller
 * 
 * Provides a simple HTML UI to the various TAP functions.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Controller
public class BasicUiController
{

    /**
     * Implementation of GET /tap -> text/html
     * 
     * @return the model-and-view
     */
    @RequestMapping(value = "/tap", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView show()
    {
        ModelAndView result = new ModelAndView("tap/show");

        List<String> outputFormats = Arrays.asList(OutputFormat.values()).stream()
                .flatMap(outputFormat -> outputFormat.getIdentifiers().stream()).collect(Collectors.toList());
        result.addObject("outputFormats", outputFormats);

        return result;
    }
    
    /**
     * Implementation of GET /datalink -> text/html
     * 
     * @return the model-and-view
     */
    @RequestMapping(value = "/datalink", method = RequestMethod.GET, produces = "text/html")
    public ModelAndView showdl()
    {
        return new ModelAndView("datalink/showdl");
    }

}
