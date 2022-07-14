package com.example.simple.spring.web.mvc.util;

import org.springframework.core.NestedExceptionUtils;

import javax.servlet.ServletException;

public class NestedServletException extends ServletException {

    private static final long serialVersionUID = -5292377985529381145L;

    static {
        // Eagerly load the NestedExceptionUtils class to avoid classloader deadlock
        // issues on OSGi when calling getMessage(). Reported by Don Brown; SPR-5607.
        NestedExceptionUtils.class.getName();
    }

    public NestedServletException(String msg) {
        super(msg);
    }

    public NestedServletException(String msg, Throwable cause) {
        super(msg, cause);
        // Set JDK 1.4 exception chain cause if not done by ServletException class already
        // (this differs between Servlet API versions).
        if (getCause() == null && cause != null) {
            initCause(cause);
        }
    }

    @Override
    public String getMessage() {
        return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
    }

}
