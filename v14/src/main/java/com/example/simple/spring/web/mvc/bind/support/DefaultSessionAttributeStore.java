package com.example.simple.spring.web.mvc.bind.support;

import com.example.simple.spring.web.mvc.context.request.RequestAttributes;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.example.simple.spring.web.mvc.context.request.RequestAttributes.SCOPE_REQUEST;

public class DefaultSessionAttributeStore implements SessionAttributeStore {

    private String attributeNamePrefix = "";

    public void setAttributeNamePrefix(String attributeNamePrefix) {
        this.attributeNamePrefix = (attributeNamePrefix != null ? attributeNamePrefix : "");
    }

    @Override
    public void storeAttribute(HttpServletRequest request, String attributeName, Object attributeValue) {
        Assert.notNull(request, "HttpServletRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        Assert.notNull(attributeValue, "Attribute value must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        setAttribute(request, storeAttributeName, attributeValue, RequestAttributes.SCOPE_SESSION);
    }

    public void setAttribute(HttpServletRequest request, String name, Object value, int scope) {
        if (scope == SCOPE_REQUEST) {
            request.setAttribute(name, value);
        } else {
            HttpSession session = obtainSession(request);
            session.setAttribute(name, value);
        }
    }

    private HttpSession obtainSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Assert.state(session != null, "No HttpSession");
        return session;
    }

    public Object getAttribute(HttpServletRequest request, String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            return request.getAttribute(name);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                try {
                    Object value = session.getAttribute(name);
                    return value;
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
            return null;
        }
    }

    public void removeAttribute(HttpServletRequest request, String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            request.removeAttribute(name);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                try {
                    session.removeAttribute(name);
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
        }
    }

    @Override
    public Object retrieveAttribute(HttpServletRequest request, String attributeName) {
        Assert.notNull(request, " HttpServletRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        return getAttribute(request, storeAttributeName, RequestAttributes.SCOPE_SESSION);
    }

    @Override
    public void cleanupAttribute(HttpServletRequest request, String attributeName) {
        Assert.notNull(request, " HttpServletRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        removeAttribute(request, storeAttributeName, RequestAttributes.SCOPE_SESSION);
    }

    protected String getAttributeNameInSession(HttpServletRequest request, String attributeName) {
        return this.attributeNamePrefix + attributeName;
    }

}
