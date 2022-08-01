package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import com.example.simple.spring.web.mvc.method.HandlerMethod;
import com.example.simple.spring.web.mvc.method.HandlerMethodSelector;
import com.example.simple.spring.web.mvc.servlet.HandlerMapping;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping {

    private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<>();
    private final MultiValueMap<String, T> urlMap = new LinkedMultiValueMap<>();
    private boolean detectHandlerMethodsInAncestorContexts = false;

    protected abstract boolean isHandler(Class<?> beanType);

    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    protected abstract Set<String> getMappingPathPatterns(T mapping);

    protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);

    public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
        this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
    }

    public Map<T, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(handlerMethods);
    }

    @Override
    public void initApplicationContext() throws ApplicationContextException {
        super.initApplicationContext();
        initHandlerMethods();
    }

    protected void initHandlerMethods() {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for request mappings in application context: " + getApplicationContext());
        }

        String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
            getApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {
            if (isHandler(getApplicationContext().getType(beanName))) {
                detectHandlerMethods(beanName);
            }
        }
        handlerMethodsInitialized(getHandlerMethods());
    }

    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
    }

    protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType = (handler instanceof String) ?
            getApplicationContext().getType((String) handler) : handler.getClass();

        final Class<?> userType = ClassUtils.getUserClass(handlerType);

        Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
            public boolean matches(Method method) {
                return getMappingForMethod(method, userType) != null;
            }
        });

        for (Method method : methods) {
            T mapping = getMappingForMethod(method, userType);
            registerHandlerMethod(handler, method, mapping);
        }
    }

    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        HandlerMethod handlerMethod;
        if (handler instanceof String) {
            String beanName = (String) handler;
            handlerMethod = new HandlerMethod(beanName, getApplicationContext(), method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }

        HandlerMethod oldHandlerMethod = handlerMethods.get(mapping);
        if (oldHandlerMethod != null && !oldHandlerMethod.equals(handlerMethod)) {
            throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + handlerMethod.getBean()
                + "' bean method \n" + handlerMethod + "\nto " + mapping + ": There is already '"
                + oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
        }

        handlerMethods.put(mapping, handlerMethod);
        logger.info("Mapped \"" + mapping + "\" onto " + handlerMethod);

        Set<String> patterns = getMappingPathPatterns(mapping);
        for (String pattern : patterns) {
            if (!getPathMatcher().isPattern(pattern)) {
                urlMap.add(pattern, mapping);
            }
        }
    }

    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        logger.debug("Looking up handler method for path " + lookupPath);

        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);

        if (handlerMethod != null) {
            logger.debug("Returning handler method [" + handlerMethod + "]");
        } else {
            logger.debug("Did not find handler method for [" + lookupPath + "]");
        }

        return (handlerMethod != null) ? handlerMethod.createWithResolvedBean() : null;
    }

    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        List<T> mappings = urlMap.get(lookupPath);
        if (mappings == null) {
            mappings = new ArrayList<>(handlerMethods.keySet());
        }

        List<Match> matches = new ArrayList<>();
        for (T mapping : mappings) {
            T match = getMatchingMapping(mapping, request);
            if (match != null) {
                matches.add(new Match(match, handlerMethods.get(mapping)));
            }
        }

        if (!matches.isEmpty()) {
            logger.trace("Found " + matches.size() + " matching mapping(s) for [" + lookupPath + "] : " + matches);
            Match bestMatch = matches.get(0);

            handleMatch(bestMatch.mapping, lookupPath, request);
            return bestMatch.handlerMethod;
        } else {
            return handleNoMatch(handlerMethods.keySet(), lookupPath, request);
        }
    }

    protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }

    protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
        throws Exception {
        return null;
    }

    private class Match {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        private Match(T mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return mapping.toString();
        }
    }

    private class MatchComparator implements Comparator<Match> {

        private final Comparator<T> comparator;

        public MatchComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        public int compare(Match match1, Match match2) {
            return comparator.compare(match1.mapping, match2.mapping);
        }
    }

}
