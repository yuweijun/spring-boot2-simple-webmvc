package com.example.simple.spring.web.mvc.annotation;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMethod;
import com.example.simple.spring.web.mvc.bind.annotation.RestController;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.AbstractDetectingUrlHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.HttpRequestHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DefaultAnnotationHandlerMapping extends AbstractDetectingUrlHandlerMapping {

    private boolean useDefaultSuffixPattern = true;

    private final Map<Class, RequestMapping> cachedMappings =  new HashMap<>();

    public void setUseDefaultSuffixPattern(boolean useDefaultSuffixPattern) {
        this.useDefaultSuffixPattern = useDefaultSuffixPattern;
    }

    @Override
    protected String[] determineUrlsForHandler(String beanName) {
        ApplicationContext context = getApplicationContext();
        Class<?> handlerType = context.getType(beanName);
        logger.debug("get handler for bean " + beanName);
        RequestMapping mapping = context.findAnnotationOnBean(beanName, RequestMapping.class);
        if (mapping != null) {
            logger.debug("get RequestMapping annotation from bean " + mapping);
            // @RequestMapping found at type level
            this.cachedMappings.put(handlerType, mapping);
            Set<String> urls =  new LinkedHashSet<>();
            String[] typeLevelPatterns = mapping.value();
            if (typeLevelPatterns.length > 0) {
                // @RequestMapping specifies paths at type level
                String[] methodLevelPatterns = determineUrlsForHandlerMethods(handlerType, true);
                for (String typeLevelPattern : typeLevelPatterns) {
                    if (!typeLevelPattern.startsWith("/")) {
                        typeLevelPattern = "/" + typeLevelPattern;
                    }
                    boolean hasEmptyMethodLevelMappings = false;
                    for (String methodLevelPattern : methodLevelPatterns) {
                        if (methodLevelPattern == null) {
                            hasEmptyMethodLevelMappings = true;
                        } else {
                            String combinedPattern = getPathMatcher().combine(typeLevelPattern, methodLevelPattern);
                            addUrlsForPath(urls, combinedPattern);
                        }
                    }
                    if (hasEmptyMethodLevelMappings || HttpRequestHandler.class.isAssignableFrom(handlerType)) {
                        addUrlsForPath(urls, typeLevelPattern);
                    }
                }
                return StringUtils.toStringArray(urls);
            } else {
                // actual paths specified by @RequestMapping at method level
                return determineUrlsForHandlerMethods(handlerType, false);
            }
        } else if (AnnotationUtils.findAnnotation(handlerType, RestController.class) != null || AnnotationUtils.findAnnotation(handlerType, Controller.class) != null) {
            // @RequestMapping to be introspected at method level
            return determineUrlsForHandlerMethods(handlerType, false);
        } else {
            return null;
        }
    }

    protected String[] determineUrlsForHandlerMethods(Class<?> handlerType, final boolean hasTypeLevelMapping) {
        String[] subclassResult = determineUrlsForHandlerMethods(handlerType);
        if (subclassResult != null) {
            return subclassResult;
        }

        final Set<String> urls =  new LinkedHashSet<>();
        Set<Class<?>> handlerTypes =  new LinkedHashSet<>();
        handlerTypes.add(handlerType);
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                    if (mapping != null) {
                        String[] mappedPatterns = mapping.value();
                        if (mappedPatterns.length > 0) {
                            for (String mappedPattern : mappedPatterns) {
                                if (!hasTypeLevelMapping && !mappedPattern.startsWith("/")) {
                                    mappedPattern = "/" + mappedPattern;
                                }
                                addUrlsForPath(urls, mappedPattern);
                            }
                        } else if (hasTypeLevelMapping) {
                            // empty method-level RequestMapping
                            urls.add(null);
                        }
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return StringUtils.toStringArray(urls);
    }

    protected String[] determineUrlsForHandlerMethods(Class<?> handlerType) {
        return null;
    }

    protected void addUrlsForPath(Set<String> urls, String path) {
        urls.add(path);
        if (this.useDefaultSuffixPattern && path.indexOf('.') == -1 && !path.endsWith("/")) {
            urls.add(path + ".*");
            urls.add(path + "/");
        }
    }

    @Override
    protected void validateHandler(Object handler, HttpServletRequest request) throws Exception {
        RequestMapping mapping = this.cachedMappings.get(handler.getClass());
        if (mapping == null) {
            mapping = AnnotationUtils.findAnnotation(handler.getClass(), RequestMapping.class);
        }
        if (mapping != null) {
            validateMapping(mapping, request);
        }
    }

    protected void validateMapping(RequestMapping mapping, HttpServletRequest request) throws Exception {
        RequestMethod[] mappedMethods = mapping.method();
        if (!ServletAnnotationMappingUtils.checkRequestMethod(mappedMethods, request)) {
            String[] supportedMethods = new String[mappedMethods.length];
            for (int i = 0; i < mappedMethods.length; i++) {
                supportedMethods[i] = mappedMethods[i].name();
            }
            logger.info("supportedMethods : " + supportedMethods);
            throw new IllegalArgumentException(request.getMethod());
        }

        String[] mappedParams = mapping.params();
        if (!ServletAnnotationMappingUtils.checkParameters(mappedParams, request)) {
            throw new IllegalArgumentException("params : " + mappedParams + request.getParameterMap());
        }

        String[] mappedHeaders = mapping.headers();
        if (!ServletAnnotationMappingUtils.checkHeaders(mappedHeaders, request)) {
            throw new ServletException("Header conditions \"" + StringUtils.arrayToDelimitedString(mappedHeaders, ", ") + "\" not met for actual request");
        }
    }

    @Override
    protected boolean supportsTypeLevelMappings() {
        return true;
    }
}
