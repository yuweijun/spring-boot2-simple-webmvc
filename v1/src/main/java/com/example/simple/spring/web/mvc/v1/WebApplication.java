package com.example.simple.spring.web.mvc.v1;

import com.example.simple.spring.boot.SimpleSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.simple.spring"})
public class WebApplication {

    public static void main(String[] args) {
        new SimpleSpringApplication(WebApplication.class).run(args);
    }

}
