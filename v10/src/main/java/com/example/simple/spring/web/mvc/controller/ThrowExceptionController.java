package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException;
import com.example.simple.spring.web.mvc.controller.exception.NotFoundException;
import org.springframework.stereotype.Controller;

@Controller
public class ThrowExceptionController {

    @RequestMapping("/loginFailed")
    public void loginFailed() {
        throw new AuthenticationFailedException("Authentication Failed");
    }

    @RequestMapping("/notFound")
    public void notFound() {
        throw new NotFoundException("404");
    }

}
