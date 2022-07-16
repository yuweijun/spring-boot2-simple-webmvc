package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.HandlerExecutionChain;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.AbstractHandlerMethodMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RequestMappingHandlerMapping extends AbstractHandlerMethodMapping<Map<String, Object>> implements HandlerMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerMapping.class);

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

    protected boolean isHandler(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
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
    protected Set<String> getMappingPathPatterns(Map<String, Object> mapping) {
        return new HashSet<>();
    }

    @Override
    protected Map<String, Object> getMatchingMapping(Map<String, Object> mapping, HttpServletRequest request) {
        return new HashMap<>();
    }

    @Override
    protected Comparator<Map<String, Object>> getMappingComparator(HttpServletRequest request) {
        return (map1, map2) -> {
            logger.debug(map1);
            logger.debug(map2);
            return 0;
        };
    }

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        LOGGER.debug("handler is {}", handler);
        if (handler instanceof HandlerExecutionChain) {
            return (HandlerExecutionChain) handler;
        }
        return new HandlerExecutionChain(handler);
    }

}
