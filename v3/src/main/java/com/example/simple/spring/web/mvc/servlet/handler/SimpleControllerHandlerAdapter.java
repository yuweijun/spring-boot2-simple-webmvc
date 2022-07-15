package com.example.simple.spring.web.mvc.servlet.handler;

import com.example.simple.spring.web.mvc.servlet.Controller;
import com.example.simple.spring.web.mvc.servlet.HandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleControllerHandlerAdapter implements HandlerAdapter {

    public boolean supports(Object handler) {
        return (handler instanceof Controller);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ((Controller) handler).handleRequest(request, response);
    }

}
