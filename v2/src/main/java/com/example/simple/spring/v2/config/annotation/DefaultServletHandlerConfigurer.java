package com.example.simple.spring.v2.config.annotation;

import com.example.simple.spring.v2.web.servlet.handler.AbstractHandlerMapping;
import com.example.simple.spring.v2.web.servlet.handler.DefaultServletHttpRequestHandler;
import com.example.simple.spring.v2.web.servlet.handler.HttpRequestHandler;
import com.example.simple.spring.v2.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

public class DefaultServletHandlerConfigurer {

	private final ServletContext servletContext;

	private DefaultServletHttpRequestHandler handler;

	/**
	 * Create a {@link DefaultServletHandlerConfigurer} instance.
	 * @param servletContext the ServletContext to use to configure the underlying DefaultServletHttpRequestHandler.
	 */
	public DefaultServletHandlerConfigurer(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Enable forwarding to the "default" Servlet. When this method is used the {@link DefaultServletHttpRequestHandler}
	 * will try to auto-detect the "default" Servlet name. Alternatively, you can specify the name of the default 
	 * Servlet via {@link #enable(String)}.
	 * @see DefaultServletHttpRequestHandler
	 */
	public void enable() {
		enable(null);
	}

	/**
	 * Enable forwarding to the "default" Servlet identified by the given name.
	 * This is useful when the default Servlet cannot be auto-detected, for example when it has been manually configured.
	 * @see DefaultServletHttpRequestHandler
	 */
	public void enable(String defaultServletName) {
		handler = new DefaultServletHttpRequestHandler();
		handler.setDefaultServletName(defaultServletName);
		handler.setServletContext(servletContext);
	}

	/**
	 * Return a handler mapping instance ordered at {@link Integer#MAX_VALUE} containing the
	 * {@link DefaultServletHttpRequestHandler} instance mapped to {@code "/**"}; or {@code null} if 
	 * default servlet handling was not been enabled.
	 */
	protected AbstractHandlerMapping getHandlerMapping() {
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