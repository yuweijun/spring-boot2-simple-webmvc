package com.example.simple.spring.web.mvc.context.request;

import com.example.simple.spring.web.mvc.contex.request.RequestAttributes;

public class RequestScope extends AbstractRequestAttributesScope {

    @Override
    protected int getScope() {
        return RequestAttributes.SCOPE_REQUEST;
    }

    public String getConversationId() {
        return null;
    }

}
