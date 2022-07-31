package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

public class ServletModelAttributeMethodProcessor extends ModelAttributeMethodProcessor {

    @Override
    protected final Object createAttribute(String attributeName, MethodParameter parameter, HttpServletRequest request) throws Exception {
        String value = getRequestValueForAttribute(attributeName, request);
        logger.debug("getRequestValueForAttribute : " + value);

        return super.createAttribute(attributeName, parameter, request);
    }

    protected String getRequestValueForAttribute(String attributeName, HttpServletRequest request) {
        Map<String, String> variables = getUriTemplateVariables(request);
        if (StringUtils.hasText(variables.get(attributeName))) {
            return variables.get(attributeName);
        } else if (StringUtils.hasText(request.getParameter(attributeName))) {
            return request.getParameter(attributeName);
        } else {
            return null;
        }
    }

    protected final Map<String, String> getUriTemplateVariables(HttpServletRequest request) {
        Map<String, String> variables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables != null) {
            return variables;
        }

        return Collections.emptyMap();
    }

}