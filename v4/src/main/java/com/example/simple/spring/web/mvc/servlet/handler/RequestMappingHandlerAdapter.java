package com.example.simple.spring.web.mvc.servlet.handler;

import com.example.simple.spring.web.mvc.method.HandlerMethod;
import com.example.simple.spring.web.mvc.servlet.HandlerAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestMappingHandlerAdapter implements HandlerAdapter, BeanFactoryAware {

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private ConfigurableBeanFactory beanFactory;

    public RequestMappingHandlerAdapter() {
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public final boolean supports(Object handler) {
        return HandlerMethod.class.isInstance(handler) && supportsInternal((HandlerMethod) handler);
    }

    @Override
    public final void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        handleInternal(request, response, (HandlerMethod) handler);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return supportsMethodParameters(handlerMethod.getMethodParameters()) &&
            supportsReturnType(handlerMethod.getReturnType());
    }

    private boolean supportsMethodParameters(MethodParameter[] methodParameters) {
        return true;
    }

    private boolean supportsReturnType(MethodParameter methodReturnType) {
        return Void.TYPE.equals(methodReturnType.getParameterType());
    }

    protected final void handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        invokeHandlerMethod(request, response, handlerMethod);
    }

    private void invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response,
        HandlerMethod handlerMethod) throws Exception {
        // TODO
    }

}
