package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.servlet.SimpleController;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("/beanName")
public class BeanNameController implements SimpleController {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("bean name controller");
    }

}
