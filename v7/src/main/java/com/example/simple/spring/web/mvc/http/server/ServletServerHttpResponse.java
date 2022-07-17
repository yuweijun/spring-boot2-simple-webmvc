package com.example.simple.spring.web.mvc.http.server;

import com.example.simple.spring.web.mvc.http.HttpHeaders;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
 
public class ServletServerHttpResponse implements ServerHttpResponse {

	private final HttpServletResponse servletResponse;

	private final HttpHeaders headers = new HttpHeaders();

	private boolean headersWritten = false;
 
	public ServletServerHttpResponse(HttpServletResponse servletResponse) {
		Assert.notNull(servletResponse, "'servletResponse' must not be null");
		this.servletResponse = servletResponse;
	}
 
	public HttpServletResponse getServletResponse() {
		return this.servletResponse;
	}

	public void setStatusCode(HttpStatus status) {
		this.servletResponse.setStatus(status.value());
	}

	public HttpHeaders getHeaders() {
		return (this.headersWritten ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
	}

	public OutputStream getBody() throws IOException {
		writeHeaders();
		return this.servletResponse.getOutputStream();
	}

	public void close() {
		writeHeaders();
	}

	private void writeHeaders() {
		if (!this.headersWritten) {
			for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
				String headerName = entry.getKey();
				for (String headerValue : entry.getValue()) {
					this.servletResponse.addHeader(headerName, headerValue);
				}
			}
			this.headersWritten = true;
		}
	}

}
