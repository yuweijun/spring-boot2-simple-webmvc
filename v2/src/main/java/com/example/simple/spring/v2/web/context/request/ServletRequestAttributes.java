package com.example.simple.spring.v2.web.context.request;

import com.example.simple.spring.v2.web.util.WebUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class ServletRequestAttributes extends AbstractRequestAttributes {

    public static final String DESTRUCTION_CALLBACK_NAME_PREFIX =
        ServletRequestAttributes.class.getName() + ".DESTRUCTION_CALLBACK.";

    private final HttpServletRequest request;
    private final Map<String, Object> sessionAttributesToUpdate = new HashMap<String, Object>();
    private volatile HttpSession session;

    public ServletRequestAttributes(HttpServletRequest request) {
        Assert.notNull(request, "Request must not be null");
        this.request = request;
    }

    public final HttpServletRequest getRequest() {
        return this.request;
    }

    protected final HttpSession getSession(boolean allowCreate) {
        if (isRequestActive()) {
            return this.request.getSession(allowCreate);
        } else {
            // Access through stored session reference, if any...
            if (this.session == null && allowCreate) {
                throw new IllegalStateException(
                    "No session found and request already completed - cannot create new session!");
            }
            return this.session;
        }
    }

    public Object getAttribute(String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException(
                    "Cannot ask for request attribute - request is not active anymore!");
            }
            return this.request.getAttribute(name);
        } else {
            HttpSession session = getSession(false);
            if (session != null) {
                try {
                    Object value = session.getAttribute(name);
                    if (value != null) {
                        synchronized (this.sessionAttributesToUpdate) {
                            this.sessionAttributesToUpdate.put(name, value);
                        }
                    }
                    return value;
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
            return null;
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if (scope == SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException(
                    "Cannot set request attribute - request is not active anymore!");
            }
            this.request.setAttribute(name, value);
        } else {
            HttpSession session = getSession(true);
            synchronized (this.sessionAttributesToUpdate) {
                this.sessionAttributesToUpdate.remove(name);
            }
            session.setAttribute(name, value);
        }
    }

    public void removeAttribute(String name, int scope) {
        if (scope == SCOPE_REQUEST) {
            if (isRequestActive()) {
                this.request.removeAttribute(name);
                removeRequestDestructionCallback(name);
            }
        } else {
            HttpSession session = getSession(false);
            if (session != null) {
                synchronized (this.sessionAttributesToUpdate) {
                    this.sessionAttributesToUpdate.remove(name);
                }
                try {
                    session.removeAttribute(name);
                    // Remove any registered destruction callback as well.
                    session.removeAttribute(DESTRUCTION_CALLBACK_NAME_PREFIX + name);
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String[] getAttributeNames(int scope) {
        if (scope == SCOPE_REQUEST) {
            if (!isRequestActive()) {
                throw new IllegalStateException(
                    "Cannot ask for request attributes - request is not active anymore!");
            }
            return StringUtils.toStringArray(this.request.getAttributeNames());
        } else {
            HttpSession session = getSession(false);
            if (session != null) {
                try {
                    return StringUtils.toStringArray(session.getAttributeNames());
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
            return new String[0];
        }
    }

    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        if (scope == SCOPE_REQUEST) {
            registerRequestDestructionCallback(name, callback);
        } else {
            registerSessionDestructionCallback(name, callback);
        }
    }

    public Object resolveReference(String key) {
        if (REFERENCE_REQUEST.equals(key)) {
            return this.request;
        } else if (REFERENCE_SESSION.equals(key)) {
            return getSession(true);
        } else {
            return null;
        }
    }

    public String getSessionId() {
        return getSession(true).getId();
    }

    public Object getSessionMutex() {
        return WebUtils.getSessionMutex(getSession(true));
    }

    @Override
    protected void updateAccessedSessionAttributes() {
        // Store session reference for access after request completion.
        this.session = this.request.getSession(false);
        // Update all affected session attributes.
        synchronized (this.sessionAttributesToUpdate) {
            if (this.session != null) {
                try {
                    for (Map.Entry<String, Object> entry : this.sessionAttributesToUpdate.entrySet()) {
                        String name = entry.getKey();
                        Object newValue = entry.getValue();
                        Object oldValue = this.session.getAttribute(name);
                        if (oldValue == newValue) {
                            this.session.setAttribute(name, newValue);
                        }
                    }
                } catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
            this.sessionAttributesToUpdate.clear();
        }
    }

    protected void registerSessionDestructionCallback(String name, Runnable callback) {
        HttpSession session = getSession(true);
        // session.setAttribute(DESTRUCTION_CALLBACK_NAME_PREFIX + name, new DestructionCallbackBindingListener(callback));
    }

    @Override
    public String toString() {
        return this.request.toString();
    }

}
