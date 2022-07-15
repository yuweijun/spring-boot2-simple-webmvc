package com.example.simple.spring.web.mvc.config;

import com.example.simple.spring.web.mvc.context.ServletContextAware;
import com.example.simple.spring.web.mvc.controller.RootController;
import com.example.simple.spring.web.mvc.servlet.handler.BeanNameUrlHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.HttpRequestHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.SimpleControllerHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.RequestMappingHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContext;

public abstract class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServletContext servletContext;

    private ApplicationContext applicationContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        logger.debug("set servletContext in WebMvcConfigurationSupport");
        this.servletContext = servletContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.debug("set applicationContext in WebMvcConfigurationSupport");
        this.applicationContext = applicationContext;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        return handlerMapping;
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        return new HttpRequestHandlerAdapter();
    }

    @Bean
    public BeanNameUrlHandlerMapping beanNameUrlHandlerMapping() {
        return new BeanNameUrlHandlerMapping();
    }

    @Bean
    public RootController rootController() {
        return new RootController();
    }

}
