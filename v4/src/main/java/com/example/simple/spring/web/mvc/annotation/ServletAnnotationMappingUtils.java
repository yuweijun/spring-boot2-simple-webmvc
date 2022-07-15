package com.example.simple.spring.web.mvc.annotation;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMethod;
import com.example.simple.spring.web.mvc.util.WebUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;

public abstract class ServletAnnotationMappingUtils {

    public static boolean checkRequestMethod(RequestMethod[] methods, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(methods)) {
            return true;
        }
        for (RequestMethod method : methods) {
            if (method.name().equals(request.getMethod())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkParameters(String[] params, HttpServletRequest request) {
        if (!ObjectUtils.isEmpty(params)) {
            for (String param : params) {
                int separator = param.indexOf('=');
                if (separator == -1) {
                    if (param.startsWith("!")) {
                        if (WebUtils.hasSubmitParameter(request, param.substring(1))) {
                            return false;
                        }
                    } else if (!WebUtils.hasSubmitParameter(request, param)) {
                        return false;
                    }
                } else {
                    boolean negated = separator > 0 && param.charAt(separator - 1) == '!';
                    String key = !negated ? param.substring(0, separator) : param.substring(0, separator - 1);
                    String value = param.substring(separator + 1);
                    boolean match = value.equals(request.getParameter(key));
                    if (negated) {
                        match = !match;
                    }
                    if (!match) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkHeaders(String[] headers, HttpServletRequest request) {
        return true;
    }

    private static boolean isMediaTypeHeader(String headerName) {
        return "Accept".equalsIgnoreCase(headerName) || "Content-Type".equalsIgnoreCase(headerName);
    }

}
