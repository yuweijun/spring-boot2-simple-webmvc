package com.example.simple.spring.web.mvc.servlet.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface View {

    String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

    String PATH_VARIABLES = View.class.getName() + ".pathVariables";

    String getContentType();

    void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
