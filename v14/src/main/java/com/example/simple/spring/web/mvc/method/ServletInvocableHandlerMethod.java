

package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.servlet.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

public class ServletInvocableHandlerMethod extends InvocableHandlerMethod {

    private HttpStatus responseStatus;

    private String responseReason;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public ServletInvocableHandlerMethod(Object handler, Method method, HttpServletRequest request, HttpServletResponse response) {
        super(handler, method, request, response);
        getResponseStatus();
    }

    public ServletInvocableHandlerMethod(String beanName, BeanFactory beanFactory, Method method, HttpServletRequest request, HttpServletResponse response) {
        super(beanName, beanFactory, method, request, response);
        getResponseStatus();
    }

    @Override
    protected HttpStatus getResponseStatus() {
        ResponseStatus annotation = getMethodAnnotation(ResponseStatus.class);
        if (annotation != null) {
            this.responseStatus = annotation.value();
            this.responseReason = annotation.reason();
        }
        return this.responseStatus;
    }

    public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public final void invokeAndHandle(Object... providedArgs) throws Exception {
        Object returnValue = invokeForRequest(request, response, providedArgs);
        setResponseStatus(response);

        if (returnValue == null) {
            return;
        }

        try {
            returnValueHandlers.handleReturnValue(returnValue, getReturnType(), request, response);
        } catch (Exception e) {
            logger.debug(getReturnValueHandlingErrorMessage("Error handling return value", returnValue), e);
            throw e;
        }
    }

    private String getReturnValueHandlingErrorMessage(String message, Object returnValue) {
        StringBuilder sb = new StringBuilder(message);
        if (returnValue != null) {
            sb.append(" [type=" + returnValue.getClass().getName() + "] ");
        }
        sb.append("[value=" + returnValue + "]");
        return getDetailedErrorMessage(sb.toString());
    }

    private void setResponseStatus(HttpServletResponse response) throws IOException {
        if (this.responseStatus != null) {
            if (StringUtils.hasText(this.responseReason)) {
                response.sendError(this.responseStatus.code(), this.responseReason);
            } else {
                response.setStatus(this.responseStatus.code());
            }
        }
    }

}