package com.example.simple.spring.v2.config.annotation;

import com.example.simple.spring.v2.web.bind.annotation.RequestMapping;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestMappingHandlerMapping {

    private boolean useSuffixPatternMatch = true;

    private boolean useTrailingSlashMatch = true;

    public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
        this.useSuffixPatternMatch = useSuffixPatternMatch;
    }

    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
    }

    public boolean useSuffixPatternMatch() {
        return this.useSuffixPatternMatch;
    }

    public boolean useTrailingSlashMatch() {
        return this.useTrailingSlashMatch;
    }

    protected boolean isHandler(Class<?> beanType) {
        return AnnotationUtils.findAnnotation(beanType, Controller.class) != null;
    }

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
        return new HashMap<>();
    }

    private Map<String, Object> createRequestMappingInfo(RequestMapping annotation, Class<?> handlerType) {
        return new HashMap<>();
    }

}
