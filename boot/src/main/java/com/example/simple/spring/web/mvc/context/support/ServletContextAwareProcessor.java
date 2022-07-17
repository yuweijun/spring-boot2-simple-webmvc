package com.example.simple.spring.web.mvc.context.support;

import com.example.simple.spring.web.mvc.context.ServletContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletContextAwareProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextAwareProcessor.class);

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    protected ServletContextAwareProcessor() {
    }

    public ServletContextAwareProcessor(ServletContext servletContext) {
        this(servletContext, null);
    }

    public ServletContextAwareProcessor(ServletConfig servletConfig) {
        this(null, servletConfig);
    }

    public ServletContextAwareProcessor(  ServletContext servletContext,   ServletConfig servletConfig) {
        this.servletContext = servletContext;
        this.servletConfig = servletConfig;
    }

     
    protected ServletContext getServletContext() {
        if (this.servletContext == null && getServletConfig() != null) {
            final ServletContext context = getServletConfig().getServletContext();
            LOGGER.debug("get servletContext from servletConfig : {}", context);
            return context;
        }
        return this.servletContext;
    }

     
    protected ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (getServletContext() != null && bean instanceof ServletContextAware) {
            final ServletContext context = getServletContext();
            ((ServletContextAware) bean).setServletContext(context);
        }
        // if (getServletConfig() != null && bean instanceof ServletConfigAware) {
        //     ((ServletConfigAware) bean).setServletConfig(getServletConfig());
        // }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
