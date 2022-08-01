package com.example.simple.spring.web.mvc.context.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServletContextScope implements Scope, DisposableBean {

    private final ServletContext servletContext;

    private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();

    public ServletContextScope(ServletContext servletContext) {
        Assert.notNull(servletContext, "ServletContext must not be null");
        this.servletContext = servletContext;
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object scopedObject = this.servletContext.getAttribute(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            this.servletContext.setAttribute(name, scopedObject);
        }
        return scopedObject;
    }

    @Override
    public Object remove(String name) {
        Object scopedObject = this.servletContext.getAttribute(name);
        if (scopedObject != null) {
            synchronized (this.destructionCallbacks) {
                this.destructionCallbacks.remove(name);
            }
            this.servletContext.removeAttribute(name);
            return scopedObject;
        } else {
            return null;
        }
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        synchronized (this.destructionCallbacks) {
            this.destructionCallbacks.put(name, callback);
        }
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }

    @Override
    public void destroy() {
        synchronized (this.destructionCallbacks) {
            for (Runnable runnable : this.destructionCallbacks.values()) {
                runnable.run();
            }
            this.destructionCallbacks.clear();
        }
    }

}
