package com.example.simple.spring.web.mvc.method;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvocableHandlerMethod extends HandlerMethod {

    private HandlerMethodArgumentResolverComposite argumentResolvers = new HandlerMethodArgumentResolverComposite();

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    public InvocableHandlerMethod(Object bean, Method method, HttpServletRequest request, HttpServletResponse response) {
        super(bean, method);
        this.request = request;
        this.response = response;
    }

    public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    public final Object invokeForRequest(Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(request, response, providedArgs);
        logger.debug("Invoking [" + this.getMethod().getName() + "] method with arguments " + Arrays.asList(args));

        Object returnValue = invoke(args);

        logger.debug("Method [" + this.getMethod().getName() + "] returned [" + returnValue + "]");
        return returnValue;
    }

    private Object[] getMethodArgumentValues(Object... providedArgs) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(parameterNameDiscoverer);
            final Class<?> aClass = GenericTypeResolver.resolveParameterType(parameter, getBean().getClass());
            final Class<?> parameterType = parameter.withContainingClass(getBean().getClass()).getParameterType();
            logger.debug("class " + aClass.getSimpleName() + ", type " + parameterType.getSimpleName());

            args[i] = resolveProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }

            if (argumentResolvers.supportsParameter(parameter)) {
                try {
                    args[i] = argumentResolvers.resolveArgument(parameter, request, response);
                    continue;
                } catch (Exception ex) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(getArgumentResolutionErrorMessage("Error resolving argument", i), ex);
                    }
                    throw ex;
                }
            }

            if (args[i] == null) {
                String msg = getArgumentResolutionErrorMessage("No suitable resolver for argument", i);
                throw new IllegalStateException(msg);
            }
        }
        return args;
    }

    private String getArgumentResolutionErrorMessage(String message, int index) {
        MethodParameter param = getMethodParameters()[index];
        message += " [" + index + "] [type=" + param.getParameterType().getName() + "]";
        return getDetailedErrorMessage(message);
    }

    protected String getDetailedErrorMessage(String message) {
        StringBuilder sb = new StringBuilder(message).append("\n");
        sb.append("HandlerMethod details: \n");
        sb.append("Controller [").append(getBeanType().getName()).append("]\n");
        sb.append("Method [").append(getBridgedMethod().toGenericString()).append("]\n");
        return sb.toString();
    }

    private Object resolveProvidedArgument(MethodParameter parameter, Object... providedArgs) {
        if (providedArgs == null) {
            return null;
        }

        final boolean hasAnnotation = parameter.hasParameterAnnotations();
        if (hasAnnotation) {
            logger.debug("parameter has annotation : " + Arrays.asList(parameter.getMethodAnnotations()));
            return null;
        }

        for (Object providedArg : providedArgs) {
            if (parameter.getParameterType().isInstance(providedArg)) {
                return providedArg;
            }
        }

        return null;
    }

    private Object invoke(Object... args) throws Exception {
        ReflectionUtils.makeAccessible(this.getBridgedMethod());
        try {
            return getBridgedMethod().invoke(getBean(), args);
        } catch (IllegalArgumentException e) {
            String msg = getInvocationErrorMessage(e.getMessage(), args);
            throw new IllegalArgumentException(msg, e);
        } catch (InvocationTargetException e) {
            // Unwrap for HandlerExceptionResolvers ...
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else if (targetException instanceof Error) {
                throw (Error) targetException;
            } else if (targetException instanceof Exception) {
                throw (Exception) targetException;
            } else {
                String msg = getInvocationErrorMessage("Failed to invoke controller method", args);
                throw new IllegalStateException(msg, targetException);
            }
        }
    }

    private String getInvocationErrorMessage(String message, Object[] resolvedArgs) {
        StringBuilder sb = new StringBuilder(getDetailedErrorMessage(message));
        sb.append("Resolved arguments: \n");
        for (int i = 0; i < resolvedArgs.length; i++) {
            sb.append("[").append(i).append("] ");
            if (resolvedArgs[i] == null) {
                sb.append("[null] \n");
            } else {
                sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
                sb.append("[value=").append(resolvedArgs[i]).append("]\n");
            }
        }
        return sb.toString();
    }

}
