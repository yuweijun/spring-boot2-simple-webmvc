package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import com.example.simple.spring.web.mvc.http.HttpEntity;
import com.example.simple.spring.web.mvc.http.HttpHeaders;
import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.ResponseEntity;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpRequest;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpResponse;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class HttpEntityMethodProcessor extends AbstractMessageConverterMethodProcessor {

    public HttpEntityMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return HttpEntity.class.equals(parameterType);
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> parameterType = returnType.getParameterType();
        return HttpEntity.class.equals(parameterType) || ResponseEntity.class.equals(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        HttpInputMessage inputMessage = createInputMessage(request);
        Class<?> paramType = getHttpEntityType(parameter);

        Object body = readWithMessageConverters(createInputMessage(request), parameter, paramType);
        return new HttpEntity<Object>(body, inputMessage.getHeaders());
    }

    private Class<?> getHttpEntityType(MethodParameter parameter) {
        Assert.isAssignable(HttpEntity.class, parameter.getParameterType());
        ParameterizedType type = (ParameterizedType) parameter.getGenericParameterType();
        if (type.getActualTypeArguments().length == 1) {
            Type typeArgument = type.getActualTypeArguments()[0];
            if (typeArgument instanceof Class) {
                return (Class<?>) typeArgument;
            } else if (typeArgument instanceof GenericArrayType) {
                Type componentType = ((GenericArrayType) typeArgument).getGenericComponentType();
                if (componentType instanceof Class) {
                    // Surely, there should be a nicer way to determine the array type
                    Object array = Array.newInstance((Class<?>) componentType, 0);
                    return array.getClass();
                }
            }
        }
        throw new IllegalArgumentException("HttpEntity parameter (" + parameter.getParameterName() + ") " + "in method " + parameter.getMethod() + "is not parameterized");
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (returnValue == null) {
            return;
        }

        ServletServerHttpRequest inputMessage = createInputMessage(request);
        ServletServerHttpResponse outputMessage = createOutputMessage(response);

        Assert.isInstanceOf(HttpEntity.class, returnValue);
        HttpEntity<?> responseEntity = (HttpEntity<?>) returnValue;
        if (responseEntity instanceof ResponseEntity) {
            outputMessage.setStatusCode(((ResponseEntity<?>) responseEntity).getStatusCode());
        }

        HttpHeaders entityHeaders = responseEntity.getHeaders();
        if (!entityHeaders.isEmpty()) {
            outputMessage.getHeaders().putAll(entityHeaders);
        }

        Object body = responseEntity.getBody();
        if (body != null) {
            writeWithMessageConverters(body, returnType, inputMessage, outputMessage);
        } else {
            // flush headers to the HttpServletResponse
            outputMessage.getBody();
        }
    }

}