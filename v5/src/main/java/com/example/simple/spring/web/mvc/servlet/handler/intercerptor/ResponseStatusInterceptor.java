package com.example.simple.spring.web.mvc.servlet.handler.intercerptor;

import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 2022-07-16.
 */
public class ResponseStatusInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseStatusInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        final int status = response.getStatus();
        final String requestURI = request.getRequestURI();
        LOGGER.info("response status of request [{}] is : {}", requestURI, status);
    }
}
