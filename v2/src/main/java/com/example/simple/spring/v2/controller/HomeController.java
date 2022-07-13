package com.example.simple.spring.v2.controller;

import com.example.simple.spring.v2.web.bind.annotation.GetMapping;
import com.example.simple.spring.v2.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

}
