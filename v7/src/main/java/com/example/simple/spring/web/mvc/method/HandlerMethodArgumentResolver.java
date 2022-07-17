  

package com.example.simple.spring.web.mvc.method;

import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerMethodArgumentResolver {

	String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

	boolean supportsParameter(MethodParameter parameter);
  
	Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception;

}