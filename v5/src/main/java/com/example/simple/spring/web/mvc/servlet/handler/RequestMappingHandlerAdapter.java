package com.example.simple.spring.web.mvc.servlet.handler;

import com.example.simple.spring.web.mvc.method.HandlerMethod;
import com.example.simple.spring.web.mvc.servlet.HandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class RequestMappingHandlerAdapter implements HandlerAdapter, BeanFactoryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerAdapter.class);

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private ConfigurableBeanFactory beanFactory;

    public RequestMappingHandlerAdapter() {
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public final boolean supports(Object handler) {
        return HandlerMethod.class.isInstance(handler);
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

    protected final void handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        invokeHandlerMethod(request, response, handlerMethod);
    }

    private void invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        final Method method = handlerMethod.getMethod();
        final Object bean = handlerMethod.getBean();
        LOGGER.info("method [{}] invoke in bean [{}]", method.getName(), bean.getClass().getSimpleName());

        final Object ret = method.invoke(bean);
        if (ret != null) {
            LOGGER.debug("invoke return value : {}", ret.getClass().getSimpleName());
            response.getWriter().write(ret.toString());
        }
    }

}
