package com.example.simple.spring.web.mvc.servlet.view;

import com.example.simple.spring.web.mvc.contex.support.WebApplicationObjectSupport;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

    private final Map<Object, View> viewCache = new HashMap<>();
    private boolean cache = true;
    private boolean cacheUnresolved = true;

    public boolean isCache() {
        return this.cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public boolean isCacheUnresolved() {
        return this.cacheUnresolved;
    }

    public void setCacheUnresolved(boolean cacheUnresolved) {
        this.cacheUnresolved = cacheUnresolved;
    }

    @Override
    public View resolveViewName(String viewName) throws Exception {
        if (!isCache()) {
            return createView(viewName);
        } else {
            Object cacheKey = getCacheKey(viewName);
            synchronized (this.viewCache) {
                View view = this.viewCache.get(cacheKey);
                if (view == null && (!this.cacheUnresolved || !this.viewCache.containsKey(cacheKey))) {
                    // Ask the subclass to create the View object.
                    view = createView(viewName);
                    if (view != null || this.cacheUnresolved) {
                        this.viewCache.put(cacheKey, view);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Cached view [" + cacheKey + "]");
                        }
                    }
                }
                return view;
            }
        }
    }

    protected Object getCacheKey(String viewName) {
        return viewName;
    }

    public void removeFromCache(String viewName) {
        if (!this.cache) {
            logger.warn("View caching is SWITCHED OFF -- removal not necessary");
        } else {
            Object cacheKey = getCacheKey(viewName);
            Object cachedView;
            synchronized (this.viewCache) {
                cachedView = this.viewCache.remove(cacheKey);
            }
            if (cachedView == null) {
                // Some debug output might be useful...
                if (logger.isDebugEnabled()) {
                    logger.debug("No cached instance for view '" + cacheKey + "' was found");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cache for view " + cacheKey + " has been cleared");
                }
            }
        }
    }

    public void clearCache() {
        logger.debug("Clearing entire view cache");
        synchronized (this.viewCache) {
            this.viewCache.clear();
        }
    }

    protected View createView(String viewName) throws Exception {
        return loadView(viewName);
    }

    protected abstract View loadView(String viewName) throws Exception;

}
