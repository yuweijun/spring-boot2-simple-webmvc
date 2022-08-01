package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.servlet.error.DefaultErrorAttributes;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

public class HandlerExceptionResolverComposite implements HandlerExceptionResolver, Ordered {

    private List<HandlerExceptionResolver> resolvers;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<HandlerExceptionResolver> getExceptionResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

    public void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        this.resolvers = exceptionResolvers;
    }

    public boolean resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DefaultErrorAttributes.storeErrorInRequest(request, ex);
        if (resolvers != null) {
            for (HandlerExceptionResolver handlerExceptionResolver : resolvers) {
                final boolean resolved = handlerExceptionResolver.resolveException(request, response, handler, ex);
                if (resolved) {
                    return true;
                }
            }
        }
        return false;
    }

}
