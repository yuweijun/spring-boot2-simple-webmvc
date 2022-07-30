package com.example.simple.spring.web.mvc.http.converter;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Type;

public interface GenericHttpMessageConverter<T> extends HttpMessageConverter<T> {

    boolean canRead(Type type, Class<?> contextClass, MediaType mediaType);

    T read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageException;

    boolean canWrite(Type type, Class<?> clazz, MediaType mediaType);

    void write(T t, Type type, MediaType contentType, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageException;

}
