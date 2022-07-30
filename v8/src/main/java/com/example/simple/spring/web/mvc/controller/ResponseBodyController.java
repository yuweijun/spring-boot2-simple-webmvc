package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResponseBodyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBodyController.class);

    @Autowired
    private List<HandlerInterceptor> interceptors;

    @RequestMapping("/responseBody")
    @ResponseBody
    public Map<String, String> responseBody() {
        LOGGER.info("responseBody method invoked");
        Map<String, String> map = new HashMap<>();
        for (HandlerInterceptor interceptor : interceptors) {
            map.put(interceptor.getClass().getSimpleName(), interceptor.getClass().getCanonicalName());
        }
        return map;
    }

}
