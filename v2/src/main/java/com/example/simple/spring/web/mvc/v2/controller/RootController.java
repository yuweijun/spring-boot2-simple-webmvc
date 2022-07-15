package com.example.simple.spring.web.mvc.v2.controller;

import com.example.simple.spring.web.mvc.servlet.Controller;
import com.example.simple.spring.web.mvc.v2.servlet.handler.HttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RootController implements Controller, HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("root controller for request url '/'");
    }

}
