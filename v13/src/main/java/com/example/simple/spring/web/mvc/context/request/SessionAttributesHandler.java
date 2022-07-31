package com.example.simple.spring.web.mvc.context.request;

import com.example.simple.spring.web.mvc.bind.annotation.SessionAttributes;
import com.example.simple.spring.web.mvc.bind.support.SessionAttributeStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionAttributesHandler {

    private final Log logger = LogFactory.getLog(getClass());

    private final Set<String> attributeNames = new HashSet<>();

    private final Set<Class<?>> attributeTypes = new HashSet<>();

    private final Set<String> knownAttributeNames = Collections.newSetFromMap(new ConcurrentHashMap<>(4));

    private final SessionAttributeStore sessionAttributeStore;

    public SessionAttributesHandler(Class<?> handlerType, SessionAttributeStore sessionAttributeStore) {
        Assert.notNull(sessionAttributeStore, "SessionAttributeStore may not be null");
        this.sessionAttributeStore = sessionAttributeStore;

        SessionAttributes ann = AnnotatedElementUtils.findMergedAnnotation(handlerType, SessionAttributes.class);
        if (ann != null) {
            Collections.addAll(this.attributeNames, ann.names());
            Collections.addAll(this.attributeTypes, ann.types());
            logger.debug(handlerType.getSimpleName() + " has @SessionAttributes names " + this.attributeNames);
        }
        this.knownAttributeNames.addAll(this.attributeNames);
    }

    public boolean hasSessionAttributes() {
        return (!this.attributeNames.isEmpty() || !this.attributeTypes.isEmpty());
    }

    public boolean isHandlerSessionAttribute(String attributeName, Class<?> attributeType) {
        Assert.notNull(attributeName, "Attribute name must not be null");
        if (this.attributeNames.contains(attributeName) || this.attributeTypes.contains(attributeType)) {
            this.knownAttributeNames.add(attributeName);
            return true;
        } else {
            return false;
        }
    }

    public void storeAttributes(HttpServletRequest request, Map<String, ?> attributes) {
        logger.debug("store session attributes : " + attributes);
        attributes.forEach((name, value) -> {
            if (value != null && isHandlerSessionAttribute(name, value.getClass())) {
                this.sessionAttributeStore.storeAttribute(request, name, value);
            }
        });
    }

    public Map<String, Object> retrieveAttributes(HttpServletRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        for (String name : this.knownAttributeNames) {
            Object value = this.sessionAttributeStore.retrieveAttribute(request, name);
            if (value != null) {
                attributes.put(name, value);
            }
        }
        return attributes;
    }

    public void cleanupAttributes(HttpServletRequest request) {
        logger.debug("clean session attributes : " + this.knownAttributeNames);
        for (String attributeName : this.knownAttributeNames) {
            this.sessionAttributeStore.cleanupAttribute(request, attributeName);
        }
    }

    public Object retrieveAttribute(HttpServletRequest request, String attributeName) {
        return this.sessionAttributeStore.retrieveAttribute(request, attributeName);
    }

}
