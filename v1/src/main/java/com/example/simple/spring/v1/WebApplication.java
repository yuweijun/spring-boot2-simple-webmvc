package com.example.simple.spring.v1;

import com.example.simple.spring.v1.config.boot.SimpleSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        new SimpleSpringApplication(WebApplication.class).run(args);
    }

}
