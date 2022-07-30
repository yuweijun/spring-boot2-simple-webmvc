

package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

public class ServletInvocableHandlerMethod extends InvocableHandlerMethod {

    private HttpStatus responseStatus;

    private String responseReason;

    private HandlerMethodReturnValueHandler returnValueHandler;

    public ServletInvocableHandlerMethod(Object handler, Method method, HttpServletRequest request, HttpServletResponse response) {
        super(handler, method, request, response);

        ResponseStatus annotation = getMethodAnnotation(ResponseStatus.class);
        if (annotation != null) {
            this.responseStatus = annotation.value();
            this.responseReason = annotation.reason();
        }
    }

    public void setHandlerMethodReturnValueHandler(HandlerMethodReturnValueHandler returnValueHandler) {
        this.returnValueHandler = returnValueHandler;
    }

    public final void invokeAndHandle(Object... providedArgs) throws Exception {
        Object returnValue = invokeForRequest(request, response, providedArgs);
        setResponseStatus(response);

        if (returnValue == null) {
            return;
        }

        try {
            returnValueHandler.handleReturnValue(returnValue, getReturnType(), request, response);
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