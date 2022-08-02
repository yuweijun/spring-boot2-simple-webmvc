package com.example.simple.spring.web.mvc.bind;

import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.springframework.beans.MutablePropertyValues;

import javax.servlet.ServletRequest;
import java.util.Map;

public class ExtendedServletRequestDataBinder extends ServletRequestDataBinder {

    public ExtendedServletRequestDataBinder() {
        this(null);
    }

    public ExtendedServletRequestDataBinder(Object target) {
        super(target);
    }

    public ExtendedServletRequestDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    @Override
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        String attr = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
        mpvs.addPropertyValues((Map<String, String>) request.getAttribute(attr));
    }

}
