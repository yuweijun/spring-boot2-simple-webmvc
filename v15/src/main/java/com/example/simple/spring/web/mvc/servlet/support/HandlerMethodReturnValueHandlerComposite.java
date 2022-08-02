package com.example.simple.spring.web.mvc.servlet.support;

import com.example.simple.spring.web.mvc.method.HandlerMethodReturnValueHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {

    protected final Log logger = LogFactory.getLog(getClass());

    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    private final Map<MethodParameter, HandlerMethodReturnValueHandler> returnValueHandlerCache = new ConcurrentHashMap<>();

    public List<HandlerMethodReturnValueHandler> getHandlers() {
        return Collections.unmodifiableList(this.returnValueHandlers);
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);
        Assert.notNull(handler, "Unknown return value type [" + returnType.getParameterType().getName() + "]");
        handler.handleReturnValue(returnValue, returnType, request, response);
    }

    private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        HandlerMethodReturnValueHandler result = this.returnValueHandlerCache.get(returnType);
        if (result == null) {
            for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
                logger.debug("Testing if return value handler [" + returnValueHandler + "] supports [" + returnType.getGenericParameterType() + "]");
                if (returnValueHandler.supportsReturnType(returnType)) {
                    result = returnValueHandler;
                    this.returnValueHandlerCache.put(returnType, returnValueHandler);
                    break;
                }
            }
        }
        return result;
    }

    public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler returnValuehandler) {
        returnValueHandlers.add(returnValuehandler);
        return this;
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(List<? extends HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers != null) {
            for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
                this.returnValueHandlers.add(handler);
            }
        }
        return this;
    }

}