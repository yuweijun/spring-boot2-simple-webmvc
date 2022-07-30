package com.example.simple.spring.web.mvc.http;

import org.springframework.core.NestedRuntimeException;

import java.util.List;

public class HttpMediaTypeNotAcceptableException extends NestedRuntimeException {

    public HttpMediaTypeNotAcceptableException(String message) {
        super(message);
    }

    public HttpMediaTypeNotAcceptableException(List<MediaType> supportedMediaTypes) {
        super("Could not find acceptable representation" + supportedMediaTypes);
    }

}
