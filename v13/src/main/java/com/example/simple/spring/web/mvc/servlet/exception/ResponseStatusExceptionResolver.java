package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver implements MessageSourceAware {

    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected boolean doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (ex instanceof ResponseStatusException) {
                logger.debug("resolve ResponseStatusException in class : " + getClass().getName());
                resolveResponseStatusException((ResponseStatusException) ex, request, response, handler);
                return true;
            }

            ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
            if (status != null) {
                resolveResponseStatus(status, request, response, handler, ex);
            }
        } catch (Exception resolveEx) {
            logger.warn("Failure while trying to resolve exception [" + ex.getClass().getName() + "]", resolveEx);
        }

        return false;
    }

    protected void resolveResponseStatus(ResponseStatus responseStatus, HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        int statusCode = responseStatus.value().code();
        String reason = responseStatus.reason();
        applyStatusAndReason(statusCode, reason, response);
    }

    protected void resolveResponseStatusException(ResponseStatusException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ex.getResponseHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));

        int statusCode = ex.getStatus().code();
        String reason = ex.getReason();
        applyStatusAndReason(statusCode, reason, response);
    }

    protected void applyStatusAndReason(int statusCode, String reason, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasLength(reason)) {
            response.sendError(statusCode);
        } else {
            String resolvedReason = (this.messageSource != null ? this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale()) : reason);
            response.sendError(statusCode, resolvedReason);
        }
    }

}
