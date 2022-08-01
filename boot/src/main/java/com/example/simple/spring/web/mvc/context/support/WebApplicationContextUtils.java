package com.example.simple.spring.web.mvc.context.support;

import com.example.simple.spring.web.mvc.context.SimpleConfigurableWebApplicationContext;
import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public abstract class WebApplicationContextUtils {

    public static SimpleWebApplicationContext getWebApplicationContext(ServletContext servletContext) {
        return getWebApplicationContext(servletContext, SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    public static SimpleWebApplicationContext getWebApplicationContext(ServletContext servletContext, String attrName) {
        Assert.notNull(servletContext, "ServletContext must not be null");
        Object attr = servletContext.getAttribute(attrName);
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

    public static void registerEnvironmentBeans(ConfigurableListableBeanFactory beanFactory, ServletContext servletContext) {
        registerEnvironmentBeans(beanFactory, servletContext, null);
    }

    public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext servletContext, ServletConfig config) {
        if (servletContext != null && !bf.containsBean(SimpleWebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
            bf.registerSingleton(SimpleWebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, servletContext);
        }

        if (config != null && !bf.containsBean(SimpleConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
            bf.registerSingleton(SimpleConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, config);
        }

        if (!bf.containsBean(SimpleWebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
            Map<String, String> parameterMap = new HashMap<>();
            if (servletContext != null) {
                Enumeration<?> paramNameEnum = servletContext.getInitParameterNames();
                while (paramNameEnum.hasMoreElements()) {
                    String paramName = (String) paramNameEnum.nextElement();
                    parameterMap.put(paramName, servletContext.getInitParameter(paramName));
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
            Map<String, Object> attributeMap = new HashMap<>();
            if (servletContext != null) {
                Enumeration<?> attrNameEnum = servletContext.getAttributeNames();
                while (attrNameEnum.hasMoreElements()) {
                    String attrName = (String) attrNameEnum.nextElement();
                    attributeMap.put(attrName, servletContext.getAttribute(attrName));
                }
            }
            bf.registerSingleton(SimpleWebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME, Collections.unmodifiableMap(attributeMap));
        }
    }

    public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory) {
        registerWebApplicationScopes(beanFactory, null);
    }

    public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory, ServletContext servletContext) {
        beanFactory.registerScope(SimpleWebApplicationContext.SCOPE_REQUEST, new RequestScope());
        beanFactory.registerScope(SimpleWebApplicationContext.SCOPE_SESSION, new SessionScope());
        if (servletContext != null) {
            ServletContextScope appScope = new ServletContextScope(servletContext);
            beanFactory.registerScope(SimpleWebApplicationContext.SCOPE_APPLICATION, appScope);
            // Register as ServletContext attribute, for ContextCleanupListener to detect it.
            servletContext.setAttribute(ServletContextScope.class.getName(), appScope);
        }
    }

}
