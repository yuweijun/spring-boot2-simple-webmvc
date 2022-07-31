package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractHandlerMethodExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        if (handler == null) {
            return super.shouldApplyTo(request, handler);
        } else if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            handler = handlerMethod.getBean();
            return super.shouldApplyTo(request, handler);
        } else if (hasGlobalExceptionHandlers() && hasHandlerMappings()) {
            return super.shouldApplyTo(request, handler);
        } else {
            return false;
        }
    }

    protected boolean hasGlobalExceptionHandlers() {
        return false;
    }

    @Override
    protected final boolean doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        return doResolveHandlerMethodException(request, response, (HandlerMethod) handler, ex);
    }

    protected abstract boolean doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex);

}
