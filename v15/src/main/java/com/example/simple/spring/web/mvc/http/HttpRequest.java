package com.example.simple.spring.web.mvc.http;

import java.net.URI;

public interface HttpRequest extends HttpMessage {

    HttpMethod getMethod();

    URI getURI();

}
