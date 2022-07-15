package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.servlet.SimpleController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RootController implements SimpleController {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("root controller for request url '/'");
    }

}
