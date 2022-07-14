package com.example.simple.spring.web.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Controller {

    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
