package com.example.simple.spring.web.mvc.http.converter;

import org.springframework.core.NestedRuntimeException;

public class HttpMessageException extends NestedRuntimeException {

    public HttpMessageException(String msg) {
        super(msg);
    }

    public HttpMessageException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
