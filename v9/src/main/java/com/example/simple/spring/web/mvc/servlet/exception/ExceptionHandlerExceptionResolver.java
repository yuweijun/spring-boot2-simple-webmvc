package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.StringHttpMessageConverter;
import com.example.simple.spring.web.mvc.method.ControllerAdviceBean;
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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHandlerExceptionResolver extends AbstractHandlerMethodExceptionResolver implements ApplicationContextAware, InitializingBean {

    private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private List<HttpMessageConverter<?>> messageConverters;

    // private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private final List<Object> responseBodyAdvice = new ArrayList<>();

    private ApplicationContext applicationContext;

    private final Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache = new ConcurrentHashMap<>(64);

    private final Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache = new LinkedHashMap<>();

    public ExceptionHandlerExceptionResolver() {
        this.messageConverters = new ArrayList<>();
        this.messageConverters.add(new StringHttpMessageConverter());
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
        } else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    public HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
        return this.returnValueHandlers;
    }

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

    // public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
    //     this.contentNegotiationManager = contentNegotiationManager;
    // }

    // public ContentNegotiationManager getContentNegotiationManager() {
    //     return this.contentNegotiationManager;
    // }

    // public void setResponseBodyAdvice( List<ResponseBodyAdvice<?>> responseBodyAdvice) {
    //     if (responseBodyAdvice != null) {
    //         this.responseBodyAdvice.addAll(responseBodyAdvice);
    //     }
    // }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initExceptionHandlerAdviceCache();

        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    private void initExceptionHandlerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }

        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
        logger.info("adviceBeans size is " + adviceBeans.size());
        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }

            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(beanType);
            if (resolver.hasExceptionMappings()) {
                this.exceptionHandlerAdviceCache.put(adviceBean, resolver);
            }

            // if (ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
            //     this.responseBodyAdvice.add(adviceBean);
            // }
        }

        int handlerSize = this.exceptionHandlerAdviceCache.size();
        int adviceSize = this.responseBodyAdvice.size();
        if (handlerSize == 0 && adviceSize == 0) {
            logger.debug("ControllerAdvice beans: none");
        } else {
            logger.debug("ControllerAdvice beans: " + handlerSize + " @ExceptionHandler, " + adviceSize + " ResponseBodyAdvice");
        }
    }

    public Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> getExceptionHandlerAdviceCache() {
        return Collections.unmodifiableMap(this.exceptionHandlerAdviceCache);
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
    protected boolean hasGlobalExceptionHandlers() {
        return !this.exceptionHandlerAdviceCache.isEmpty();
    }

    @Override
    protected void doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        ServletInvocableHandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod(request, response, handlerMethod, exception);
        if (exceptionHandlerMethod == null) {
            return;
        }

        if (this.argumentResolvers != null) {
            exceptionHandlerMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        }
        if (this.returnValueHandlers != null) {
            exceptionHandlerMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        }

        ArrayList<Throwable> exceptions = new ArrayList<>();
        try {
            logger.debug("Using @ExceptionHandler " + exceptionHandlerMethod);

            // Expose causes as provided arguments as well
            Throwable exToExpose = exception;
            while (exToExpose != null) {
                exceptions.add(exToExpose);
                Throwable cause = exToExpose.getCause();
                exToExpose = (cause != exToExpose ? cause : null);
            }

            Object[] arguments = new Object[exceptions.size() + 1];
            exceptions.toArray(arguments);  // efficient arraycopy call in ArrayList
            arguments[arguments.length - 1] = handlerMethod;
            exceptionHandlerMethod.invokeAndHandle(arguments);
        } catch (Throwable invocationEx) {
            // Any other than the original exception (or a cause) is unintended here,
            // probably an accident (e.g. failed assertion or the like).
            if (!exceptions.contains(invocationEx) && logger.isWarnEnabled()) {
                logger.warn("Failure in @ExceptionHandler " + exceptionHandlerMethod, invocationEx);
            }
            // Continue with default processing of the original exception...
        }
    }

    protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {
        Class<?> handlerType = null;

        if (handlerMethod != null) {
            // Local exception handler methods on the controller class itself.
            // To be invoked through the proxy, even in case of an interface-based proxy.
            handlerType = handlerMethod.getBeanType();
            ExceptionHandlerMethodResolver resolver = this.exceptionHandlerCache.get(handlerType);
            if (resolver == null) {
                resolver = new ExceptionHandlerMethodResolver(handlerType);
                this.exceptionHandlerCache.put(handlerType, resolver);
            }
            Method method = resolver.resolveMethod(exception);
            if (method != null) {
                logger.info("controller advice [" + handlerType.getClass().getSimpleName() + "] find exception resolve method : " + method.getName());
                return new ServletInvocableHandlerMethod(handlerMethod.getBean(), method, request, response);
            }

            // For advice applicability check below (involving base packages, assignable types
            // and annotation presence), use target class instead of interface-based proxy.
            if (Proxy.isProxyClass(handlerType)) {
                handlerType = AopUtils.getTargetClass(handlerMethod.getBean());
            }
        }

        for (Map.Entry<ControllerAdviceBean, ExceptionHandlerMethodResolver> entry : this.exceptionHandlerAdviceCache.entrySet()) {
            ControllerAdviceBean advice = entry.getKey();
            if (advice.isApplicableToBeanType(handlerType)) {
                ExceptionHandlerMethodResolver resolver = entry.getValue();
                Method method = resolver.resolveMethod(exception);
                if (method != null) {
                    logger.info("controller advice [" + advice.getClass().getSimpleName() + "] find exception resolve method : " + method.getName());
                    return new ServletInvocableHandlerMethod(advice.resolveBean(), method, request, response);
                }
            }
        }

        return null;
    }

}
