package com.example.simple.spring.web.mvc.context.request;

import com.example.simple.spring.web.mvc.context.request.RequestAttributes;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractRequestAttributes implements RequestAttributes {

    protected final Map<String, Runnable> requestDestructionCallbacks = new LinkedHashMap<>(8);

    private volatile boolean requestActive = true;

    public void requestCompleted() {
        executeRequestDestructionCallbacks();
        updateAccessedSessionAttributes();
        this.requestActive = false;
    }

    protected final boolean isRequestActive() {
        return this.requestActive;
    }

    protected final void registerRequestDestructionCallback(String name, Runnable callback) {
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(callback, "Callback must not be null");
        synchronized (this.requestDestructionCallbacks) {
            this.requestDestructionCallbacks.put(name, callback);
        }
    }

    protected final void removeRequestDestructionCallback(String name) {
        Assert.notNull(name, "Name must not be null");
        synchronized (this.requestDestructionCallbacks) {
            this.requestDestructionCallbacks.remove(name);
        }
    }

    private void executeRequestDestructionCallbacks() {
        synchronized (this.requestDestructionCallbacks) {
            for (Runnable runnable : this.requestDestructionCallbacks.values()) {
                runnable.run();
            }
            this.requestDestructionCallbacks.clear();
        }
    }

    protected abstract void updateAccessedSessionAttributes();

}
