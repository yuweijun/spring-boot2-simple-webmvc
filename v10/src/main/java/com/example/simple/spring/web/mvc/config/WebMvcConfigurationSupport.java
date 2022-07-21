package com.example.simple.spring.web.mvc.config;

import com.example.simple.spring.web.mvc.context.ServletContextAware;
import com.example.simple.spring.web.mvc.controller.RootController;
import com.example.simple.spring.web.mvc.controller.exception.NotFoundHandlerExceptionResolver;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.json.MappingJackson2HttpMessageConverter;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import com.example.simple.spring.web.mvc.servlet.exception.ExceptionHandlerExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.exception.HandlerExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.exception.HandlerExceptionResolverComposite;
import com.example.simple.spring.web.mvc.servlet.exception.ResponseStatusExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.handler.HttpRequestHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.RequestMappingHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.SimpleControllerHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.intercerptor.MappedInterceptor;
import com.example.simple.spring.web.mvc.servlet.handler.intercerptor.RequestLoggerInterceptor;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.BeanNameUrlHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.RequestMappingHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.SimpleUrlHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

public abstract class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<HandlerInterceptor> interceptors;

    private List<HttpMessageConverter<?>> messageConverters;

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
    public RootController rootController() {
        return new RootController();
    }

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setRootHandler(rootController());
        handlerMapping.setOrder(1);
        handlerMapping.setInterceptors(getInterceptors());
        return handlerMapping;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        handlerMapping.setInterceptors(getInterceptors());
        return handlerMapping;
    }

    protected final HandlerInterceptor[] getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
            addInterceptors(interceptors);
        }
        return interceptors.toArray(new HandlerInterceptor[0]);
    }

    protected void addInterceptors(List<HandlerInterceptor> registry) {
    }

    protected final List<HttpMessageConverter<?>> getMessageConverters() {
        if (messageConverters == null) {
            messageConverters = new ArrayList<>();
            configureMessageConverters(messageConverters);
            if (messageConverters.isEmpty()) {
                messageConverters.add(new MappingJackson2HttpMessageConverter());
            }
        }
        return messageConverters;
    }

    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        final RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.setMessageConverters(getMessageConverters());

        return adapter;
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
        final BeanNameUrlHandlerMapping handlerMapping = new BeanNameUrlHandlerMapping();
        handlerMapping.setOrder(2);
        handlerMapping.setInterceptors(getInterceptors());

        return handlerMapping;
    }

    @Bean
    public RequestLoggerInterceptor requestLoggerInterceptor() {
        return new RequestLoggerInterceptor();
    }

    @Bean
    public MappedInterceptor mappedInterceptor() {
        String[] pathPatterns = new String[]{"/"};
        return new MappedInterceptor(pathPatterns, requestLoggerInterceptor());
    }

    @Bean
    public HandlerExceptionResolverComposite handlerExceptionResolver() throws Exception {
        List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();

        ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setMessageConverters(getMessageConverters());
        exceptionHandlerExceptionResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerExceptionResolver);
        exceptionResolvers.add(new ResponseStatusExceptionResolver());
        exceptionResolvers.add(new NotFoundHandlerExceptionResolver());

        HandlerExceptionResolverComposite handlerExceptionResolver = new HandlerExceptionResolverComposite();
        handlerExceptionResolver.setExceptionResolvers(exceptionResolvers);
        return handlerExceptionResolver;
    }
}
