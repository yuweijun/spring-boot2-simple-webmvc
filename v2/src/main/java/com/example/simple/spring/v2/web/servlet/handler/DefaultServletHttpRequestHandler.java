/*
 * Copyright 2002-2010 the original author or authors.
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

package com.example.simple.spring.v2.web.servlet.handler;

import org.springframework.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultServletHttpRequestHandler implements HttpRequestHandler {

    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String GAE_DEFAULT_SERVLET_NAME = "_ah_default";

    private static final String RESIN_DEFAULT_SERVLET_NAME = "resin-file";

    private static final String WEBLOGIC_DEFAULT_SERVLET_NAME = "FileServlet";

    private static final String WEBSPHERE_DEFAULT_SERVLET_NAME = "SimpleFileServlet";

    private String defaultServletName;

    private ServletContext servletContext;

    public void setDefaultServletName(String defaultServletName) {
        this.defaultServletName = defaultServletName;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        if (!StringUtils.hasText(this.defaultServletName)) {
            if (this.servletContext.getNamedDispatcher(COMMON_DEFAULT_SERVLET_NAME) != null) {
                this.defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
            } else if (this.servletContext.getNamedDispatcher(GAE_DEFAULT_SERVLET_NAME) != null) {
                this.defaultServletName = GAE_DEFAULT_SERVLET_NAME;
            } else if (this.servletContext.getNamedDispatcher(RESIN_DEFAULT_SERVLET_NAME) != null) {
                this.defaultServletName = RESIN_DEFAULT_SERVLET_NAME;
            } else if (this.servletContext.getNamedDispatcher(WEBLOGIC_DEFAULT_SERVLET_NAME) != null) {
                this.defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
            } else if (this.servletContext.getNamedDispatcher(WEBSPHERE_DEFAULT_SERVLET_NAME) != null) {
                this.defaultServletName = WEBSPHERE_DEFAULT_SERVLET_NAME;
            } else {
                throw new IllegalStateException("Unable to locate the default servlet for serving static content. " +
                    "Please set the 'defaultServletName' property explicitly.");
            }
        }
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        RequestDispatcher rd = this.servletContext.getNamedDispatcher(this.defaultServletName);
        if (rd == null) {
            throw new IllegalStateException("A RequestDispatcher could not be located for the default servlet '" +
                this.defaultServletName + "'");
        }
        rd.forward(request, response);
    }

}
