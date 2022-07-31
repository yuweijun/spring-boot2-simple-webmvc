package com.example.simple.spring.web.mvc.servlet.handler;

import com.example.simple.spring.web.mvc.bind.ExtendedServletRequestDataBinder;
import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.support.DefaultSessionAttributeStore;
import com.example.simple.spring.web.mvc.bind.support.SessionAttributeStore;
import com.example.simple.spring.web.mvc.contex.support.WebApplicationObjectSupport;
import com.example.simple.spring.web.mvc.context.request.SessionAttributesHandler;
import com.example.simple.spring.web.mvc.http.converter.HttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.StringHttpMessageConverter;
import com.example.simple.spring.web.mvc.http.converter.json.MappingJackson2HttpMessageConverter;
import com.example.simple.spring.web.mvc.method.ControllerAdviceBean;
import com.example.simple.spring.web.mvc.method.HandlerMethod;
import com.example.simple.spring.web.mvc.method.HandlerMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.HandlerMethodArgumentResolverComposite;
import com.example.simple.spring.web.mvc.method.HttpEntityMethodProcessor;
import com.example.simple.spring.web.mvc.method.InvocableHandlerMethod;
import com.example.simple.spring.web.mvc.method.MapMethodProcessor;
import com.example.simple.spring.web.mvc.method.ModelAttributeMethodProcessor;
import com.example.simple.spring.web.mvc.method.ModelFactory;
import com.example.simple.spring.web.mvc.method.RequestAttributeMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.RequestParamMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.RequestResponseBodyMethodProcessor;
import com.example.simple.spring.web.mvc.method.ServletInvocableHandlerMethod;
import com.example.simple.spring.web.mvc.method.ServletModelAttributeMethodProcessor;
import com.example.simple.spring.web.mvc.method.ServletRequestMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.ServletResponseMethodArgumentResolver;
import com.example.simple.spring.web.mvc.method.SessionAttributeMethodArgumentResolver;
import com.example.simple.spring.web.mvc.servlet.HandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.support.HandlerMethodReturnValueHandlerComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RequestMappingHandlerAdapter extends WebApplicationObjectSupport implements HandlerAdapter, BeanFactoryAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingHandlerAdapter.class);

    private List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);

    private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);

    private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private ConfigurableBeanFactory beanFactory;

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public void setReturnValueHandler(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public void setArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
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

    @Override
    public void afterPropertiesSet() {
        initControllerAdviceCache();
        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
    }

    private void initControllerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }

        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());

        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }

            Set<Method> attrMethods = MethodIntrospector.selectMethods(beanType, MODEL_ATTRIBUTE_METHODS);
            if (!attrMethods.isEmpty()) {
                this.modelAttributeAdviceCache.put(adviceBean, attrMethods);
            }

        }

        int modelSize = this.modelAttributeAdviceCache.size();
        logger.debug("ControllerAdvice beans: " + modelSize + " @ModelAttribute");
    }

    private HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
        if (this.returnValueHandlers == null) {
            if (messageConverters.isEmpty()) {
                messageConverters.add(new MappingJackson2HttpMessageConverter());
                messageConverters.add(new StringHttpMessageConverter());
            }

            returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            returnValueHandlers.addHandler(new RequestResponseBodyMethodProcessor(messageConverters));
            returnValueHandlers.addHandler(new HttpEntityMethodProcessor(messageConverters));
            returnValueHandlers.addHandler(new ModelAttributeMethodProcessor());
        }

        return this.returnValueHandlers;
    }

    private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
        resolvers.add(new ServletModelAttributeMethodProcessor());
        resolvers.add(new RequestResponseBodyMethodProcessor(messageConverters));
        resolvers.add(new SessionAttributeMethodArgumentResolver());
        resolvers.add(new RequestAttributeMethodArgumentResolver());

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());
        resolvers.add(new HttpEntityMethodProcessor(messageConverters));
        resolvers.add(new MapMethodProcessor());

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

        return resolvers;
    }

    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    protected final void handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        invokeHandlerMethod(request, response, handlerMethod);
    }

    private ServletInvocableHandlerMethod createRequestMappingMethod(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
        ServletInvocableHandlerMethod requestMethod = new ServletInvocableHandlerMethod(handlerMethod.getBean(), handlerMethod.getMethod(), request, response);
        requestMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
        requestMethod.setParameterNameDiscoverer(parameterNameDiscoverer);
        requestMethod.setHandlerMethodReturnValueHandlers(getReturnValueHandlers());

        ExtendedServletRequestDataBinder setServletRequestDataBinder = new ExtendedServletRequestDataBinder();
        requestMethod.setServletRequestDataBinder(setServletRequestDataBinder);
        return requestMethod;
    }

    private void invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        final Method method = handlerMethod.getMethod();
        final MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        LOGGER.debug("methodParameters is : {}", Arrays.asList(methodParameters));

        final Object bean = handlerMethod.getBean();
        LOGGER.info("method [{}] invoke in bean [{}]", method.getName(), bean.getClass().getSimpleName());

        ModelFactory modelFactory = getModelFactory(handlerMethod, request, response);
        modelFactory.initModel(request, handlerMethod);

        final ServletInvocableHandlerMethod invocableHandlerMethod = createRequestMappingMethod(handlerMethod, request, response);
        invocableHandlerMethod.invokeAndHandle();

        modelFactory.updateModel(request);
    }

    private ModelFactory getModelFactory(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
        SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.modelAttributeCache.get(handlerType);
        if (methods == null) {
            methods = MethodIntrospector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
            logger.info("get @ModelAttribute methods : " + methods);
            this.modelAttributeCache.put(handlerType, methods);
        }

        List<InvocableHandlerMethod> attrMethods = new ArrayList<>();

        // Global methods first
        this.modelAttributeAdviceCache.forEach((controllerAdviceBean, methodSet) -> {
            if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
                Object bean = controllerAdviceBean.resolveBean();
                for (Method method : methodSet) {
                    attrMethods.add(createModelAttributeMethod(bean, method, request, response));
                }
            }
        });
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            attrMethods.add(createModelAttributeMethod(bean, method, request, response));
        }

        return new ModelFactory(attrMethods, sessionAttrHandler);
    }

    private InvocableHandlerMethod createModelAttributeMethod(Object bean, Method method, HttpServletRequest request, HttpServletResponse response) {
        InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method, request, response);
        if (this.argumentResolvers != null) {
            attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        }
        attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        return attrMethod;
    }

    private SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
        return this.sessionAttributesHandlerCache.computeIfAbsent(handlerMethod.getBeanType(), type -> new SessionAttributesHandler(type, this.sessionAttributeStore));
    }

    public static final ReflectionUtils.MethodFilter MODEL_ATTRIBUTE_METHODS = new ReflectionUtils.MethodFilter() {
        public boolean matches(Method method) {
            return ((AnnotationUtils.findAnnotation(method, ModelAttribute.class) != null) && (AnnotationUtils.findAnnotation(method, RequestMapping.class) == null));
        }
    };
}
