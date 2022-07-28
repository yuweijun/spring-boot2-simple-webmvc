package com.example.spring.boot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TomcatJspApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TomcatJspApplication.class).run(args);
    }

}
