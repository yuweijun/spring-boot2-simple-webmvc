

package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.bind.annotation.ExceptionHandler;
import com.example.simple.spring.web.mvc.method.HandlerMethodSelector;
import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHandlerMethodResolver {

    private static final Method NO_METHOD_FOUND = ClassUtils.getMethodIfAvailable(System.class, "currentTimeMillis");

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new ConcurrentHashMap<>();

    private final Map<Class<? extends Throwable>, Method> exceptionLookupCache = new ConcurrentHashMap<>();

    public ExceptionHandlerMethodResolver(Class<?> handlerType) {
        init(HandlerMethodSelector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS));
    }

    private void init(Set<Method> exceptionHandlerMethods) {
        for (Method method : exceptionHandlerMethods) {
            for (Class<? extends Throwable> exceptionType : detectMappedExceptions(method)) {
                addExceptionMapping(exceptionType, method);
            }
        }
    }

    private List<Class<? extends Throwable>> detectMappedExceptions(Method method) {
        List<Class<? extends Throwable>> result = new ArrayList<>();
        ExceptionHandler annotation = AnnotationUtils.findAnnotation(method, ExceptionHandler.class);
        if (annotation != null) {
            result.addAll(Arrays.asList(annotation.value()));
        }
        if (result.isEmpty()) {
            for (Class<?> paramType : method.getParameterTypes()) {
                if (Throwable.class.isAssignableFrom(paramType)) {
                    result.add((Class<? extends Throwable>) paramType);
                }
            }
        }
        Assert.notEmpty(result, "No exception types mapped to {" + method + "}");
        return result;
    }

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
        Method oldMethod = this.mappedMethods.put(exceptionType, method);
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException("Ambiguous @ExceptionHandler method mapped for [" + exceptionType + "]: {" + oldMethod + ", " + method + "}.");
        }
    }

    public Method resolveMethod(Exception exception) {
        Class<? extends Exception> exceptionType = exception.getClass();
        Method method = this.exceptionLookupCache.get(exceptionType);
        if (method == null) {
            method = getMappedMethod(exceptionType);
            this.exceptionLookupCache.put(exceptionType, method != null ? method : NO_METHOD_FOUND);
        }
        return method != NO_METHOD_FOUND ? method : null;
    }

    private Method getMappedMethod(Class<? extends Exception> exceptionType) {
        List<Class<? extends Throwable>> matches = new ArrayList<>();
        for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
            if (mappedException.isAssignableFrom(exceptionType)) {
                matches.add(mappedException);
            }
        }
        if (!matches.isEmpty()) {
            Collections.sort(matches, new ExceptionDepthComparator(exceptionType));
            return mappedMethods.get(matches.get(0));
        } else {
            return null;
        }
    }

    public final static MethodFilter EXCEPTION_HANDLER_METHODS = new MethodFilter() {
        public boolean matches(Method method) {
            return AnnotationUtils.findAnnotation(method, ExceptionHandler.class) != null;
        }
    };

}
