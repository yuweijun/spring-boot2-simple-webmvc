package com.example.simple.spring.web.mvc.v2.config;

import com.example.simple.spring.web.mvc.config.annotation.RequestMappingHandlerMapping;
import com.example.simple.spring.web.mvc.context.ServletContextAware;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import com.example.simple.spring.web.mvc.v2.servlet.handler.SimpleControllerHandlerAdapter;
import com.example.simple.spring.web.mvc.v2.servlet.handler.AbstractHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public abstract class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServletContext servletContext;

    private ApplicationContext applicationContext;

    private List<Object> interceptors;

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
    public HandlerMapping defaultServletHandlerMapping() {
        DefaultServletHandlerConfigurer configurer = new DefaultServletHandlerConfigurer(servletContext);
        configureDefaultServletHandling(configurer);
        AbstractHandlerMapping handlerMapping = configurer.getHandlerMapping();
        handlerMapping = handlerMapping != null ? handlerMapping : new EmptyHandlerMapping();
        return handlerMapping;
    }

    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable("dispatcherServlet");
    }

    @Bean
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        addFormatters(conversionService);
        return conversionService;
    }

    protected void addFormatters(FormatterRegistry registry) {
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    private final static class EmptyHandlerMapping extends AbstractHandlerMapping {
        @Override
        protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
            return null;
        }
    }

}
