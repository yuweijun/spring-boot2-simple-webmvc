package com.example.simple.spring.web.mvc.method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class HandlerMethod {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Object bean;

    private final Method method;

    private final BeanFactory beanFactory;

    private MethodParameter[] parameters;

    private final Method bridgedMethod;

    public HandlerMethod(Object bean, Method method) {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(method, "method must not be null");
        this.bean = bean;
        this.beanFactory = null;
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
    }

    public HandlerMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "bean must not be null");
        Assert.notNull(methodName, "method must not be null");
        this.bean = bean;
        this.beanFactory = null;
        this.method = bean.getClass().getMethod(methodName, parameterTypes);
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
    }

    public HandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        Assert.hasText(beanName, "'beanName' must not be null");
        Assert.notNull(beanFactory, "'beanFactory' must not be null");
        Assert.notNull(method, "'method' must not be null");
        Assert.isTrue(beanFactory.containsBean(beanName),
            "Bean factory [" + beanFactory + "] does not contain bean " + "with name [" + beanName + "]");
        this.bean = beanName;
        this.beanFactory = beanFactory;
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
    }

    public Object getBean() {
        return this.bean;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getBeanType() {
        if (bean instanceof String) {
            String beanName = (String) bean;
            return beanFactory.getType(beanName);
        } else {
            return ClassUtils.getUserClass(bean.getClass());
        }
    }

    protected Method getBridgedMethod() {
        return bridgedMethod;
    }

    public MethodParameter[] getMethodParameters() {
        if (this.parameters == null) {
            int parameterCount = this.bridgedMethod.getParameterTypes().length;
            MethodParameter[] p = new MethodParameter[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                p[i] = new HandlerMethodParameter(this.bridgedMethod, i);
            }
            this.parameters = p;
        }
        return parameters;
    }

    public MethodParameter getReturnType() {
        return new HandlerMethodParameter(this.bridgedMethod, -1);
    }

    public boolean isVoid() {
        return Void.TYPE.equals(getReturnType().getParameterType());
    }

    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(this.method, annotationType);
    }

    public HandlerMethod createWithResolvedBean() {
        Object handler = this.bean;
        if (this.bean instanceof String) {
            String beanName = (String) this.bean;
            handler = this.beanFactory.getBean(beanName);
        }
        return new HandlerMethod(handler, method);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof HandlerMethod) {
            HandlerMethod other = (HandlerMethod) o;
            return this.bean.equals(other.bean) && this.method.equals(other.method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * this.bean.hashCode() + this.method.hashCode();
    }

    @Override
    public String toString() {
        return method.toGenericString();
    }

    private class HandlerMethodParameter extends MethodParameter {

        public HandlerMethodParameter(Method method, int parameterIndex) {
            super(method, parameterIndex);
        }

        @Override
        public Class<?> getDeclaringClass() {
            return HandlerMethod.this.getBeanType();
        }

        @Override
        public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
            return HandlerMethod.this.getMethodAnnotation(annotationType);
        }
    }

}
