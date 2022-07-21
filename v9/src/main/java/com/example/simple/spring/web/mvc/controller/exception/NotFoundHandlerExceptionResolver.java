package com.example.simple.spring.web.mvc.controller.exception;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.http.MediaType;
import com.example.simple.spring.web.mvc.servlet.exception.AbstractHandlerExceptionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotFoundHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    private final Log logger = LogFactory.getLog(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex instanceof NotFoundException) {
            return handleNotFoundException((NotFoundException) ex, request, response, handler);
        }
        return false;
    }

    private boolean handleNotFoundException(NotFoundException ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.debug("handler is : " + handler.getClass().getName());
        try {
            // response.sendError(HttpServletResponse.SC_NOT_FOUND);
            response.setStatus(HttpStatus.NOT_FOUND.code());
            response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            Map<String, String> map = new HashMap<>();
            map.put("error", NotFoundException.class.getName());
            response.getWriter().write(objectMapper.writeValueAsString(map));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
