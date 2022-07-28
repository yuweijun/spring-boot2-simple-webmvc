 

package com.example.simple.spring.web.mvc.servlet.config;

import com.example.simple.spring.web.mvc.servlet.handler.HttpRequestHandler;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.AbstractHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.handler.mapping.SimpleUrlHandlerMapping;
import com.example.simple.spring.web.mvc.servlet.resource.DefaultServletHttpRequestHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
 
public class DefaultServletHandlerConfigurer {

	private final ServletContext servletContext;

	private DefaultServletHttpRequestHandler handler;
 
	public DefaultServletHandlerConfigurer(ServletContext servletContext) {
		Assert.notNull(servletContext, "A ServletContext is required to configure default servlet handling");
		this.servletContext = servletContext;
	}
 
	public void enable() {
		enable(null);
	}
 
	public void enable(String defaultServletName) {
		handler = new DefaultServletHttpRequestHandler();
		handler.setDefaultServletName(defaultServletName);
		handler.setServletContext(servletContext);
	}
 
	public AbstractHandlerMapping getHandlerMapping() {
		if (handler == null) {
			return null;
		}
		
		Map<String, HttpRequestHandler> urlMap = new HashMap<String, HttpRequestHandler>();
		urlMap.put("/**", handler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(Integer.MAX_VALUE);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

}