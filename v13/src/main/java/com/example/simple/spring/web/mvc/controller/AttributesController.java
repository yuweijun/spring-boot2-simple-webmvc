package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.GetMapping;
import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.RequestAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.bind.annotation.SessionAttributes;
import com.example.simple.spring.web.mvc.bind.support.SessionStatus;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@SessionAttributes("session_attr")
public class AttributesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesController.class);

    @ModelAttribute("todos")
    public List<String> todos(HttpServletRequest request) {
        LOGGER.info("add request attribute for @RequestAttribute");
        request.setAttribute("request_attr", "request_attribute_value_" + new Date());

        ModelMap model = ModelAndView.getModel(request);
        if (model.containsAttribute("session_attr")) {
            LOGGER.info("session attribute exists in model : {}", model.getAttribute("session_attr"));
        } else {
            model.addAttribute("session_attr", "add_session_value_in_@ModelAttribute_method_" + new Date());
        }

        final List<String> list = new ArrayList<>();
        list.add("todo1_" + model.getAttribute("session_attr"));
        return list;
    }

    @GetMapping("/show")
    @ResponseBody
    public List<String> show(HttpServletRequest request, HttpSession httpSession,
        @RequestAttribute("request_attr") String requestAttr,
        @ModelAttribute("session_attr") String sessionAttr,
        @ModelAttribute("todos") List<String> todos) {

        LOGGER.info("get request attribute in show : {}", requestAttr);
        LOGGER.info("get session key which will not clear after sessionStatus.setComplete() : {}", httpSession.getAttribute("session_key"));
        LOGGER.info("get session attribute which will clear after sessionStatus.setComplete() : {}", sessionAttr);
        SessionStatus sessionStatus = ModelAndView.getSessionStatus(request);
        sessionStatus.setComplete();

        todos.add("todo2_" + System.currentTimeMillis());
        return todos;
    }

    @GetMapping("/create")
    public void create(HttpServletRequest request, HttpSession httpSession,
        @RequestAttribute("request_attr") String requestAttr,
        @ModelAttribute("todos") List<String> todos) {

        LOGGER.info("get request attribute in create : {}", requestAttr);
        LOGGER.info("get todos from @ModelAttribute : {}", todos);

        httpSession.setAttribute("session_key", "session_value");

        ModelMap model = ModelAndView.getModel(request);
        model.addAttribute("session_attr", "session_attribute_value");

        ModelAndView.setViewName(request, "redirect:/show");
    }

}
