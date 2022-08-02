package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.GetMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RestController;
import com.example.simple.spring.web.mvc.controller.dto.RequestScopeBean;
import com.example.simple.spring.web.mvc.controller.dto.SessionScopeBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

@RestController
public class ScopedBeanController {

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private RequestScopeBean requestScopeBean;

    @Autowired
    private SessionScopeBean sessionScopeBean;

    @GetMapping("/requestScope")
    public String requestScope() {
        return requestScopeBean.getIndexDescription();
    }

    @GetMapping("/sessionScope")
    public String sessionScope() {
        return sessionScopeBean.getIndexDescription();
    }

    @GetMapping("/sessionInvalidate")
    public String sessionInvalidate() {
        httpSession.invalidate();
        return sessionScopeBean.getIndexDescription();
    }

}
