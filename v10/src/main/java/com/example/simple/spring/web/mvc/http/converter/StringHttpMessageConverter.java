package com.example.simple.spring.web.mvc.http.converter;

import com.example.simple.spring.web.mvc.http.HttpInputMessage;
import com.example.simple.spring.web.mvc.http.HttpOutputMessage;
import com.example.simple.spring.web.mvc.http.MediaType;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

    private final List<Charset> availableCharsets;

    private boolean writeAcceptCharset = true;

    public StringHttpMessageConverter() {
        super(new MediaType("text", "plain", DEFAULT_CHARSET), MediaType.ALL);
        this.availableCharsets = new ArrayList<>(Charset.availableCharsets().values());
    }

    public void setWriteAcceptCharset(boolean writeAcceptCharset) {
        this.writeAcceptCharset = writeAcceptCharset;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    protected String readInternal(Class clazz, HttpInputMessage inputMessage) throws IOException {
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
        return FileCopyUtils.copyToString(new InputStreamReader(inputMessage.getBody(), charset));
    }

    @Override
    protected Long getContentLength(String s, MediaType contentType) {
        Charset charset = getContentTypeCharset(contentType);
        try {
            return (long) s.getBytes(charset.name()).length;
        } catch (UnsupportedEncodingException ex) {
            // should not occur
            throw new InternalError(ex.getMessage());
        }
    }

    @Override
    protected void writeInternal(String s, HttpOutputMessage outputMessage) throws IOException {
        if (writeAcceptCharset) {
            outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
        }
        Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());
        FileCopyUtils.copy(s, new OutputStreamWriter(outputMessage.getBody(), charset));
    }

    protected List<Charset> getAcceptedCharsets() {
        return this.availableCharsets;
    }

    private Charset getContentTypeCharset(MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        } else {
            return DEFAULT_CHARSET;
        }
    }

}
