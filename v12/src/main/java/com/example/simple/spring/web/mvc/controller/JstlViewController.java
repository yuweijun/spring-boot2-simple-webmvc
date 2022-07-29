package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class JstlViewController {

    @RequestMapping("/redirectView")
    public void redirectView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/jstlView");
        modelAndView.addObject("msg", "World");

        modelAndView.put(request);
    }

    @RequestMapping("/jstlView")
    public void jstlView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jstl");
        modelAndView.addObject("msg", "World");

        modelAndView.put(request);
    }

}
