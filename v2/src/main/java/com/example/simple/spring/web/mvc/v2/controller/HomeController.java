package com.example.simple.spring.web.mvc.v2.controller;

import com.example.simple.spring.web.mvc.bind.annotation.GetMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

}
