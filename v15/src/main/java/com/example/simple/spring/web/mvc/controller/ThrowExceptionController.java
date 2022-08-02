package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException;
import com.example.simple.spring.web.mvc.controller.exception.NotFoundException;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.servlet.error.DefaultErrorAttributes;
import com.example.simple.spring.web.mvc.servlet.exception.ResponseStatusException;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ThrowExceptionController {

    @RequestMapping("/loginFailed")
    public void loginFailed() {
        throw new AuthenticationFailedException("Authentication Failed");
    }

    @RequestMapping("/illegalArgumentException")
    public void illegalArgumentException(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IllegalArgumentException error = new IllegalArgumentException("throws IllegalArgumentException from ThrowExceptionController");
        DefaultErrorAttributes.storeErrorInRequest(request, error);
        response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
    }

    @RequestMapping("/notFound")
    public void notFound() {
        throw new NotFoundException("404");
    }

    @RequestMapping("/responseStatus")
    public void responseStatus() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

}
