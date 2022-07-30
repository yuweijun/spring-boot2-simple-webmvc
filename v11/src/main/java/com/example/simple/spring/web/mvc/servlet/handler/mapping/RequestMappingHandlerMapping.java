package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RequestMappingHandlerMapping extends AbstractHandlerMethodMapping<Map<String, Object>> implements HandlerMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerMapping.class);

    @Override
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

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    private Map<String, Object> getAnnotationAttributes(RequestMapping annotation) {
        final HashMap<String, Object> values = new HashMap<>();
        final Method[] declaredMethods = annotation.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getParameterCount() == 0) {
                try {
                    values.put(method.getName(), method.invoke(annotation));
                } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return values;
    }

    private Map<String, Object> createRequestMappingInfo(RequestMapping annotation, Method customCondition) {
        LOGGER.debug("createRequestMappingInfo by method : {}", annotation);
        return getAnnotationAttributes(annotation);
    }

    private Map<String, Object> createRequestMappingInfo(RequestMapping annotation, Class<?> handlerType) {
        LOGGER.debug("createRequestMappingInfo by class : {}", annotation);
        return getAnnotationAttributes(annotation);
    }

    @Override
    protected Set<String> getMappingPathPatterns(Map<String, Object> mapping) {
        final String[] value = (String[]) mapping.get("value");

        return prependLeadingSlash(Arrays.asList(value));
    }

    private static Set<String> prependLeadingSlash(Collection<String> patterns) {
        if (patterns == null) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>(patterns.size());
        for (String pattern : patterns) {
            if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    @Override
    protected Map<String, Object> getMatchingMapping(Map<String, Object> mapping, HttpServletRequest request) {
        final Set<String> mappingPathPatterns = getMappingPathPatterns(mapping);
        final String requestURI = request.getRequestURI();
        if (mappingPathPatterns.contains(requestURI)) {
            logger.debug("request [" + requestURI + "] match mapping : " + mapping);
            return mapping;
        }

        return null;
    }

}
