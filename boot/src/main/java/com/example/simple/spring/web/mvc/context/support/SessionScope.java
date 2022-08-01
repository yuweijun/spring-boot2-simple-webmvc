package com.example.simple.spring.web.mvc.context.support;

import com.example.simple.spring.web.mvc.context.request.RequestAttributes;
import com.example.simple.spring.web.mvc.context.request.RequestContextHolder;
import org.springframework.beans.factory.ObjectFactory;

public class SessionScope extends AbstractRequestAttributesScope {

    @Override
    protected int getScope() {
        return RequestAttributes.SCOPE_SESSION;
    }

    @Override
    public String getConversationId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();
        synchronized (mutex) {
            return super.get(name, objectFactory);
        }
    }

    @Override
    public Object remove(String name) {
        Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();
        synchronized (mutex) {
            return super.remove(name);
        }
    }

}
