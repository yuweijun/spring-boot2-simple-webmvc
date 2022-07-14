package com.example.simple.spring.web.mvc.v2.controller;

import com.example.simple.spring.web.mvc.bind.annotation.GetMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
