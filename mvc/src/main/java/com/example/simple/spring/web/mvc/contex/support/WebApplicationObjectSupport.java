package com.example.simple.spring.web.mvc.contex.support;

import com.example.simple.spring.web.mvc.context.ServletContextAware;
import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.util.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.servlet.ServletContext;
import java.io.File;

public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport
    implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    protected boolean isContextRequired() {
        return true;
    }

    @Override
    protected void initApplicationContext(ApplicationContext context) {
        super.initApplicationContext(context);
        if (this.servletContext == null && context instanceof SimpleWebApplicationContext) {
            this.servletContext = ((SimpleWebApplicationContext) context).getServletContext();
            if (this.servletContext != null) {
                initServletContext(this.servletContext);
            }
        }
    }

    protected void initServletContext(ServletContext servletContext) {
    }

    protected final SimpleWebApplicationContext getWebApplicationContext() throws IllegalStateException {
        ApplicationContext ctx = getApplicationContext();
        if (ctx instanceof SimpleWebApplicationContext) {
            return (SimpleWebApplicationContext) getApplicationContext();
        } else if (isContextRequired()) {
            throw new IllegalStateException("WebApplicationObjectSupport instance [" + this +
                "] does not run in a WebApplicationContext but in: " + ctx);
        } else {
            return null;
        }
    }

    protected final ServletContext getServletContext() throws IllegalStateException {
        if (this.servletContext != null) {
            return this.servletContext;
        }
        ServletContext servletContext = getWebApplicationContext().getServletContext();
        if (servletContext == null && isContextRequired()) {
            throw new IllegalStateException("WebApplicationObjectSupport instance [" + this +
                "] does not run within a ServletContext. Make sure the object is fully configured!");
        }
        return servletContext;
    }

    public final void setServletContext(ServletContext servletContext) {
        logger.debug("set servletContext in class : " + getClass().getSimpleName());
        if (servletContext != this.servletContext) {
            this.servletContext = servletContext;
            if (servletContext != null) {
                initServletContext(servletContext);
            }
        }
    }

    protected final File getTempDir() throws IllegalStateException {
        return WebUtils.getTempDir(getServletContext());
    }

}
