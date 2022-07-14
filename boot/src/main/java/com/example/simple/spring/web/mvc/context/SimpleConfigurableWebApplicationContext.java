package com.example.simple.spring.web.mvc.context;

import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public interface SimpleConfigurableWebApplicationContext extends SimpleWebApplicationContext, ConfigurableApplicationContext {

    String APPLICATION_CONTEXT_ID_PREFIX = SimpleWebApplicationContext.class.getName() + ":";

    String SERVLET_CONFIG_BEAN_NAME = "servletConfig";

    void setServletContext(ServletContext servletContext);

    ServletConfig getServletConfig();

    void setServletConfig(ServletConfig servletConfig);

    String getNamespace();

    void setNamespace(String namespace);

    void setConfigLocation(String configLocation);

    String[] getConfigLocations();

    void setConfigLocations(String[] configLocations);

}
