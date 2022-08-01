package com.example.simple.spring.web.mvc.context.support;

import com.example.simple.spring.web.mvc.context.request.RequestAttributes;

public class RequestScope extends AbstractRequestAttributesScope {

    @Override
    protected int getScope() {
        return RequestAttributes.SCOPE_REQUEST;
    }

    public String getConversationId() {
        return null;
    }

}
