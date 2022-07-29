package com.example.simple.spring.web.mvc.servlet.view;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class JstlView extends InternalResourceView {

    public JstlView() {
    }

    public JstlView(String url) {
        super(url);
    }

    @Override
    protected void initServletContext(ServletContext servletContext) {
        super.initServletContext(servletContext);
    }

    @Override
    protected void exposeHelpers(HttpServletRequest request) throws Exception {
    }

}
