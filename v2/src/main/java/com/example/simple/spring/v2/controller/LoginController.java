package com.example.simple.spring.v2.controller;

import com.example.simple.spring.v2.web.bind.annotation.GetMapping;
import com.example.simple.spring.v2.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
