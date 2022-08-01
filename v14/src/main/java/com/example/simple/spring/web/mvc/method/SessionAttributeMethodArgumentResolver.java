package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ValueConstants;
import com.example.simple.spring.web.mvc.bind.annotation.SessionAttribute;
import com.example.simple.spring.web.mvc.context.request.RequestAttributes;
import com.example.simple.spring.web.mvc.contex.request.ServletRequestAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionAttributeMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SessionAttribute.class);
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        SessionAttribute ann = parameter.getParameterAnnotation(SessionAttribute.class);
        Assert.state(ann != null, "No SessionAttribute annotation");
        return new NamedValueInfo(ann.value(), ann.required(), ValueConstants.DEFAULT_NONE);
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ServletRequestAttributes(request).getAttribute(name, RequestAttributes.SCOPE_SESSION);
    }

    @Override
    protected void handleMissingValue(String name, MethodParameter parameter) throws ServletException {
        // throw new ServletRequestBindingException("Missing session attribute '" + name + "' of type " + parameter.getNestedParameterType().getSimpleName());
    }

}
