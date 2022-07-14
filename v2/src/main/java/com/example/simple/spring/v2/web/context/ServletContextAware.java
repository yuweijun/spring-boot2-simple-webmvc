package com.example.simple.spring.v2.web.context;

import org.springframework.beans.factory.Aware;

import javax.servlet.ServletContext;

public interface ServletContextAware extends Aware {

    void setServletContext(ServletContext servletContext);

}
