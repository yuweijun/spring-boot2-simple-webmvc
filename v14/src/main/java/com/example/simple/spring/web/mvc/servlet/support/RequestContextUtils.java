package com.example.simple.spring.web.mvc.servlet.support;

import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.context.support.WebApplicationContextUtils;
import com.example.simple.spring.web.mvc.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

public abstract class RequestContextUtils {

    public static SimpleWebApplicationContext getWebApplicationContext(ServletRequest request) throws IllegalStateException {
        return getWebApplicationContext(request, null);
    }

    public static SimpleWebApplicationContext getWebApplicationContext(ServletRequest request, ServletContext servletContext) throws IllegalStateException {
        SimpleWebApplicationContext webApplicationContext = (SimpleWebApplicationContext) request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (webApplicationContext == null) {
            if (servletContext == null) {
                throw new IllegalStateException("No WebApplicationContext found: not in a DispatcherServlet request?");
            }
            webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        }
        return webApplicationContext;
    }

}
