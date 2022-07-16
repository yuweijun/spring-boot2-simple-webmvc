package com.example.simple.spring.web.mvc.http;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpOutputMessage extends HttpMessage {

    OutputStream getBody() throws IOException;

}
