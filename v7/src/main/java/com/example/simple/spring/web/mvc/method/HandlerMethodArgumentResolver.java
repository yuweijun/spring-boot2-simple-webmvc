  

package com.example.simple.spring.web.mvc.method;

import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerMethodArgumentResolver {
  
	boolean supportsParameter(MethodParameter parameter);
  
	Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception;

}