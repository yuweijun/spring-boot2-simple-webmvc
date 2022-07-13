package com.example.simple.spring.v2.config.boot;

import com.example.simple.spring.v2.web.context.ServletWebServerApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @since 2022-07-13.
 */
public class SimpleSpringApplication extends SpringApplication {

    public SimpleSpringApplication(Class<?>... primarySources) {
        super(null, primarySources);
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        return new ServletWebServerApplicationContext();
    }

}
