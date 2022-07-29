package com.example.simple.spring.web.mvc.servlet.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerExceptionResolver {

    boolean resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);

}
