package com.example.simple.spring.web.mvc.servlet.handler;

import com.example.simple.spring.web.mvc.servlet.HandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.SimpleController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleControllerHandlerAdapter implements HandlerAdapter {

    public boolean supports(Object handler) {
        return (handler instanceof SimpleController);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        ((SimpleController) handler).handleRequest(request, response);
    }

}
