package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.HandlerExecutionChain;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import com.example.simple.spring.web.mvc.util.UrlPathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotationUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping extends ApplicationObjectSupport implements HandlerMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerMapping.class);

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    protected Map<String, Object> getMappingForMethod(Method method, Class<?> handlerType) {
        Map<String, Object> info = null;
        RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (methodAnnotation != null) {
            info = createRequestMappingInfo(methodAnnotation, method);
            RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
            if (typeAnnotation != null) {
                info = createRequestMappingInfo(typeAnnotation, handlerType);
            }
        }
        return info;
    }

    private Map<String, Object> createRequestMappingInfo(RequestMapping annotation, Method customCondition) {
        LOGGER.debug("createRequestMappingInfo by method : {}", annotation);
        return new HashMap<>();
    }

    private Map<String, Object> createRequestMappingInfo(RequestMapping annotation, Class<?> handlerType) {
        LOGGER.debug("createRequestMappingInfo by class : {}", annotation);
        return new HashMap<>();
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }
        return getHandlerExecutionChain(handler, request);
    }

    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String lookupPath = urlPathHelper.getLookupPathForRequest(request);
        Object rawHandler = null;
        if ("/".equals(lookupPath)) {
            rawHandler = getRootHandler();
        }
        if (rawHandler != null) {
            LOGGER.debug("rawHandler is {}", rawHandler);
            if (rawHandler instanceof String) {
                String handlerName = (String) rawHandler;
                rawHandler = getApplicationContext().getBean(handlerName);
            }

            Object handler = buildPathExposingHandler(rawHandler, lookupPath);
            LOGGER.debug("Mapping [{}] lookupPath to {}", lookupPath, handler);
            return handler;
        }
        LOGGER.debug("get handler failed");
        return null;
    }

    private Object getRootHandler() {
        return "rootController";
    }

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        LOGGER.debug("handler is {}", handler);
        if (handler instanceof HandlerExecutionChain) {
            return (HandlerExecutionChain) handler;
        }
        return new HandlerExecutionChain(handler);
    }

    protected Object buildPathExposingHandler(Object rawHandler, String bestMatchingPattern) {
        HandlerExecutionChain chain = new HandlerExecutionChain(rawHandler);
        return chain;
    }
}
