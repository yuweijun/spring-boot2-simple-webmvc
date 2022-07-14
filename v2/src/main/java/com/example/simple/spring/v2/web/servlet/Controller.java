package com.example.simple.spring.v2.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Controller {

    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
