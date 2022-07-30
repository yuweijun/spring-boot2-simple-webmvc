package com.example.simple.spring.web.mvc.servlet.error;

import com.example.simple.spring.web.mvc.http.HttpStatus;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.util.Assert;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public abstract class AbstractErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public AbstractErrorController(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    protected Map<String, Object> getErrorAttributes(HttpServletRequest request, ErrorAttributeOptions options) {
        return this.errorAttributes.getErrorAttributes(request, options);
    }

    protected boolean getTraceParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "trace");
    }

    protected boolean getMessageParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "message");
    }

    protected boolean getErrorsParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "errors");
    }

    protected boolean getBooleanParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            return false;
        }
        return !"false".equalsIgnoreCase(parameter);
    }

    protected HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
 
}
