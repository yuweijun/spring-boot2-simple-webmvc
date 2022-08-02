package com.example.simple.spring.web.mvc.bind;

import com.example.simple.spring.web.util.WebUtils;
import org.springframework.beans.MutablePropertyValues;

import javax.servlet.ServletRequest;

public class ServletRequestParameterPropertyValues extends MutablePropertyValues {

    public static final String DEFAULT_PREFIX_SEPARATOR = "_";

    public ServletRequestParameterPropertyValues(ServletRequest request) {
        this(request, null, null);
    }

    public ServletRequestParameterPropertyValues(ServletRequest request, String prefix) {
        this(request, prefix, DEFAULT_PREFIX_SEPARATOR);
    }

    public ServletRequestParameterPropertyValues(ServletRequest request, String prefix, String prefixSeparator) {
        super(WebUtils.getParametersStartingWith(
            request, (prefix != null ? prefix + prefixSeparator : null)));
    }

}
