

package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import org.springframework.core.MethodParameter;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Locale;

public class ServletRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return ServletRequest.class.isAssignableFrom(paramType) || HttpSession.class.isAssignableFrom(paramType) || Principal.class.isAssignableFrom(paramType)
            || Locale.class.equals(paramType) || InputStream.class.isAssignableFrom(paramType) || Reader.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder)
        throws Exception {
        Class<?> paramType = parameter.getParameterType();
        if (HttpServletRequest.class.isAssignableFrom(paramType)) {
            return request;
        }

        if (HttpSession.class.isAssignableFrom(paramType)) {
            return request.getSession();
        } else if (Principal.class.isAssignableFrom(paramType)) {
            return request.getUserPrincipal();
        } else if (InputStream.class.isAssignableFrom(paramType)) {
            return request.getInputStream();
        } else if (Reader.class.isAssignableFrom(paramType)) {
            return request.getReader();
        } else {
            // should never happen..
            Method method = parameter.getMethod();
            throw new UnsupportedOperationException("Unknown parameter type: " + paramType + " in method: " + method);
        }
    }

}