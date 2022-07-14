package com.example.simple.spring.v2.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {

    boolean supports(Object handler);

    void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

}
