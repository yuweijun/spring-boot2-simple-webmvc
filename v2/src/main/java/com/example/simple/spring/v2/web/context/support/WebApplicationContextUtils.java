package com.example.simple.spring.v2.web.context.support;

import com.example.simple.spring.v2.web.context.SimpleConfigurableWebApplicationContext;
import com.example.simple.spring.v2.web.context.SimpleWebApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public abstract class WebApplicationContextUtils {

    public static SimpleWebApplicationContext getWebApplicationContext(ServletContext sc) {
        return getWebApplicationContext(sc, SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    public static SimpleWebApplicationContext getWebApplicationContext(ServletContext sc, String attrName) {
        Assert.notNull(sc, "ServletContext must not be null");
        Object attr = sc.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (attr instanceof RuntimeException) {
            throw (RuntimeException) attr;
        }
        if (attr instanceof Error) {
            throw (Error) attr;
        }
        if (attr instanceof Exception) {
            throw new IllegalStateException((Exception) attr);
        }
        if (!(attr instanceof SimpleWebApplicationContext)) {
            throw new IllegalStateException("Context attribute is not of type WebApplicationContext: " + attr);
        }
        return (SimpleWebApplicationContext) attr;
    }

    public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext sc) {
        registerEnvironmentBeans(bf, sc, null);
    }

    public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext sc, ServletConfig config) {

        if (sc != null && !bf.containsBean(SimpleWebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
            bf.registerSingleton(SimpleWebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, sc);
        }

        if (config != null && !bf.containsBean(SimpleConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
            bf.registerSingleton(SimpleConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, config);
        }

        if (!bf.containsBean(SimpleWebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
            Map<String, String> parameterMap = new HashMap<String, String>();
            if (sc != null) {
                Enumeration<?> paramNameEnum = sc.getInitParameterNames();
                while (paramNameEnum.hasMoreElements()) {
                    String paramName = (String) paramNameEnum.nextElement();
                    parameterMap.put(paramName, sc.getInitParameter(paramName));
                }
            }
            if (config != null) {
                Enumeration<?> paramNameEnum = config.getInitParameterNames();
                while (paramNameEnum.hasMoreElements()) {
                    String paramName = (String) paramNameEnum.nextElement();
                    parameterMap.put(paramName, config.getInitParameter(paramName));
                }
            }
            bf.registerSingleton(SimpleWebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME, Collections.unmodifiableMap(parameterMap));
        }

        if (!bf.containsBean(SimpleWebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
            Map<String, Object> attributeMap = new HashMap<String, Object>();
            if (sc != null) {
                Enumeration<?> attrNameEnum = sc.getAttributeNames();
                while (attrNameEnum.hasMoreElements()) {
                    String attrName = (String) attrNameEnum.nextElement();
                    attributeMap.put(attrName, sc.getAttribute(attrName));
                }
            }
            bf.registerSingleton(SimpleWebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME, Collections.unmodifiableMap(attributeMap));
        }
    }

}
