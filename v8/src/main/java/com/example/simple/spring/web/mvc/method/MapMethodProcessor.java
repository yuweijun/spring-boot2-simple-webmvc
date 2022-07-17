package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapMethodProcessor implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    private final Log logger = LogFactory.getLog(getClass());

    public boolean supportsParameter(MethodParameter parameter) {
        return Map.class.isAssignableFrom(parameter.getParameterType());
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return Map.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        return new LinkedHashMap<>();
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (returnValue != null) {
            logger.debug("return value is " + returnValue);
        }
    }
}