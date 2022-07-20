package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException;
import org.springframework.stereotype.Controller;

@Controller
public class ThrowAuthenticationFailedExceptionController {

    @RequestMapping("/loginFailed")
    public void loginFailed() {
        throw new AuthenticationFailedException("Authentication Failed");
    }

}
