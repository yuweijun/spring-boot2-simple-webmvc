package com.example.simple.spring.web.mvc.http;

import org.springframework.core.NestedRuntimeException;
 
public class HttpMediaTypeNotSupportedException extends NestedRuntimeException {

	private final MediaType contentType;
 
	public HttpMediaTypeNotSupportedException(String message) {
		super(message);
		this.contentType = null;
	}
 
	public HttpMediaTypeNotSupportedException(MediaType contentType) {
		this(contentType, "Content type '" + contentType + "' not supported");
	}
 
	public HttpMediaTypeNotSupportedException(MediaType contentType, String msg) {
		super(msg);
		this.contentType = contentType;
	}
 
	public MediaType getContentType() {
		return contentType;
	}

}
