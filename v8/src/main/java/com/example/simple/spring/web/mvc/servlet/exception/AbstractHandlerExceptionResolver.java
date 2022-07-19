package com.example.simple.spring.web.mvc.servlet.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public abstract class AbstractHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private static final String HEADER_PRAGMA = "Pragma";

    private static final String HEADER_EXPIRES = "Expires";

    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    protected final Log logger = LogFactory.getLog(getClass());

    private int order = Ordered.LOWEST_PRECEDENCE;

    private Set mappedHandlers;

    private Class[] mappedHandlerClasses;

    private Log warnLogger;

    private boolean preventResponseCaching = false;

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setMappedHandlers(Set mappedHandlers) {
        this.mappedHandlers = mappedHandlers;
    }

    public void setMappedHandlerClasses(Class[] mappedHandlerClasses) {
        this.mappedHandlerClasses = mappedHandlerClasses;
    }

    public void setWarnLogCategory(String loggerName) {
        this.warnLogger = LogFactory.getLog(loggerName);
    }

    public void setPreventResponseCaching(boolean preventResponseCaching) {
        this.preventResponseCaching = preventResponseCaching;
    }

    public void resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (shouldApplyTo(request, handler)) {
            // Log exception, both at debug log level and at warn level, if desired.
            logger.debug("Resolving exception from handler [" + handler + "]: " + ex);
            logException(ex, request);
            prepareResponse(ex, response);
            doResolveException(request, response, handler, ex);
        }
    }

    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        if (handler != null) {
            if (this.mappedHandlers != null && this.mappedHandlers.contains(handler)) {
                return true;
            }
            if (this.mappedHandlerClasses != null) {
                for (Class handlerClass : this.mappedHandlerClasses) {
                    if (handlerClass.isInstance(handler)) {
                        return true;
                    }
                }
            }
        }
        // Else only apply if there are no explicit handler mappings.
        return (this.mappedHandlers == null && this.mappedHandlerClasses == null);
    }

    protected void logException(Exception ex, HttpServletRequest request) {
        this.logger.warn(buildLogMessage(ex, request), ex);
    }

    protected String buildLogMessage(Exception ex, HttpServletRequest request) {
        return "Handler execution resulted in exception";
    }

    protected void prepareResponse(Exception ex, HttpServletResponse response) {
        if (this.preventResponseCaching) {
            preventCaching(response);
        }
    }

    protected void preventCaching(HttpServletResponse response) {
        response.setHeader(HEADER_PRAGMA, "no-cache");
        response.setDateHeader(HEADER_EXPIRES, 1L);
        response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
        response.addHeader(HEADER_CACHE_CONTROL, "no-store");
    }

    protected abstract void doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);

}
