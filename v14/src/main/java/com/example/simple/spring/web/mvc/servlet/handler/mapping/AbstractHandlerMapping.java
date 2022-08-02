package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.contex.support.WebApplicationObjectSupport;
import com.example.simple.spring.web.mvc.servlet.HandlerExecutionChain;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.intercerptor.MappedInterceptor;
import com.example.simple.spring.web.util.UrlPathHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {

    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    private final List<MappedInterceptor> mappedInterceptors = new ArrayList<>();
    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered
    private Object defaultHandler;
    private UrlPathHelper urlPathHelper = new UrlPathHelper();
    private PathMatcher pathMatcher = new AntPathMatcher();

    protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    public final int getOrder() {
        return this.order;
    }

    public final void setOrder(int order) {
        this.order = order;
    }

    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
    }

    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    public UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected void initApplicationContext() throws BeansException {
        detectMappedInterceptors();
        initInterceptors();
    }

    protected void detectMappedInterceptors() {
        final Map<String, MappedInterceptor> map = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MappedInterceptor.class, true, false);
        logger.debug("detectMappedInterceptors : " + map);

        mappedInterceptors.addAll(map.values());
    }

    protected void initInterceptors() {
    }

    protected final HandlerInterceptor[] getInterceptors() {
        int count = interceptors.size();
        return (count > 0) ? interceptors.toArray(new HandlerInterceptor[count]) : null;
    }

    public void setInterceptors(HandlerInterceptor[] interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }

        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }

        return getHandlerExecutionChain(handler, request);
    }

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        HandlerExecutionChain chain = handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler : new HandlerExecutionChain(handler);
        chain.addInterceptors(getInterceptors());

        String lookupPath = urlPathHelper.getLookupPathForRequest(request);
        for (MappedInterceptor mappedInterceptor : mappedInterceptors) {
            if (mappedInterceptor.matches(lookupPath, pathMatcher)) {
                chain.addInterceptor(mappedInterceptor.getInterceptor());
            }
        }

        return chain;
    }

}
