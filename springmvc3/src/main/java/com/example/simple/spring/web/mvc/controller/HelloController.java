package com.example.simple.spring.web.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class HelloController {

    @RequestMapping("/hello")
    public String hello(Map<String, String> model) {
        model.put("msg", "using RequestMappingHandlerMapping");

        return "hello";
    }

}
