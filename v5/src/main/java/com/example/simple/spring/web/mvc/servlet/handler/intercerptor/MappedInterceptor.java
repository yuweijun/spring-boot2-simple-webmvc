package com.example.simple.spring.web.mvc.servlet.handler.intercerptor;

import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.springframework.util.PathMatcher;

public final class MappedInterceptor {

    private final String[] pathPatterns;

    private final HandlerInterceptor interceptor;

    public MappedInterceptor(String[] pathPatterns, HandlerInterceptor interceptor) {
        this.pathPatterns = pathPatterns;
        this.interceptor = interceptor;
    }

    public String[] getPathPatterns() {
        return this.pathPatterns;
    }

    public HandlerInterceptor getInterceptor() {
        return this.interceptor;
    }

    public boolean matches(String lookupPath, PathMatcher pathMatcher) {
        if (pathPatterns == null) {
            return true;
        } else {
            for (String pathPattern : pathPatterns) {
                if (pathMatcher.match(pathPattern, lookupPath)) {
                    return true;
                }
            }
            return false;
        }
    }
}
