package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.RequestBody;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpRequest;
import com.example.simple.spring.web.mvc.http.server.ServletServerHttpResponse;
import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {

    public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    public boolean supportsParameter(MethodParameter parameter) {
        logger.debug("parameter is " + parameter);
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        logger.debug("returnType is " + returnType);
        return returnType.hasMethodAnnotation(ResponseBody.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("handle return value by " + getClass().getSimpleName());
        if (returnValue != null) {
            writeWithMessageConverters(returnValue, returnType, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
        }
    }

}