package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.StringHttpMessageConverter;
import com.example.simple.spring.web.mvc.method.HandlerMethod;
import com.example.simple.spring.web.mvc.method.HandlerMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.HandlerMethodArgumentResolverComposite;
import com.example.simple.spring.web.mvc.method.HandlerMethodReturnValueHandler;
import com.example.simple.spring.web.mvc.method.MapMethodProcessor;
import com.example.simple.spring.web.mvc.method.RequestResponseBodyMethodProcessor;
import com.example.simple.spring.web.mvc.method.ServletInvocableHandlerMethod;
import com.example.simple.spring.web.mvc.method.ServletRequestMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.ServletResponseMethodArgumentResolver;
import com.example.simple.spring.web.mvc.servlet.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHandlerExceptionResolver extends AbstractHandlerMethodExceptionResolver implements
    InitializingBean {

    private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    private List<HttpMessageConverter<?>> messageConverters;

    private final Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerMethodResolvers = new ConcurrentHashMap<>();

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public ExceptionHandlerExceptionResolver() {

        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false); // See SPR-7316

        this.messageConverters = new ArrayList<>();
        this.messageConverters.add(stringHttpMessageConverter);
    }

    public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.customArgumentResolvers = argumentResolvers;
    }

    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return this.customArgumentResolvers;
    }

    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = null;
        } else {
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolvers(argumentResolvers);
        }
    }

    public HandlerMethodArgumentResolverComposite getArgumentResolvers() {
        return this.argumentResolvers;
    }

    public void setCustomReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        this.customReturnValueHandlers = returnValueHandlers;
    }

    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return this.customReturnValueHandlers;
    }

    public void setReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null) {
            this.returnValueHandlers = null;
        }
        else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    public HandlerMethodReturnValueHandler getReturnValueHandlers() {
        return this.returnValueHandlers;
    }

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return messageConverters;
    }

    public void afterPropertiesSet() {
        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    protected List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        return resolvers;
    }

    protected List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        // Single-purpose return value types
        // handlers.add(new ModelAndViewMethodReturnValueHandler());
        // handlers.add(new ModelMethodProcessor());
        // handlers.add(new ViewMethodReturnValueHandler());
        // handlers.add(new HttpEntityMethodProcessor(getMessageConverters()));

        // Annotation-based return value types
        // handlers.add(new ModelAttributeMethodProcessor(false));
        handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters()));

        // Multi-purpose return value types
        // handlers.add(new ViewNameMethodReturnValueHandler());
        handlers.add(new MapMethodProcessor());

        // Custom return value types
        if (getCustomReturnValueHandlers() != null) {
            handlers.addAll(getCustomReturnValueHandlers());
        }

        // Catch-all
        // handlers.add(new ModelAttributeMethodProcessor(true));

        return handlers;
    }

    @Override
    protected void doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        if (handlerMethod == null) {
            logger.debug("handlerMethod is null");
            return;
        }

        ServletInvocableHandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod(request, response, handlerMethod, exception);
        if (exceptionHandlerMethod == null) {
            logger.debug("exceptionHandlerMethod is not found");
            return;
        }

        exceptionHandlerMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        exceptionHandlerMethod.setHandlerMethodReturnValueHandler(this.returnValueHandlers);

        try {
            logger.debug("Invoking @ExceptionHandler method: " + exceptionHandlerMethod);
            exceptionHandlerMethod.invokeAndHandle(request, response, exception);
        } catch (Exception invocationEx) {
            logger.error("Failed to invoke @ExceptionHandler method: " + exceptionHandlerMethod, invocationEx);
        }

    }

    protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        Class<?> handlerType = handlerMethod.getBeanType();
        final ExceptionHandlerMethodResolver exceptionHandlerMethodResolver = getExceptionHandlerMethodResolver(handlerType);
        Method method = exceptionHandlerMethodResolver.resolveMethod(exception);
        return (method != null ? new ServletInvocableHandlerMethod(handlerMethod.getBean(), method, request, response) : null);
    }

    private ExceptionHandlerMethodResolver getExceptionHandlerMethodResolver(Class<?> handlerType) {
        ExceptionHandlerMethodResolver resolver = this.exceptionHandlerMethodResolvers.get(handlerType);
        if (resolver == null) {
            resolver = new ExceptionHandlerMethodResolver(handlerType);
            this.exceptionHandlerMethodResolvers.put(handlerType, resolver);
        }
        return resolver;
    }

}
