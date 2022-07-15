package com.example.simple.spring.web.mvc.servlet;

import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.util.UrlPathHelper;
import com.example.simple.spring.web.mvc.servlet.handler.SimpleControllerHandlerAdapter;
import com.example.simple.spring.web.mvc.servlet.handler.SimpleUrlHandlerMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends FrameworkServlet {

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private List<HandlerMapping> handlerMappings;

    private List<HandlerAdapter> handlerAdapters;

    public DispatcherServlet() {
        super();
    }

    public DispatcherServlet(SimpleWebApplicationContext simpleWebApplicationContext) {
        super(simpleWebApplicationContext);
    }

    @Override
    protected void onRefresh(ApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters(context);
    }

    private void initHandlerMappings(ApplicationContext context) {
        logger.debug("initHandlerMappings for context");

        this.handlerMappings = null;

        // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
        Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        logger.debug("matchingBeans : " + matchingBeans);

        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList<>(matchingBeans.values());
            // We keep HandlerMappings in sorted order.
            OrderComparator.sort(this.handlerMappings);
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            this.handlerMappings = new ArrayList<>();
            final SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
            handlerMappings.add(simpleUrlHandlerMapping);
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
        Map<String, HandlerAdapter> matchingBeans =
            BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
            // We keep HandlerAdapters in sorted order.
            OrderComparator.sort(this.handlerAdapters);
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            final SimpleControllerHandlerAdapter simpleControllerHandlerAdapter = new SimpleControllerHandlerAdapter();
            this.handlerAdapters = new ArrayList<>();
            handlerAdapters.add(simpleControllerHandlerAdapter);
        }
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = urlPathHelper.getRequestUri(request);
        logger.debug("DispatcherServlet with name '" + getServletName() + "' processing " + request.getMethod() + " request for [" + requestUri + "]");

        doDispatch(request, response);
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Determine handler for the current request.
        HandlerExecutionChain mappedHandler = getHandler(request);
        if (mappedHandler == null) {
            logger.info("not found mapped handler for request : " + request.getRequestURI());
            return;
        }

        final Object handler = mappedHandler.getHandler();
        if (handler == null) {
            noHandlerFound(request, response);
            return;
        }

        // Determine handler adapter for the current request.
        logger.debug("getHandlerAdapter from mappedHandler : " + request);
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        logger.debug("handlerAdapter start handle request : " + request);
        // Actually invoke the handler.
        handlerAdapter.handle(request, response, handler);
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

    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = urlPathHelper.getRequestUri(request);
        pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + requestUri + "] in DispatcherServlet with name '" + getServletName() + "'");
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
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

}
