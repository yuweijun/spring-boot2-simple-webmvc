package com.example.simple.spring.web.mvc.contex.request;

public interface RequestAttributes {

    int SCOPE_REQUEST = 0;

    int SCOPE_SESSION = 1;

    int SCOPE_GLOBAL_SESSION = 2;

    String REFERENCE_REQUEST = "request";

    String REFERENCE_SESSION = "session";

    Object getAttribute(String name, int scope);

    void setAttribute(String name, Object value, int scope);

    void removeAttribute(String name, int scope);

    String[] getAttributeNames(int scope);

    void registerDestructionCallback(String name, Runnable callback, int scope);

    Object resolveReference(String key);

    String getSessionId();

    Object getSessionMutex();

}
