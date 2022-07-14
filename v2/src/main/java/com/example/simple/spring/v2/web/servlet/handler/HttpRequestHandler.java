package com.example.simple.spring.v2.web.servlet.handler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpRequestHandler {

    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
