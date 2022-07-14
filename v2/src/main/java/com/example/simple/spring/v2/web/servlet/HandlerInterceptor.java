package com.example.simple.spring.v2.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerInterceptor {

    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception;

    void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception;

    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception;

}
