package com.example.simple.spring.web.mvc.http.server;

import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.HttpStatus;

public interface ServerHttpResponse extends HttpOutputMessage {

    void setStatusCode(HttpStatus status);

    void close();

}
