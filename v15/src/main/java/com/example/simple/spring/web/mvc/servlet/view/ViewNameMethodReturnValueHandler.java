package com.example.simple.spring.web.mvc.servlet.view;

import com.example.simple.spring.web.mvc.method.HandlerMethodReturnValueHandler;
import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewNameMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private String[] redirectPatterns;

    public void setRedirectPatterns(String... redirectPatterns) {
        this.redirectPatterns = redirectPatterns;
    }

    public String[] getRedirectPatterns() {
        return this.redirectPatterns;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return (void.class == paramType || CharSequence.class.isAssignableFrom(paramType));
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (returnValue instanceof CharSequence) {
            String viewName = returnValue.toString();
            ModelAndView.setViewName(request, viewName);
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " + returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

}
