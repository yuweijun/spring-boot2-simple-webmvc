package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpMediaTypeNotSupportedException;
import com.example.simple.spring.web.mvc.http.MediaType;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractMessageConverterMethodArgumentResolver implements HandlerMethodArgumentResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    protected final List<HttpMessageConverter<?>> messageConverters;

    protected final List<MediaType> allSupportedMediaTypes;

    public AbstractMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters) {
        Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
        this.messageConverters = messageConverters;
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(messageConverters);
    }

    private static List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<>(allSupportedMediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }

    protected <T> Object readWithMessageConverters(MethodParameter methodParam, Class<T> paramType, HttpServletRequest request) throws IOException {
        HttpInputMessage inputMessage = createInputMessage(request);
        return readWithMessageConverters(inputMessage, methodParam, paramType);
    }

    protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter methodParam, Class<T> paramType)
        throws IOException, HttpMediaTypeNotSupportedException {

        MediaType contentType = inputMessage.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
            if (messageConverter.canRead(paramType, contentType)) {
                logger.debug("Reading [" + paramType.getName() + "] as \"" + contentType + "\" using [" + messageConverter + "]");
                return ((HttpMessageConverter<T>) messageConverter).read(paramType, inputMessage);
            }
        }

        throw new HttpMediaTypeNotSupportedException(contentType);
    }

    protected ServletServerHttpRequest createInputMessage(HttpServletRequest request) {
        return new ServletServerHttpRequest(request);
    }

}