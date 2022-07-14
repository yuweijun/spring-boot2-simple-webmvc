package com.example.simple.spring.web.mvc.context;

import com.example.simple.spring.web.mvc.context.support.WebApplicationContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.context.MissingWebServerFactoryBeanException;
import org.springframework.boot.web.context.WebServerGracefulShutdownLifecycle;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SimpleServletWebServerApplicationContext extends SimpleGenericWebApplicationContext implements ConfigurableWebServerApplicationContext {

    public static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";
    private static final Log logger = LogFactory.getLog(SimpleServletWebServerApplicationContext.class);
    private volatile WebServer webServer;

    private ServletConfig servletConfig;

    private String serverNamespace;

    public SimpleServletWebServerApplicationContext() {
    }

    public SimpleServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
        registerWebApplicationScopes();
    }

    @Override
    public final void refresh() throws BeansException, IllegalStateException {
        try {
            super.refresh();
        } catch (RuntimeException ex) {
            WebServer webServer = this.webServer;
            if (webServer != null) {
                webServer.stop();
            }
            throw ex;
        }
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        try {
            createWebServer();
        } catch (Throwable ex) {
            throw new ApplicationContextException("Unable to start web server", ex);
        }
    }

    @Override
    protected void doClose() {
        if (isActive()) {
            AvailabilityChangeEvent.publish(this, ReadinessState.REFUSING_TRAFFIC);
        }
        super.doClose();
    }

    private void createWebServer() {
        logger.debug("start create web server");
        WebServer webServer = this.webServer;
        ServletContext servletContext = getServletContext();
        if (webServer == null && servletContext == null) {
            StartupStep createWebServer = this.getApplicationStartup().start("spring.boot.webserver.create");
            ServletWebServerFactory factory = getWebServerFactory();
            createWebServer.tag("factory", factory.getClass().toString());
            this.webServer = factory.getWebServer(getSelfInitializer());
            createWebServer.end();
            getBeanFactory().registerSingleton("webServerGracefulShutdown", new WebServerGracefulShutdownLifecycle(this.webServer));
            getBeanFactory().registerSingleton("webServerStartStop", new WebServerStartStopLifecycle(this, this.webServer));
        } else if (servletContext != null) {
            try {
                getSelfInitializer().onStartup(servletContext);
            } catch (ServletException ex) {
                throw new ApplicationContextException("Cannot initialize servlet context", ex);
            }
        }
        initPropertySources();
    }

    protected ServletWebServerFactory getWebServerFactory() {
        // Use bean names so that we don't consider the hierarchy
        String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
        if (beanNames.length == 0) {
            throw new MissingWebServerFactoryBeanException(getClass(), ServletWebServerFactory.class, WebApplicationType.SERVLET);
        }
        if (beanNames.length > 1) {
            throw new ApplicationContextException(
                "Unable to start ServletWebServerApplicationContext due to multiple " + "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
        }
        return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
    }

    private ServletContextInitializer getSelfInitializer() {
        return this::selfInitialize;
    }

    private void selfInitialize(ServletContext servletContext) throws ServletException {
        prepareWebApplicationContext(servletContext);
        registerApplicationScope(servletContext);
        WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
        for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
            beans.onStartup(servletContext);
        }
    }

    private void registerApplicationScope(ServletContext servletContext) {
        // ServletContextScope appScope = new ServletContextScope(servletContext);
        // getBeanFactory().registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
        // Register as ServletContext attribute, for ContextCleanupListener to detect it.
        // servletContext.setAttribute(ServletContextScope.class.getName(), appScope);
    }

    private void registerWebApplicationScopes() {
        ExistingWebApplicationScopes existingScopes = new ExistingWebApplicationScopes(getBeanFactory());
        // WebApplicationContextUtils.registerWebApplicationScopes(getBeanFactory());
        existingScopes.restore();
    }

    protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
        return new ServletContextInitializerBeans(getBeanFactory());
    }

    protected void prepareWebApplicationContext(ServletContext servletContext) {
        Object rootContext = servletContext.getAttribute(SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (rootContext != null) {
            if (rootContext == this) {
                throw new IllegalStateException(
                    "Cannot initialize context because there is already a root application context present - " + "check whether you have multiple ServletContextInitializers!");
            }
            return;
        }
        servletContext.log("Initializing Spring embedded WebApplicationContext");
        try {
            servletContext.setAttribute(SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Published root WebApplicationContext as ServletContext attribute with name [" + SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }
            setServletContext(servletContext);
            if (logger.isInfoEnabled()) {
                long elapsedTime = System.currentTimeMillis() - getStartupDate();
                logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
            }
        } catch (RuntimeException | Error ex) {
            logger.error("Context initialization failed", ex);
            servletContext.setAttribute(SimpleWebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        }
    }

    @Override
    protected Resource getResourceByPath(String path) {
        return new ClassPathContextResource(path, getClassLoader());
    }

    @Override
    public String getServerNamespace() {
        return this.serverNamespace;
    }

    @Override
    public void setServerNamespace(String serverNamespace) {
        this.serverNamespace = serverNamespace;
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public WebServer getWebServer() {
        return this.webServer;
    }

    public static class ExistingWebApplicationScopes {

        private static final Set<String> SCOPES;

        static {
            Set<String> scopes = new LinkedHashSet<>();
            scopes.add(SimpleWebApplicationContext.SCOPE_REQUEST);
            scopes.add(SimpleWebApplicationContext.SCOPE_SESSION);
            SCOPES = Collections.unmodifiableSet(scopes);
        }

        private final ConfigurableListableBeanFactory beanFactory;

        private final Map<String, Scope> scopes = new HashMap<>();

        public ExistingWebApplicationScopes(ConfigurableListableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
            for (String scopeName : SCOPES) {
                Scope scope = beanFactory.getRegisteredScope(scopeName);
                if (scope != null) {
                    this.scopes.put(scopeName, scope);
                }
            }
        }

        public void restore() {
            this.scopes.forEach((key, value) -> {
                if (logger.isInfoEnabled()) {
                    logger.info("Restoring user defined scope " + key);
                }
                this.beanFactory.registerScope(key, value);
            });
        }

    }

}
