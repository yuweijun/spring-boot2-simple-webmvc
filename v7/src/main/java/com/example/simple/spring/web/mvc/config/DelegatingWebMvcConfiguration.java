package com.example.simple.spring.web.mvc.config;

import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import com.example.simple.spring.web.mvc.servlet.handler.intercerptor.ResponseStatusInterceptor;
import com.example.simple.spring.web.mvc.servlet.handler.intercerptor.ResponseTimeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

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

}
