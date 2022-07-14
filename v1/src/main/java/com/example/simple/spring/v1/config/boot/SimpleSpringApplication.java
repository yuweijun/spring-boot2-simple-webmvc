package com.example.simple.spring.v1.config.boot;

import com.example.simple.spring.v1.web.context.SimpleAnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SimpleSpringApplication extends SpringApplication {

    public SimpleSpringApplication(Class<?>... primarySources) {
        super(null, primarySources);
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        return new SimpleAnnotationConfigServletWebServerApplicationContext();
    }

}
