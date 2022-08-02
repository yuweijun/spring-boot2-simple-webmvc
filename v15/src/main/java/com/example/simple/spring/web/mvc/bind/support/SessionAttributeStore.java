package com.example.simple.spring.web.mvc.bind.support;

import javax.servlet.http.HttpServletRequest;

public interface SessionAttributeStore {

    void storeAttribute(HttpServletRequest request, String attributeName, Object attributeValue);

    Object retrieveAttribute(HttpServletRequest request, String attributeName);

    void cleanupAttribute(HttpServletRequest request, String attributeName);

}
