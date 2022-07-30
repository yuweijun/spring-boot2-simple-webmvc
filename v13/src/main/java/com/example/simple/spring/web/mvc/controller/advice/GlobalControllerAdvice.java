package com.example.simple.spring.web.mvc.controller.advice;

import com.example.simple.spring.web.mvc.bind.annotation.ControllerAdvice;
import com.example.simple.spring.web.mvc.bind.annotation.ExceptionHandler;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import com.example.simple.spring.web.mvc.controller.exception.AuthenticationFailedException;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final Log logger = LogFactory.getLog(getClass());

    @ExceptionHandler(value = {AuthenticationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleAuthenticationFailedException() {
        logger.info("global exception handler for AuthenticationFailedException");
        Map<String, String> map = new HashMap<>();
        map.put("exception", AuthenticationFailedException.class.getName());
        return map;
    }


}
