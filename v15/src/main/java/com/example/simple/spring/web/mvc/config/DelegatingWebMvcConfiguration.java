package com.example.simple.spring.web.mvc.config;

import com.example.simple.spring.web.mvc.bind.annotation.RequestScope;
import com.example.simple.spring.web.mvc.bind.annotation.SessionScope;
import com.example.simple.spring.web.mvc.controller.dto.RequestScopeBean;
import com.example.simple.spring.web.mvc.controller.dto.SessionScopeBean;
import com.example.simple.spring.web.mvc.controller.interceptor.ResponseStatusInterceptor;
import com.example.simple.spring.web.mvc.controller.interceptor.ResponseTimeInterceptor;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import com.example.simple.spring.web.mvc.servlet.view.InternalResourceViewResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

    private final Log logger = LogFactory.getLog(getClass());

    @Bean
    public ResponseStatusInterceptor responseStatusInterceptor() {
        return new ResponseStatusInterceptor();
    }

    @Bean
    public ResponseTimeInterceptor responseTimeInterceptor() {
        return new ResponseTimeInterceptor();
    }

    @Override
    protected void addInterceptors(List<HandlerInterceptor> registry) {
        registry.add(responseTimeInterceptor());
        registry.add(responseStatusInterceptor());
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalResourceViewResolver defaultViewResolver() {
        logger.info("create bean : defaultViewResolver");
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean
    @RequestScope
    public RequestScopeBean requestScopeBean() {
        final RequestScopeBean requestScopeBean = new RequestScopeBean();
        logger.info("get RequestScopeBean with index : " + requestScopeBean.getIndex());
        return requestScopeBean;
    }

    @Bean
    @SessionScope
    public SessionScopeBean sessionScopeBean() {
        final SessionScopeBean sessionScopeBean = new SessionScopeBean();
        logger.info("get SessionScopeBean with index : " + sessionScopeBean.getIndex());
        return sessionScopeBean;
    }

}
