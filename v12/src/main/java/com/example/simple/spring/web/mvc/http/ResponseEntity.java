package com.example.simple.spring.web.mvc.http;

import org.springframework.util.MultiValueMap;

public class ResponseEntity<T> extends HttpEntity<T> {

    private final HttpStatus statusCode;

    public ResponseEntity(HttpStatus statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public ResponseEntity(T body, HttpStatus statusCode) {
        super(body);
        this.statusCode = statusCode;
    }

    public ResponseEntity(MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(headers);
        this.statusCode = statusCode;
    }

    public ResponseEntity(T body, MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(body, headers);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        builder.append(statusCode.toString());
        builder.append(' ');
        builder.append(statusCode.getReasonPhrase());
        builder.append(',');
        T body = getBody();
        HttpHeaders headers = getHeaders();
        if (body != null) {
            builder.append(body);
            if (headers != null) {
                builder.append(',');
            }
        }
        if (headers != null) {
            builder.append(headers);
        }
        builder.append('>');
        return builder.toString();
    }

}
