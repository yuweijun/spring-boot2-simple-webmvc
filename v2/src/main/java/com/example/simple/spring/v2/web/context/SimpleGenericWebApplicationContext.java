/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.simple.spring.v2.web.context;

import com.example.simple.spring.v2.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class SimpleGenericWebApplicationContext extends GenericApplicationContext
    implements SimpleConfigurableWebApplicationContext, ThemeSource {

    private ServletContext servletContext;

    private ThemeSource themeSource;

    public SimpleGenericWebApplicationContext() {
        super();
    }

    public SimpleGenericWebApplicationContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public SimpleGenericWebApplicationContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    public SimpleGenericWebApplicationContext(DefaultListableBeanFactory beanFactory, ServletContext servletContext) {
        super(beanFactory);
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);

        WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext);
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

    // ---------------------------------------------------------------------
    // Pseudo-implementation of ConfigurableWebApplicationContext
    // ---------------------------------------------------------------------

    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException(
            "GenericWebApplicationContext does not support getServletConfig()");
    }

    public void setServletConfig(ServletConfig servletConfig) {
        // no-op
    }

    public String getNamespace() {
        throw new UnsupportedOperationException(
            "GenericWebApplicationContext does not support getNamespace()");
    }

    public void setNamespace(String namespace) {
        // no-op
    }

    public void setConfigLocation(String configLocation) {
        if (StringUtils.hasText(configLocation)) {
            throw new UnsupportedOperationException(
                "GenericWebApplicationContext does not support setConfigLocation(). " +
                    "Do you still have an 'contextConfigLocations' init-param set?");
        }
    }

    public String[] getConfigLocations() {
        throw new UnsupportedOperationException(
            "GenericWebApplicationContext does not support getConfigLocations()");
    }

    public void setConfigLocations(String[] configLocations) {
        if (!ObjectUtils.isEmpty(configLocations)) {
            throw new UnsupportedOperationException(
                "GenericWebApplicationContext does not support setConfigLocations(). " +
                    "Do you still have an 'contextConfigLocations' init-param set?");
        }
    }

}
