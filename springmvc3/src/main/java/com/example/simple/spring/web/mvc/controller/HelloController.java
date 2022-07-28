package com.example.simple.spring.web.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HelloController {

    @RequestMapping("/hello")
    public String hello(Map<String, String> model) {
        model.put("msg", "using RequestMappingHandlerMapping");

        return "hello";
    }

    @RequestMapping("/index")
    public String index(@ModelAttribute("map") Map<String, Integer> map, String time, Model model) {
        System.out.println(map);
        System.out.println("index");
        System.out.println("time is " + time);
        model.addAttribute("hello", "yu");
        return "index";
    }

    @ModelAttribute
    public Map<String, Integer> method1() {
        System.out.println("method1");
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("test", 1);
        return map;
    }
}
