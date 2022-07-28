package com.example.simple.spring.web.mvc.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

import java.util.Map;

@Controller
public class RequestParamController {

    private final Log logger = LogFactory.getLog(RequestParamController.class);

    @RequestMapping("/requestForwardToHello")
    @ResponseBody
    public ModelAndView requestForwardToHello(Map<String, String> map) {
        logger.info("forward to /hello and response render : Hello using RequestMappingHandlerMapping");
        map.put("msg", "World");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().putAll(map);

        // rd.forward(requestToExpose, response);
        View hello = new InternalResourceView("hello");
        modelAndView.setView(hello);

        return modelAndView;
    }

    @RequestMapping("/requestParam")
    @ResponseBody
    public ModelAndView requestParam(Map<String, String> map) {
        logger.info("response render : Hello World");
        map.put("msg", "World");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModelMap().putAll(map);

        if (!modelAndView.wasCleared()) {
            logger.debug("org.springframework.web.servlet.DispatcherServlet: if (mv != null && !mv.wasCleared()){render(mv, processedRequest, response);");
        }

        modelAndView.setViewName("hello");

        return modelAndView;
    }

    @RequestMapping("/requestIntegerParam")
    @ResponseBody
    public String requestIntegerParam(@RequestParam int id) {
        logger.info("arg = binder.convertIfNecessary(arg, paramType, parameter) will convert String id which get from request parameters and convert to int value");
        return "id = " + id;
    }

}
