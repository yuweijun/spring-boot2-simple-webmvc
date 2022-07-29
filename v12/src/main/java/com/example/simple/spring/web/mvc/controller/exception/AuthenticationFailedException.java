package com.example.simple.spring.web.mvc.controller.exception;

import org.springframework.core.NestedRuntimeException;

public class AuthenticationFailedException extends NestedRuntimeException {

    public AuthenticationFailedException(String msg) {
        super(msg);
    }

    public AuthenticationFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
