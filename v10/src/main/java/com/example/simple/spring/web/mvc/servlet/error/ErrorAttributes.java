package com.example.simple.spring.web.mvc.servlet.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
 
public interface ErrorAttributes {
 
	String ERROR_ATTRIBUTE = ErrorAttributes.class.getName() + ".error";
 
	default Map<String, Object> getErrorAttributes(HttpServletRequest request, ErrorAttributeOptions options) {
		return Collections.emptyMap();
	}
 
	Throwable getError(HttpServletRequest request);
 
	void storeErrorInformation(HttpServletRequest request, Throwable error);

}
