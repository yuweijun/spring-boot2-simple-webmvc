package com.example.simple.spring.web.mvc.servlet;

import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.util.NestedServletException;
import com.example.simple.spring.web.mvc.util.UrlPathHelper;
import com.example.simple.spring.web.mvc.util.WebUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DispatcherServlet extends FrameworkServlet {

    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

    public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

    public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String LOCALE_RESOLVER_ATTRIBUTE = com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

    public static final String THEME_RESOLVER_ATTRIBUTE = com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class.getName() + ".THEME_RESOLVER";

    public static final String THEME_SOURCE_ATTRIBUTE = com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class.getName() + ".THEME_SOURCE";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";
    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);
    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";
    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private static final Properties defaultStrategies;

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
        }
    }

    private boolean detectAllHandlerMappings = true;

    private boolean detectAllHandlerAdapters = true;

    private boolean detectAllHandlerExceptionResolvers = true;

    private boolean detectAllViewResolvers = true;

    private boolean cleanupAfterInclude = true;

    private List<HandlerMapping> handlerMappings;

    private List<HandlerAdapter> handlerAdapters;

    public DispatcherServlet() {
        super();
    }

    public DispatcherServlet(SimpleWebApplicationContext simpleWebApplicationContext) {
        super(simpleWebApplicationContext);
    }

    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
        this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
    }

    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
        this.cleanupAfterInclude = cleanupAfterInclude;
    }

    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    protected void initStrategies(ApplicationContext context) {
        initHandlerMappings(context);
    }

    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                OrderComparator.sort(this.handlerMappings);
            }
        } else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    public final ThemeSource getThemeSource() {
        if (getWebApplicationContext() instanceof ThemeSource) {
            return (ThemeSource) getWebApplicationContext();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<T>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, com.example.simple.spring.web.mvc.servlet.DispatcherServlet.class.getClassLoader());
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException("Could not find DispatcherServlet's default strategy class [" + className + "] for interface [" + key + "]", ex);
                } catch (LinkageError err) {
                    throw new BeanInitializationException(
                        "Error loading DispatcherServlet's default strategy class [" + className + "] for interface [" + key + "]: problem with class file or dependent class",
                        err);
                }
            }
            return strategies;
        } else {
            return new LinkedList<T>();
        }
    }

    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
            String requestUri = urlPathHelper.getRequestUri(request);
            logger.debug("DispatcherServlet with name '" + getServletName() + "' processing " + request.getMethod() + " request for [" + requestUri + "]");
        }

        // Keep a snapshot of the request attributes in case of an include,
        // to be able to restore the original attributes after the include.
        Map<String, Object> attributesSnapshot = null;
        if (WebUtils.isIncludeRequest(request)) {
            logger.debug("Taking snapshot of request attributes before include");
            attributesSnapshot = new HashMap<String, Object>();
            Enumeration<?> attrNames = request.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
                if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
                    attributesSnapshot.put(attrName, request.getAttribute(attrName));
                }
            }
        }

        // Make framework objects available to handlers and view objects.
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
        request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

        try {
            doDispatch(request, response);
        } finally {
            // Restore the original attribute snapshot, in case of an include.
            if (attributesSnapshot != null) {
                restoreAttributesAfterInclude(request, attributesSnapshot);
            }
        }
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerExecutionChain mappedHandler = null;
        int interceptorIndex = -1;

        try {
            try {
                // Determine handler for the current request.
                mappedHandler = getHandler(request);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    noHandlerFound(request, response);
                    return;
                }

                // Determine handler adapter for the current request.
                logger.debug("getHandlerAdapter from mappedHandler : " + request);
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                // Apply preHandle methods of registered interceptors.
                HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
                if (interceptors != null) {
                    for (int i = 0; i < interceptors.length; i++) {
                        HandlerInterceptor interceptor = interceptors[i];
                        if (!interceptor.preHandle(request, response, mappedHandler.getHandler())) {
                            triggerAfterCompletion(mappedHandler, interceptorIndex, request, response, null);
                            return;
                        }
                        interceptorIndex = i;
                    }
                }

                logger.debug("handlerAdapter start handle request : " + request);
                // Actually invoke the handler.
                ha.handle(request, response, mappedHandler.getHandler());

                // Apply postHandle methods of registered interceptors.
                if (interceptors != null) {
                    for (int i = interceptors.length - 1; i >= 0; i--) {
                        HandlerInterceptor interceptor = interceptors[i];
                        interceptor.postHandle(request, response, mappedHandler.getHandler());
                    }
                }
            } catch (Exception ex) {
                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
                processHandlerException(request, response, handler, ex);
            }

            // Trigger after-completion for successful outcome.
            triggerAfterCompletion(mappedHandler, interceptorIndex, request, response, null);
        } catch (Exception ex) {
            // Trigger after-completion for thrown exception.
            triggerAfterCompletion(mappedHandler, interceptorIndex, request, response, ex);
            throw ex;
        } catch (Error err) {
            ServletException ex = new NestedServletException("Handler processing failed", err);
            // Trigger after-completion for thrown exception.
            triggerAfterCompletion(mappedHandler, interceptorIndex, request, response, ex);
            throw ex;
        }
    }

    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
            }
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            String requestUri = urlPathHelper.getRequestUri(request);
            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + requestUri + "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler + "]: Does your handler implement a supported interface like Controller?");
    }

    protected void processHandlerException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        throw ex;
    }

    private void triggerAfterCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex, HttpServletRequest request, HttpServletResponse response, Exception ex)
        throws Exception {

        // Apply afterCompletion methods of registered interceptors.
        if (mappedHandler != null) {
            HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
            if (interceptors != null) {
                for (int i = interceptorIndex; i >= 0; i--) {
                    HandlerInterceptor interceptor = interceptors[i];
                    try {
                        interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
                    } catch (Throwable ex2) {
                        logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                    }
                }
            }
        }
    }

    private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?, ?> attributesSnapshot) {
        logger.debug("Restoring snapshot of request attributes after include");

        // Need to copy into separate Collection here, to avoid side effects
        // on the Enumeration when removing attributes.
        Set<String> attrsToCheck = new HashSet<String>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
                attrsToCheck.add(attrName);
            }
        }

        // Iterate over the attributes to check, restoring the original value
        // or removing the attribute, respectively, if appropriate.
        for (String attrName : attrsToCheck) {
            Object attrValue = attributesSnapshot.get(attrName);
            if (attrValue == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attribute [" + attrName + "] after include");
                }
                request.removeAttribute(attrName);
            } else if (attrValue != request.getAttribute(attrName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring original value of attribute [" + attrName + "] after include");
                }
                request.setAttribute(attrName, attrValue);
            }
        }
    }

}
