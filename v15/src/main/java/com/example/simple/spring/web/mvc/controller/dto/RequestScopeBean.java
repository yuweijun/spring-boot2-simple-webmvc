package com.example.simple.spring.web.mvc.controller.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestScopeBean {

    private final Log logger = LogFactory.getLog(getClass());

    private static AtomicInteger ai = new AtomicInteger();

    private final int index;

    public RequestScopeBean() {
        this.index = ai.getAndIncrement();
    }

    public int getIndex() {
        return index;
    }

    public String getIndexDescription() {
        return "bean index value : " + index + ", [" + this + "]";
    }

}
