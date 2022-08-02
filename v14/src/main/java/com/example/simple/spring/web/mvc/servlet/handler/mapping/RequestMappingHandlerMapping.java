package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.method.RequestMappingInfo;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class RequestMappingHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> implements HandlerMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerMapping.class);

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

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
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        // AnnotationUtils.findAnnotation not support @GetMapping and @PostMapping
        // RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        final Set<RequestMapping> allMergedAnnotations = AnnotatedElementUtils.getAllMergedAnnotations(method, RequestMapping.class);

        RequestMappingInfo childInfo = null;
        if (!allMergedAnnotations.isEmpty()) {
            childInfo = createRequestMappingInfo(allMergedAnnotations, method);
        }

        final RequestMappingInfo parentInfo = getMappingInfoFromType(handlerType);
        if (childInfo != null || parentInfo != null) {
            return combine(parentInfo, childInfo);
        } else {
            logger.debug("method is not a handler request mapping : " + method.getName());
            return null;
        }
    }

    private RequestMappingInfo combine(RequestMappingInfo parent, RequestMappingInfo child) {
        if (parent == null) {
            return child;
        } else if (child == null) {
            return parent;
        }

        final String[] parentPath = parent.getPath();
        final String[] childPath = child.getPath();
        Set<String> result = new LinkedHashSet<>();
        for (String pattern1 : parentPath) {
            for (String pattern2 : childPath) {
                result.add(antPathMatcher.combine(pattern1, pattern2));
            }
        }

        LOGGER.debug("path before combine is {}", Arrays.toString(child.getPath()));
        final String[] combinedPath = result.toArray(new String[0]);

        child.put("path", combinedPath);
        LOGGER.debug("path after combine is {}", Arrays.toString(child.getPath()));
        return child;
    }

    private RequestMappingInfo getMappingInfoFromType(Class<?> handlerType) {
        Set<RequestMapping> typeAnnotations = AnnotatedElementUtils.getAllMergedAnnotations(handlerType, RequestMapping.class);
        if (!typeAnnotations.isEmpty()) {
            return createRequestMappingInfo(typeAnnotations, handlerType);
        }

        return null;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    private RequestMappingInfo getAnnotationAttributes(RequestMapping annotation) {
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

        return new RequestMappingInfo(values);
    }

    private RequestMappingInfo createRequestMappingInfo(Set<RequestMapping> annotations, Method customCondition) {
        LOGGER.debug("createRequestMappingInfo by method : {}", annotations);
        return getAnnotationAttributes(annotations.iterator().next());
    }

    private RequestMappingInfo createRequestMappingInfo(Set<RequestMapping> annotations, Class<?> handlerType) {
        LOGGER.debug("createRequestMappingInfo by class : {}", annotations);
        return getAnnotationAttributes(annotations.iterator().next());
    }

    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo mapping) {
        final String[] value = mapping.getValue();

        return prependLeadingSlash(Arrays.asList(value));
    }

    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo mapping, HttpServletRequest request) {
        final Set<String> mappingPathPatterns = getMappingPathPatterns(mapping);
        final String requestURI = request.getRequestURI();
        if (mappingPathPatterns.contains(requestURI)) {
            logger.debug("request [" + requestURI + "] match mapping : " + mapping);
            return mapping;
        }

        return null;
    }

}
