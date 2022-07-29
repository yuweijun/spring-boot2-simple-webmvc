package com.example.simple.spring.web.mvc.servlet.view;

public interface ViewResolver {

    View resolveViewName(String viewName) throws Exception;

}
