package com.example.simple.spring.web.mvc.v2.config;

import com.example.simple.spring.web.mvc.v2.servlet.handler.AbstractHandlerMapping;
import com.example.simple.spring.web.mvc.v2.servlet.handler.DefaultServletHttpRequestHandler;
import com.example.simple.spring.web.mvc.v2.servlet.handler.HttpRequestHandler;
import com.example.simple.spring.web.mvc.v2.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

public class DefaultServletHandlerConfigurer {

    private final ServletContext servletContext;

    private DefaultServletHttpRequestHandler handler;

    public DefaultServletHandlerConfigurer(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void enable() {
        enable(null);
    }

    public void enable(String defaultServletName) {
        handler = new DefaultServletHttpRequestHandler();
        handler.setDefaultServletName(defaultServletName);
        handler.setServletContext(servletContext);
    }

    protected AbstractHandlerMapping getHandlerMapping() {
        if (handler == null) {
            return null;
        }

        Map<String, HttpRequestHandler> urlMap = new HashMap<String, HttpRequestHandler>();
        urlMap.put("/**", handler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(Integer.MAX_VALUE);
        handlerMapping.setUrlMap(urlMap);
        return handlerMapping;
    }

}