package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
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

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        final ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);
        return readWithMessageConverters(inputMessage, parameter, parameter.getParameterType());
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        logger.debug("returnType is " + returnType);
        return returnType.hasMethodAnnotation(ResponseBody.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("handle return value by HandlerMethodReturnValueHandler : " + getClass().getSimpleName());
        if (returnValue != null) {
            writeWithMessageConverters(returnValue, returnType, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
        }
    }

}