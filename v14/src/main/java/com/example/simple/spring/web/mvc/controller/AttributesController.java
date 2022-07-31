package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.GetMapping;
import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.PostMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RequestAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.RequestBody;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.bind.annotation.SessionAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.SessionAttributes;
import com.example.simple.spring.web.mvc.bind.support.SessionStatus;
import com.example.simple.spring.web.mvc.controller.dto.UserDTO;
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
@SessionAttributes("session_attributes_1")
public class AttributesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesController.class);

    @ModelAttribute("listReturnedFromModelAttributeMethod")
    public List<String> modelAttributeMethod(HttpServletRequest request) {
        LOGGER.info("add request attribute for @RequestAttribute");
        final List<String> list = new ArrayList<>();
        list.add("time1_" + System.currentTimeMillis());

        request.setAttribute("request_attribute_1", "request_attribute_1_value_in_@ModelAttribute method at" + new Date());

        ModelMap model = ModelAndView.getModel(request);
        if (model.containsAttribute("session_attributes_1")) {
            LOGGER.info("session attribute exists in model : {}", model.getAttribute("session_attributes_1"));
        } else {
            model.addAttribute("session_attributes_1", "session_attributes_1_value_in_@ModelAttribute method at " + new Date());
        }

        return list;
    }

    @GetMapping("/show")
    @ResponseBody
    public ModelMap show(HttpServletRequest request, HttpSession httpSession,
        @RequestAttribute("request_attribute_1") String requestAttr,
        @SessionAttribute("httpsession_key") String sessionValue,
        @ModelAttribute("session_attributes_1") String sessionAttr,
        @ModelAttribute("listReturnedFromModelAttributeMethod") List<String> list) {

        list.add("time2_" + System.currentTimeMillis());
        final Object getAttributeFromHttpSession = httpSession.getAttribute("httpsession_key");

        LOGGER.info("get request attribute in show : {}", requestAttr);
        LOGGER.info("get session value which inject using @SessionAttribute : {}", sessionValue);
        LOGGER.info("get session key which will not clear after sessionStatus.setComplete() : {}", getAttributeFromHttpSession);
        LOGGER.info("get session attribute which will clear after sessionStatus.setComplete() : {}", sessionAttr);

        final ModelMap model = ModelAndView.getModel(request);
        model.addAttribute("httpsession_key", getAttributeFromHttpSession);
        model.addAttribute("request_attribute_1", requestAttr);

        SessionStatus sessionStatus = ModelAndView.getSessionStatus(request);
        sessionStatus.setComplete();
        return model;
    }

    @PostMapping("/create")
    public void create(HttpServletRequest request, HttpSession httpSession,
        @RequestAttribute("request_attribute_1") String requestAttr,
        @ModelAttribute("listReturnedFromModelAttributeMethod") List<String> list,
        @RequestBody UserDTO userDTO) {

        LOGGER.info("get request attribute in create : {}", requestAttr);
        LOGGER.info("get modelAttributeMethod from @ModelAttribute : {}", list);

        httpSession.setAttribute("httpsession_key", "httpsession_value");

        ModelMap model = ModelAndView.getModel(request);
        model.addAttribute("session_attributes_1", "session_attributes_1_value");
        model.addAttribute("userDTO@RequestBody", userDTO);

        ModelAndView.setViewName(request, "redirect:/show");
    }

}
