package com.example.simple.spring.v1.web.context;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

public interface SimpleWebApplicationContext extends ApplicationContext {

    String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = SimpleWebApplicationContext.class.getName() + ".ROOT";

    String SCOPE_REQUEST = "request";

    String SCOPE_SESSION = "session";

    String SCOPE_GLOBAL_SESSION = "globalSession";

    String SCOPE_APPLICATION = "application";

    String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

    String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

    String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";

    ServletContext getServletContext();

}
