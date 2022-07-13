package com.example.simple.spring.v2.config.annotation;

import com.example.simple.spring.v2.web.context.ServletContextAware;
import com.example.simple.spring.v2.web.servlet.HandlerMapping;
import com.example.simple.spring.v2.web.servlet.SimpleControllerHandlerAdapter;
import com.example.simple.spring.v2.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public abstract class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

	private ServletContext servletContext;

	private ApplicationContext applicationContext;

	private List<Object> interceptors;

	// private List<HttpMessageConverter<?>> messageConverters;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Return a {@link RequestMappingHandlerMapping} ordered at 0 for mapping 
	 * requests to annotated controllers.
	 */
	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
		return handlerMapping;
	}
	
	/**
	 * Return a handler mapping ordered at Integer.MAX_VALUE with a mapped 
	 * default servlet handler. To configure "default" Servlet handling, 
	 * override {@link #configureDefaultServletHandling}.  
	 */
	@Bean
	public HandlerMapping defaultServletHandlerMapping() {
		DefaultServletHandlerConfigurer configurer = new DefaultServletHandlerConfigurer(servletContext);
		configureDefaultServletHandling(configurer);
		AbstractHandlerMapping handlerMapping = configurer.getHandlerMapping();
		handlerMapping = handlerMapping != null ? handlerMapping : new EmptyHandlerMapping();
		return handlerMapping;
	}

	/**
	 * Override this method to configure "default" Servlet handling. 
	 * @see DefaultServletHandlerConfigurer
	 */
	protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
	}


	/**
	 * Returns a {@link FormattingConversionService} for use with annotated 
	 * controller methods and the {@code spring:eval} JSP tag. 
	 * Also see {@link #addFormatters} as an alternative to overriding this method.
	 */
	@Bean
	public FormattingConversionService mvcConversionService() {
		FormattingConversionService conversionService = new DefaultFormattingConversionService();
		addFormatters(conversionService);
		return conversionService;
	}

	/**
	 * Override this method to add custom {@link Converter}s and {@link Formatter}s.
	 */
	protected void addFormatters(FormatterRegistry registry) {
	}

	/**
	 * Returns a {@link SimpleControllerHandlerAdapter} for processing requests 
	 * with interface-based controllers.
	 */
	@Bean
	public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
		return new SimpleControllerHandlerAdapter();
	}

	private final static class EmptyHandlerMapping extends AbstractHandlerMapping {
		
		@Override
		protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
			return null;
		}
	}
	
}
