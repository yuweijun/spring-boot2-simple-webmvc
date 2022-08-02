package com.example.simple.spring.web.mvc.servlet.error;

import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.servlet.exception.HandlerExceptionResolver;
import com.example.simple.spring.web.mvc.servlet.exception.ResponseStatusException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

// org.springframework.boot.web.servlet.error.DefaultErrorAttributes
public class DefaultErrorAttributes implements ErrorAttributes, HandlerExceptionResolver {

    private static final String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    public static Throwable getErrorFromRequest(HttpServletRequest request) {
        return (Throwable) request.getAttribute(ERROR_INTERNAL_ATTRIBUTE);
    }

    public static void storeErrorInRequest(HttpServletRequest request, Throwable error) {
        request.setAttribute(ERROR_INTERNAL_ATTRIBUTE, error);
    }

    @Override
    public Map<String, Object> getErrorAttributes(HttpServletRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, options.isIncluded(Include.STACK_TRACE));
        if (!options.isIncluded(Include.EXCEPTION)) {
            errorAttributes.remove("exception");
        }
        if (!options.isIncluded(Include.STACK_TRACE)) {
            errorAttributes.remove("trace");
        }
        if (!options.isIncluded(Include.MESSAGE) && errorAttributes.get("message") != null) {
            errorAttributes.remove("message");
        }
        if (!options.isIncluded(Include.BINDING_ERRORS)) {
            errorAttributes.remove("errors");
        }
        return errorAttributes;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", new Date());
        errorAttributes.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        errorAttributes.put("requestId", ObjectUtils.getIdentityHexString(request));
        Throwable error = getError(request);
        if (error != null) {
            MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
            HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
            errorAttributes.put("status", errorStatus.code());
            errorAttributes.put("error", errorStatus.getReasonPhrase());
            errorAttributes.put("message", determineMessage(error, responseStatusAnnotation));
            handleException(errorAttributes, determineException(error), includeStackTrace);
        }
        return errorAttributes;
    }

    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String determineMessage(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof BindingResult) {
            return error.getMessage();
        }
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getReason();
        }
        String reason = responseStatusAnnotation.getValue("reason", String.class).orElse("");
        if (StringUtils.hasText(reason)) {
            return reason;
        }
        return (error.getMessage() != null) ? error.getMessage() : "";
    }

    private Throwable determineException(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return (error.getCause() != null) ? error.getCause() : error;
        }
        return error;
    }

    private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        errorAttributes.put("trace", stackTrace.toString());
    }

    private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
        errorAttributes.put("exception", error.getClass().getName());
        if (includeStackTrace) {
            addStackTrace(errorAttributes, error);
        }
        if (error instanceof BindingResult) {
            BindingResult result = (BindingResult) error;
            if (result.hasErrors()) {
                errorAttributes.put("errors", result.getAllErrors());
            }
        }
    }

    @Override
    public Throwable getError(HttpServletRequest request) {
        return getErrorFromRequest(request);
    }

    @Override
    public boolean resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        storeErrorInRequest(request, ex);
        return false;
    }

    private void storeErrorInformation(HttpServletRequest request, Throwable error) {
        storeErrorInRequest(request, error);
    }

}
