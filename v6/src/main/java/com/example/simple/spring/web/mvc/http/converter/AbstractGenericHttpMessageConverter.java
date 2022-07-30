package com.example.simple.spring.web.mvc.http.converter;

import com.example.simple.spring.web.mvc.http.HttpHeaders;
import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class AbstractGenericHttpMessageConverter<T> extends AbstractHttpMessageConverter<T>
    implements GenericHttpMessageConverter<T> {

    protected AbstractGenericHttpMessageConverter() {
    }

    protected AbstractGenericHttpMessageConverter(MediaType supportedMediaType) {
        super(supportedMediaType);
    }

    protected AbstractGenericHttpMessageConverter(MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType));
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return canWrite(clazz, mediaType);
    }

    @Override
    public final void write(final T t, final Type type, MediaType contentType,
        HttpOutputMessage outputMessage) throws IOException, HttpMessageException {

        final HttpHeaders headers = outputMessage.getHeaders();
        // addDefaultHeaders(headers, t, contentType);

        writeInternal(t, type, outputMessage);
        outputMessage.getBody().flush();
    }

    @Override
    protected void writeInternal(T t, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageException {

        writeInternal(t, null, outputMessage);
    }

    protected abstract void writeInternal(T t, Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageException;

}
