package com.example.simple.spring.web.mvc.servlet.config;

import com.example.simple.spring.web.mvc.servlet.DispatcherServlet;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.util.Assert;

import java.util.Collection;

public class SimpleDispatcherServletRegistrationBean extends ServletRegistrationBean<DispatcherServlet>
    implements DispatcherServletPath {

    private final String path;

    public SimpleDispatcherServletRegistrationBean(DispatcherServlet servlet, String path) {
        super(servlet);
        Assert.notNull(path, "Path must not be null");
        this.path = path;
        super.addUrlMappings(getServletUrlMapping());
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setUrlMappings(Collection<String> urlMappings) {
        throw new UnsupportedOperationException("URL Mapping cannot be changed on a DispatcherServlet registration");
    }

    @Override
    public void addUrlMappings(String... urlMappings) {
        throw new UnsupportedOperationException("URL Mapping cannot be changed on a DispatcherServlet registration");
    }

}
