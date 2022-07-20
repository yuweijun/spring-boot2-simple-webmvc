package com.example.simple.spring.web.mvc.http;

import java.io.IOException;
import java.io.InputStream;

public interface HttpInputMessage extends HttpMessage {

    InputStream getBody() throws IOException;

}
