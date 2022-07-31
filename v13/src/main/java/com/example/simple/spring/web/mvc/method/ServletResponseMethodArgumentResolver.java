package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import org.springframework.core.MethodParameter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;

public class ServletResponseMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return ServletResponse.class.isAssignableFrom(paramType) ||
            OutputStream.class.isAssignableFrom(paramType) ||
            Writer.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder)
        throws Exception {
        Class<?> paramType = parameter.getParameterType();

        if (ServletResponse.class.isAssignableFrom(paramType)) {
            return response;
        } else if (OutputStream.class.isAssignableFrom(paramType)) {
            return response.getOutputStream();
        } else if (Writer.class.isAssignableFrom(paramType)) {
            return response.getWriter();
        } else {
            // should not happen
            Method method = parameter.getMethod();
            throw new UnsupportedOperationException("Unknown parameter type: " + paramType + " in method: " + method);
        }
    }

}
