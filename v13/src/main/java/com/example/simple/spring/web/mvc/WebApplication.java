package com.example.simple.spring.web.mvc;

import com.example.simple.spring.boot.SimpleSpringApplication;
import com.example.simple.spring.web.mvc.config.EnableWebMvc;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableWebMvc
@SpringBootApplication(scanBasePackages = {"com.example.simple.spring"})
public class WebApplication {

    public static void main(String[] args) {
        new SimpleSpringApplication(WebApplication.class).run(args);
    }

}
