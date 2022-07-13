package com.example.simple.spring.v2;

import com.example.simple.spring.v2.config.annotation.EnableWebMvc;
import com.example.simple.spring.v2.config.boot.SimpleSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableWebMvc
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        new SimpleSpringApplication(WebApplication.class).run(args);
    }

}
