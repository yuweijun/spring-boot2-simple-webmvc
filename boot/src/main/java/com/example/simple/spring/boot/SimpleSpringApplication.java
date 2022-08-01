package com.example.simple.spring.boot;

import com.example.simple.spring.web.mvc.context.SimpleAnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SimpleSpringApplication extends SpringApplication {

    public SimpleSpringApplication(Class<?>... primarySources) {
        super(null, primarySources);
    }

    @Override
    protected ConfigurableApplicationContext createApplicationContext() {
        return new SimpleAnnotationConfigServletWebServerApplicationContext();
    }

}
