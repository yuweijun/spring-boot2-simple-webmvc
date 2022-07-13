package com.example.simple.spring.v2.web.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public abstract class AbstractSimpleRefreshableWebApplicationContext extends AbstractRefreshableConfigApplicationContext implements SimpleConfigurableWebApplicationContext {

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    private String namespace;

    private ThemeSource themeSource;

    public AbstractSimpleRefreshableWebApplicationContext() {
        setDisplayName("Root WebApplicationContext");
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null && this.servletContext == null) {
            this.setServletContext(servletConfig.getServletContext());
        }
    }

    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null) {
            setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
        }
    }

    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public String[] getConfigLocations() {
        return super.getConfigLocations();
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    @Override
    protected void onRefresh() {
        this.themeSource = UiApplicationContextUtils.initThemeSource(this);
    }

    @Override
    protected void initPropertySources() {
        super.initPropertySources();
    }

    public Theme getTheme(String themeName) {
        return this.themeSource.getTheme(themeName);
    }

}
