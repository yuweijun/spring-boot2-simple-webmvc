package com.example.simple.spring.web.mvc.context.request;

import com.example.simple.spring.web.mvc.contex.request.RequestAttributes;
import com.example.simple.spring.web.mvc.contex.request.RequestContextHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public abstract class AbstractRequestAttributesScope implements Scope {

    public Object get(String name, ObjectFactory objectFactory) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object scopedObject = attributes.getAttribute(name, getScope());
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            attributes.setAttribute(name, scopedObject, getScope());
        }
        return scopedObject;
    }

    public Object remove(String name) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object scopedObject = attributes.getAttribute(name, getScope());
        if (scopedObject != null) {
            attributes.removeAttribute(name, getScope());
            return scopedObject;
        } else {
            return null;
        }
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        attributes.registerDestructionCallback(name, callback, getScope());
    }

    public Object resolveContextualObject(String key) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        return attributes.resolveReference(key);
    }

    protected abstract int getScope();

}
