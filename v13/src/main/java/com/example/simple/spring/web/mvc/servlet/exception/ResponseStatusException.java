package com.example.simple.spring.web.mvc.servlet.exception;

import com.example.simple.spring.web.mvc.http.HttpHeaders;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

public class ResponseStatusException extends NestedRuntimeException {

    private final HttpStatus status;

    private final String reason;

    public ResponseStatusException(HttpStatus status) {
        this(status, null, null);
    }

    public ResponseStatusException(HttpStatus status, String reason) {
        this(status, reason, null);
    }

    public ResponseStatusException(HttpStatus status, String reason, Throwable cause) {
        super(null, cause);
        Assert.notNull(status, "HttpStatus is required");
        this.status = status;
        this.reason = reason;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    protected Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

    public HttpHeaders getResponseHeaders() {
        Map<String, String> headers = getHeaders();
        if (headers.isEmpty()) {
            return new HttpHeaders();
        }
        HttpHeaders result = new HttpHeaders();
        getHeaders().forEach(result::add);
        return result;
    }

    public String getReason() {
        return this.reason;
    }

    @Override
    public String getMessage() {
        String msg = this.status + (this.reason != null ? " \"" + this.reason + "\"" : "");
        return NestedExceptionUtils.buildMessage(msg, getCause());
    }

}
