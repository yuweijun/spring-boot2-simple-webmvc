package com.example.simple.spring.boot.config;

import com.example.simple.spring.web.mvc.context.SimpleConfigurableWebApplicationContext;
import com.example.simple.spring.web.mvc.context.support.ServletContextAwareProcessor;
import org.springframework.util.Assert;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class SimpleWebApplicationContextServletContextAwareProcessor extends ServletContextAwareProcessor {

    private final SimpleConfigurableWebApplicationContext webApplicationContext;

    public SimpleWebApplicationContextServletContextAwareProcessor(SimpleConfigurableWebApplicationContext webApplicationContext) {
        Assert.notNull(webApplicationContext, "WebApplicationContext must not be null");
        this.webApplicationContext = webApplicationContext;
    }

    @Override
    protected ServletContext getServletContext() {
        ServletContext servletContext = this.webApplicationContext.getServletContext();
        return (servletContext != null) ? servletContext : super.getServletContext();
    }

    @Override
    protected ServletConfig getServletConfig() {
        ServletConfig servletConfig = this.webApplicationContext.getServletConfig();
        return (servletConfig != null) ? servletConfig : super.getServletConfig();
    }

}
