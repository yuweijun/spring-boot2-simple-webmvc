package com.example.simple.spring.v1.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatcherServlet extends HttpServlet {

    public DispatcherServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        out.write("com.example.simple.spring.v1.web.servlet.DispatcherServlet".getBytes());
        out.flush();
        out.close();
    }

}
