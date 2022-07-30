package com.example.spring.boot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("session_attr")
public class AttributesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @ModelAttribute("todos")
    public List<String> todos(HttpServletRequest request, Model model) {
        LOGGER.info("add request attribute for @RequestAttribute");
        request.setAttribute("request_attr", "request_attribute_value");

        if (model.containsAttribute("session_attr")) {
            LOGGER.info("session attribute exists in model : {}", model.getAttribute("session_attr"));
        } else {
            model.addAttribute("session_attr", "add_session_value_in_@ModelAttribute_method");
        }

        if (model.containsAttribute("flash_attr")) {
            LOGGER.info("flash attribute exists in model : {}", model.getAttribute("flash_attr"));
        } else {
            model.addAttribute("flash_attr", "add_flash_value_in_@ModelAttribute_method");
        }

        final List<String> list = new ArrayList<>();
        list.add("todo1");
        return list;
    }

    @GetMapping("/show")
    @ResponseBody
    public List<String> show(Model model, HttpSession httpSession, SessionStatus sessionStatus,
        @RequestAttribute("request_attr") String requestAttr,
        @ModelAttribute("session_attr") String sessionAttr,
        @ModelAttribute("flash_attr") String flashAttr,
        @ModelAttribute("todos") List<String> todos) {

        LOGGER.info("get request attribute in show : {}", requestAttr);
        LOGGER.info("get session key which will not clear after sessionStatus.setComplete() : {}", httpSession.getAttribute("session_key"));
        LOGGER.info("get session attribute which will clear after sessionStatus.setComplete() : {}", sessionAttr);
        LOGGER.info("get flash attribute which will clear after sessionStatus.setComplete() : {}", flashAttr);

        sessionStatus.setComplete();

        todos.add("todo2");
        return todos;
    }

    @GetMapping("/create")
    public String create(HttpServletRequest request, HttpSession httpSession, Model model, RedirectAttributes attributes,
        @RequestAttribute("request_attr") String requestAttr,
        @ModelAttribute("todos") List<String> todos) {
        
        LOGGER.info("get request attribute in create : {}", requestAttr);
        LOGGER.info("get todos from @ModelAttribute : {}", todos);

        httpSession.setAttribute("session_key", "session_value");

        attributes.addFlashAttribute("flash_attr", "flash_attribute_value");
        model.addAttribute("session_attr", "session_attribute_value");
        return "redirect:/show";
    }

}
