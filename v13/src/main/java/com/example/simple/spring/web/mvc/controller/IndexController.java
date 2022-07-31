package com.example.simple.spring.web.mvc.controller;


import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping("/index")
    public void index(@ModelAttribute("map") Map<String, Integer> map, String time, HttpServletRequest request) {
        LOGGER.info("@ModelAttribute(map) is {}", map);
        LOGGER.info("index");
        LOGGER.info("time is : {}", time);

        final ModelMap modelMap = ModelAndView.getModel(request);
        modelMap.addAttribute("map", map);
        modelMap.addAttribute("time", time);
        modelMap.addAttribute("hello", "yu");

        ModelAndView.setViewName(request, "index");
    }

    @ModelAttribute
    public Map<String, Integer> method1() {
        LOGGER.info("method1");
        Map<String, Integer> map = new HashMap<>();
        map.put("test", 1);
        return map;
    }

}
