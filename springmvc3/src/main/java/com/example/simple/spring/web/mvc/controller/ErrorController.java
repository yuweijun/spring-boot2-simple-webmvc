package com.example.simple.spring.web.mvc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ErrorController {

    private final Log logger = LogFactory.getLog(getClass());

    @RequestMapping(value = "/errors", method = RequestMethod.GET)
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest, ModelMap model) throws JsonProcessingException {
        logger.info("render /errors page according web.xml error-page configs");

        ModelAndView modelAndView = new ModelAndView("json");
        String message = "";
        
        int code = getErrorCode(httpRequest);

        switch (code) {
            case 400: {
                message = "Http Error Code: 400. Bad Request";
                break;
            }
            case 401: {
                message = "Http Error Code: 401. Unauthorized";
                break;
            }
            case 404: {
                message = "Http Error Code: 404. Resource not found";
                break;
            }
            case 500: {
                message = "Http Error Code: 500. Internal Server Error";
                break;
            }
        }

        // DispatcherServlet:1155 - Rendering view [org.springframework.web.servlet.view.JstlView: name 'json'; URL [/WEB-INF/jsp/json.jsp]] in DispatcherServlet with name 'dispatcher'
        // JstlView:371 - Added model object 'json' of type [java.lang.String] to request in view with name 'json'
        // JstlView:236 - Forwarding to resource [/WEB-INF/jsp/json.jsp] in InternalResourceView 'json'
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("error", message);

        final ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(hashMap);

        model.addAttribute("json", json);

        return modelAndView;
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest.getAttribute("javax.servlet.error.status_code");
    }

}