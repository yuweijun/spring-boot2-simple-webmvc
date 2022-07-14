package com.example.simple.spring.v2.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleControllerHandlerAdapter implements HandlerAdapter {

    public boolean supports(Object handler) {
        return (handler instanceof Controller);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {

        ((Controller) handler).handleRequest(request, response);
    }

}
