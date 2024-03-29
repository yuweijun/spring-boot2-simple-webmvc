package com.example.simple.spring.web.mvc.servlet;

import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.servlet.exception.ExceptionHandlerExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.exception.HandlerExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.handler.HttpRequestHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.SimpleUrlHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.view.InternalResourceViewResolver;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import com.example.simple.spring.web.mvc.servlet.view.View;
import com.example.simple.spring.web.mvc.servlet.view.ViewResolver;
import com.example.simple.spring.web.util.NestedServletException;
import com.example.simple.spring.web.util.UrlPathHelper;
import com.example.simple.spring.web.util.WebUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.ui.ModelMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

    public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

    public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private HandlerExceptionResolver handlerExceptionResolver;

    private boolean detectAllHandlerMappings = true;

    private boolean detectAllHandlerAdapters = true;

    private boolean detectAllViewResolvers = true;

    private boolean cleanupAfterInclude = true;

    private List<HandlerMapping> handlerMappings;

    private List<HandlerAdapter> handlerAdapters;

    private List<ViewResolver> viewResolvers;

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

    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
        this.cleanupAfterInclude = cleanupAfterInclude;
    }

    @Override
    protected void onRefresh(ApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initViewResolvers(context);
    }

    private void initHandlerMappings(ApplicationContext context) {
        logger.debug("initHandlerMappings for context");

        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);

            matchingBeans.values().forEach(logger::debug);

            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
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
            logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            this.handlerMappings = new ArrayList<>();
            final SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
            simpleUrlHandlerMapping.setRootHandler("rootController");
            handlerMappings.add(simpleUrlHandlerMapping);
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                OrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            final HttpRequestHandlerAdapter httpRequestHandlerAdapter = new HttpRequestHandlerAdapter();
            this.handlerAdapters = new ArrayList<>();
            handlerAdapters.add(httpRequestHandlerAdapter);
        }
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
        try {
            this.handlerExceptionResolver = context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
        } catch (NoSuchBeanDefinitionException ex) {
            // Ignore, no HandlerExceptionResolver is fine too.
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolver == null) {
            logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
            this.handlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        }
    }

    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        if (this.detectAllViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
            logger.info("match view resolvers bean : " + matchingBeans.values());
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                OrderComparator.sort(this.viewResolvers);
            }
        } else {
            try {
                ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
                this.viewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        // Ensure we have at least one ViewResolver, by registering
        // a default ViewResolver if no other resolvers are found.
        if (this.viewResolvers == null) {
            this.viewResolvers = new ArrayList<>();
            this.viewResolvers.add(new InternalResourceViewResolver());
            logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
        }
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = urlPathHelper.getRequestUri(request);
        logger.debug("DispatcherServlet with name '" + getServletName() + "' processing " + request.getMethod() + " request for [" + requestUri + "]");

        // Keep a snapshot of the request attributes in case of an include,
        // to be able to restore the original attributes after the include.
        Map<String, Object> attributesSnapshot = null;
        if (WebUtils.isIncludeRequest(request)) {
            logger.debug("Taking snapshot of request attributes before include");
            attributesSnapshot = new HashMap<>();
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
        ModelAndView.clear(request);
        HandlerExecutionChain mappedHandler = null;
        int interceptorIndex = -1;

        try {
            try {
                // Determine handler for the current request.
                mappedHandler = getHandler(request);
                if (mappedHandler == null) {
                    return;
                }
                logger.info("request handler is " + mappedHandler);

                final Object handler = mappedHandler.getHandler();
                if (handler == null) {
                    noHandlerFound(request, response);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(handler);
                logger.debug("getHandlerAdapter from mappedHandler : " + ha);

                // Apply preHandle methods of registered interceptors.
                HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
                if (interceptors != null) {
                    for (int i = 0; i < interceptors.length; i++) {
                        HandlerInterceptor interceptor = interceptors[i];
                        if (!interceptor.preHandle(request, response, handler)) {
                            triggerAfterCompletion(mappedHandler, interceptorIndex, request, response, null);
                            return;
                        }
                        interceptorIndex = i;
                    }
                }

                logger.debug("handlerAdapter start handle request : " + request);
                // Actually invoke the handler.
                ha.handle(request, response, handler);

                // Apply postHandle methods of registered interceptors.
                if (interceptors != null) {
                    for (int i = interceptors.length - 1; i >= 0; i--) {
                        HandlerInterceptor interceptor = interceptors[i];
                        interceptor.postHandle(request, response, handler);
                    }
                }
            } catch (Exception ex) {
                processHandlerException(request, response, (mappedHandler != null ? mappedHandler.getHandler() : null), ex);
            }

            if (ModelAndView.hasView(request)) {
                render(request, response);
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
            logger.debug("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter handlerAdapter : this.handlerAdapters) {
            if (handlerAdapter.supports(handler)) {
                logger.debug("match handler adapter [" + handlerAdapter + "]");
                return handlerAdapter;
            }
        }
        throw new ServletException("No adapter for handler [" + handler + "]: Does your handler implement a supported interface like Controller?");
    }

    protected void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
        View view = ModelAndView.getView(request);
        ModelMap model = ModelAndView.getModel(request);
        if (view == null) {
            // We need to resolve the view name.
            final String viewName = ModelAndView.getViewName(request);
            logger.debug("render ModelAndView by viewName : " + viewName);
            view = resolveViewName(viewName, model, request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + viewName + "' in servlet with name '" + getServletName() + "'");
            }
        }

        // Delegate to the View object for rendering.
        logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
        view.render(model, request, response);
    }

    protected View resolveViewName(String viewName, Map<String, Object> model, HttpServletRequest request) throws Exception {
        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                return view;
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

    protected void processHandlerException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.error("process handler exception for request : " + request.getRequestURI(), ex);
        handlerExceptionResolver.resolveException(request, response, handler, ex);
    }

    private void triggerAfterCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex, HttpServletRequest request, HttpServletResponse response, Exception ex) throws Exception {
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
        Set<String> attrsToCheck = new HashSet<>();
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
