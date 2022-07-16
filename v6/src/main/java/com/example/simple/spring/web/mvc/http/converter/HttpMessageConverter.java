package com.example.simple.spring.web.mvc.http.converter;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.MediaType;

import java.io.IOException;
import java.util.List;

public interface HttpMessageConverter<T> {

    boolean canRead(Class<?> clazz, MediaType mediaType);

    boolean canWrite(Class<?> clazz, MediaType mediaType);

    List<MediaType> getSupportedMediaTypes();

    T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageException;

    void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageException;

}

