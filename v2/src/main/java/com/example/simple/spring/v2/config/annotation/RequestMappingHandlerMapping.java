/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * Whether to use suffix pattern match (".*") when matching patterns to requests. If enabled a method mapped to "/users" also matches to "/users.*".
     * <p>The default value is {@code true}.
     */
    public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
        this.useSuffixPatternMatch = useSuffixPatternMatch;
    }

    /**
     * Whether to match to URLs irrespective of the presence of a trailing slash. If enabled a method mapped to "/users" also matches to "/users/".
     * <p>The default value is {@code true}.
     */
    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
    }

    /**
     * Whether to use suffix pattern matching.
     */
    public boolean useSuffixPatternMatch() {
        return this.useSuffixPatternMatch;
    }

    /**
     * Whether to match to URLs irrespective of the presence of a trailing  slash.
     */
    public boolean useTrailingSlashMatch() {
        return this.useTrailingSlashMatch;
    }

    protected boolean isHandler(Class<?> beanType) {
        return AnnotationUtils.findAnnotation(beanType, Controller.class) != null;
    }

    /**
     * Uses method and type-level @{@link RequestMapping} annotations to create the RequestMappingInfo.
     *
     * @return the created RequestMappingInfo, or {@code null} if the method does not have a {@code @RequestMapping} annotation.
     */
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
