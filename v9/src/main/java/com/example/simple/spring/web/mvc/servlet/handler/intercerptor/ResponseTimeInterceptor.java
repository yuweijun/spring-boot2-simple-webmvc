package com.example.simple.spring.web.mvc.servlet.handler.intercerptor;

import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
public class ResponseTimeInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseTimeInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("System.currentTimeMillis", System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestURI = request.getRequestURI();
        final Long startTime = (Long) request.getAttribute("System.currentTimeMillis");
        Long cost = System.currentTimeMillis() - startTime;
        LOGGER.info("response time of request [{}] is : {}ms", requestURI, cost);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
