package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class JstlViewController {

    @RequestMapping("/redirectView")
    public void redirectView(HttpServletRequest request, HttpServletResponse response) {
        final ModelAndView modelAndView = ModelAndView.get(request);
        modelAndView.addObject("msg", "World");
        modelAndView.setViewName("redirect:/jstlView");
    }

    @RequestMapping("/jstlView")
    public void jstlView(HttpServletRequest request, HttpServletResponse response) {
        final ModelMap model = ModelAndView.getModel(request);
        model.addAttribute("msg", "World");
        ModelAndView.setViewName(request, "jstl");
    }

}
