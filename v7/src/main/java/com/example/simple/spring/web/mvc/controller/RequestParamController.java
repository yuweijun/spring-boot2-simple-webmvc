package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class RequestParamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParamController.class);

    @Autowired
    private List<HandlerInterceptor> interceptors;

    @RequestMapping("/requestParam")
    @ResponseBody
    public Map<String, String> requestParam(Map<String, String> map) {
        LOGGER.info("requestParam method invoked");
        for (HandlerInterceptor interceptor : interceptors) {
            map.put(interceptor.getClass().getSimpleName(), interceptor.getClass().getCanonicalName());
        }
        return map;
    }

}
