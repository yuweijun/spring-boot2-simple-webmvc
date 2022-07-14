package com.example.simple.spring.web.mvc.servlet;

import com.example.simple.spring.web.mvc.context.SimpleAnnotationConfigServletWebServerApplicationContext;
import com.example.simple.spring.web.mvc.context.SimpleConfigurableWebApplicationContext;
import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.context.support.WebApplicationContextUtils;
import com.example.simple.spring.web.mvc.contex.request.RequestAttributes;
import com.example.simple.spring.web.mvc.contex.request.RequestContextHolder;
import com.example.simple.spring.web.mvc.contex.request.ServletRequestAttributes;
import com.example.simple.spring.web.mvc.util.NestedServletException;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;

public abstract class FrameworkServlet extends HttpServletBean {

    public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

    public static final Class<?> DEFAULT_CONTEXT_CLASS = SimpleAnnotationConfigServletWebServerApplicationContext.class;

    public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

    private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

    private String contextAttribute;

    private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

    private String contextId;

    private String namespace;

    private String contextConfigLocation;

    private boolean publishContext = true;

    private boolean publishEvents = true;

    private boolean threadContextInheritable = false;

    private boolean dispatchOptionsRequest = false;

    private boolean dispatchTraceRequest = false;

    private SimpleWebApplicationContext simpleWebApplicationContext;

    private boolean refreshEventReceived = false;

    private String contextInitializerClasses;

    private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers = new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();

    public FrameworkServlet() {
    }

    public FrameworkServlet(SimpleWebApplicationContext simpleWebApplicationContext) {
        this.simpleWebApplicationContext = simpleWebApplicationContext;
    }

    public String getContextAttribute() {
        return this.contextAttribute;
    }

    public void setContextAttribute(String contextAttribute) {
        this.contextAttribute = contextAttribute;
    }

    public Class<?> getContextClass() {
        return this.contextClass;
    }

    public void setContextClass(Class<?> contextClass) {
        this.contextClass = contextClass;
    }

    public String getContextId() {
        return this.contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getNamespace() {
        return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setContextInitializerClasses(String contextInitializerClasses) {
        this.contextInitializerClasses = contextInitializerClasses;
    }

    public void setContextInitializers(ApplicationContextInitializer<ConfigurableApplicationContext>... contextInitializers) {
        for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : contextInitializers) {
            this.contextInitializers.add(initializer);
        }
    }

    public String getContextConfigLocation() {
        return this.contextConfigLocation;
    }

    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void setPublishContext(boolean publishContext) {
        this.publishContext = publishContext;
    }

    public void setPublishEvents(boolean publishEvents) {
        this.publishEvents = publishEvents;
    }

    public void setThreadContextInheritable(boolean threadContextInheritable) {
        this.threadContextInheritable = threadContextInheritable;
    }

    public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
        this.dispatchOptionsRequest = dispatchOptionsRequest;
    }

    public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
        this.dispatchTraceRequest = dispatchTraceRequest;
    }

    @Override
    protected final void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
        if (this.logger.isInfoEnabled()) {
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            this.simpleWebApplicationContext = initWebApplicationContext();
            initFrameworkServlet();
        } catch (ServletException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        } catch (RuntimeException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        }

        if (this.logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " + elapsedTime + " ms");
        }
    }

    protected SimpleWebApplicationContext initWebApplicationContext() {
        SimpleWebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        SimpleWebApplicationContext wac = null;

        if (this.simpleWebApplicationContext != null) {
            // A context instance was injected at construction time -> use it
            wac = this.simpleWebApplicationContext;
            if (wac instanceof SimpleConfigurableWebApplicationContext) {
                SimpleConfigurableWebApplicationContext cwac = (SimpleConfigurableWebApplicationContext) wac;
                if (!cwac.isActive()) {
                    // The context has not yet been refreshed -> provide services such as
                    // setting the parent context, setting the application context id, etc
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent -> set
                        // the root application context (if any; may be null) as the parent
                        cwac.setParent(rootContext);
                    }
                    configureAndRefreshWebApplicationContext(cwac);
                }
            }
        }
        if (wac == null) {
            // No context instance was injected at construction time -> see if one
            // has been registered in the servlet context. If one exists, it is assumed
            // that the parent context (if any) has already been set and that the
            // user has performed any initialization such as setting the context id
            wac = findWebApplicationContext();
        }
        if (wac == null) {
            // No context instance is defined for this servlet -> create a local one
            wac = createWebApplicationContext(rootContext);
        }

        if (!this.refreshEventReceived) {
            // Either the context is not a ConfigurableApplicationContext with refresh
            // support or the context injected at construction time had already been
            // refreshed -> trigger initial onRefresh manually here.
            onRefresh(wac);
        }

        if (this.publishContext) {
            // Publish the context as a servlet context attribute.
            String attrName = getServletContextAttributeName();
            getServletContext().setAttribute(attrName, wac);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() + "' as ServletContext attribute with name [" + attrName + "]");
            }
        }

        return wac;
    }

    protected SimpleWebApplicationContext findWebApplicationContext() {
        String attrName = getContextAttribute();
        if (attrName == null) {
            return null;
        }
        SimpleWebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
        }
        return wac;
    }

    protected SimpleWebApplicationContext createWebApplicationContext(ApplicationContext parent) {
        Class<?> contextClass = getContextClass();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Servlet with name '" + getServletName() + "' will try to create custom WebApplicationContext context of class '" + contextClass.getName() + "'"
                + ", using parent context [" + parent + "]");
        }
        if (!SimpleConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
            throw new ApplicationContextException(
                "Fatal initialization error in servlet with name '" + getServletName() + "': custom WebApplicationContext class [" + contextClass.getName()
                    + "] is not of type ConfigurableWebApplicationContext");
        }
        SimpleConfigurableWebApplicationContext wac = (SimpleConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

        wac.setParent(parent);
        wac.setConfigLocation(getContextConfigLocation());

        configureAndRefreshWebApplicationContext(wac);

        return wac;
    }

    protected void configureAndRefreshWebApplicationContext(SimpleConfigurableWebApplicationContext wac) {
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            // The application context id is still set to its original default value
            // -> assign a more useful id based on available information
            if (this.contextId != null) {
                wac.setId(this.contextId);
            } else {
                // Generate default id...
                ServletContext sc = getServletContext();
                if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
                    // Servlet <= 2.4: resort to name specified in web.xml, if any.
                    String servletContextName = sc.getServletContextName();
                    if (servletContextName != null) {
                        wac.setId(SimpleConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName + "." + getServletName());
                    } else {
                        wac.setId(SimpleConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
                    }
                } else {
                    // Servlet 2.5's getContextPath available!
                    wac.setId(SimpleConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + ObjectUtils.getDisplayString(sc.getContextPath()) + "/" + getServletName());
                }
            }
        }

        wac.setServletContext(getServletContext());
        wac.setServletConfig(getServletConfig());
        wac.setNamespace(getNamespace());
        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        postProcessWebApplicationContext(wac);

        applyInitializers(wac);

        wac.refresh();
    }

    protected SimpleWebApplicationContext createWebApplicationContext(SimpleWebApplicationContext parent) {
        return createWebApplicationContext((ApplicationContext) parent);
    }

    @SuppressWarnings("unchecked")
    protected void applyInitializers(ConfigurableApplicationContext wac) {
        if (this.contextInitializerClasses != null) {
            String[] initializerClassNames = StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS);
            for (String initializerClassName : initializerClassNames) {
                ApplicationContextInitializer<ConfigurableApplicationContext> initializer;
                try {
                    Class<?> initializerClass = ClassUtils.forName(initializerClassName, wac.getClassLoader());
                    initializer = BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                        String.format("Could not instantiate class [%s] specified via " + "'contextInitializerClasses' init-param", initializerClassName), ex);
                }
                this.contextInitializers.add(initializer);
            }
        }
        Collections.sort(this.contextInitializers, new AnnotationAwareOrderComparator());
        for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
            initializer.initialize(wac);
        }
    }

    protected void postProcessWebApplicationContext(SimpleConfigurableWebApplicationContext wac) {
    }

    public String getServletContextAttributeName() {
        return SERVLET_CONTEXT_PREFIX + getServletName();
    }

    public final SimpleWebApplicationContext getWebApplicationContext() {
        return this.simpleWebApplicationContext;
    }

    protected void initFrameworkServlet() throws ServletException {
    }

    public void refresh() {
        SimpleWebApplicationContext wac = getWebApplicationContext();
        if (!(wac instanceof ConfigurableApplicationContext)) {
            throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
        }
        ((ConfigurableApplicationContext) wac).refresh();
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.refreshEventReceived = true;
        onRefresh(event.getApplicationContext());
    }

    protected void onRefresh(ApplicationContext context) {
        // For subclasses: do nothing by default.
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("start doGet in FrameworkServlet");
        processRequest(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (this.dispatchOptionsRequest) {
            processRequest(request, response);
            if (response.containsHeader("Allow")) {
                // Proper OPTIONS response coming from a handler - we're done.
                return;
            }
        }
        super.doOptions(request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (this.dispatchTraceRequest) {
            processRequest(request, response);
            if ("message/http".equals(response.getContentType())) {
                // Proper TRACE response coming from a handler - we're done.
                return;
            }
        }
        super.doTrace(request, response);
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Throwable failureCause = null;

        // Expose current LocaleResolver and request as LocaleContext.
        LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
        LocaleContextHolder.setLocaleContext(buildLocaleContext(request), this.threadContextInheritable);

        // Expose current RequestAttributes to current thread.
        RequestAttributes previousRequestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = null;
        if (previousRequestAttributes == null || previousRequestAttributes.getClass().equals(ServletRequestAttributes.class)) {
            requestAttributes = new ServletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
        }

        try {
            logger.debug("doService in FrameworkServlet for request : " + request);
            doService(request, response);
        } catch (ServletException ex) {
            failureCause = ex;
            throw ex;
        } catch (Throwable ex) {
            failureCause = ex;
            throw new NestedServletException("Request processing failed", ex);
        } finally {
            // Clear request attributes and reset thread-bound context.
            LocaleContextHolder.setLocaleContext(previousLocaleContext, this.threadContextInheritable);
            if (requestAttributes != null) {
                RequestContextHolder.setRequestAttributes(previousRequestAttributes, this.threadContextInheritable);
                requestAttributes.requestCompleted();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Cleared thread-bound request context: " + request);
            }

            if (logger.isDebugEnabled()) {
                if (failureCause != null) {
                    this.logger.debug("Could not complete request", failureCause);
                } else {
                    this.logger.debug("Successfully completed request");
                }
            }
        }
    }

    protected LocaleContext buildLocaleContext(HttpServletRequest request) {
        return new SimpleLocaleContext(request.getLocale());
    }

    protected String getUsernameForRequest(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        return (userPrincipal != null ? userPrincipal.getName() : null);
    }

    protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws Exception;

    @Override
    public void destroy() {
        getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
        if (this.simpleWebApplicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) this.simpleWebApplicationContext).close();
        }
    }

    private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

        public void onApplicationEvent(ContextRefreshedEvent event) {
            FrameworkServlet.this.onApplicationEvent(event);
        }
    }

}
