package com.example.simple.spring.web.mvc.http.converter.json;

import com.example.simple.spring.web.mvc.http.HttpHeaders;
import com.example.simple.spring.web.mvc.http.HttpInputMessage;

import java.io.IOException;
import java.io.InputStream;

public class MappingJacksonInputMessage implements HttpInputMessage {

    private final InputStream body;

    private final HttpHeaders headers;

    private Class<?> deserializationView;

    public MappingJacksonInputMessage(InputStream body, HttpHeaders headers) {
        this.body = body;
        this.headers = headers;
    }

    public MappingJacksonInputMessage(InputStream body, HttpHeaders headers, Class<?> deserializationView) {
        this(body, headers);
        this.deserializationView = deserializationView;
    }

    @Override
    public InputStream getBody() throws IOException {
        return this.body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public Class<?> getDeserializationView() {
        return this.deserializationView;
    }

    public void setDeserializationView(Class<?> deserializationView) {
        this.deserializationView = deserializationView;
    }

}
