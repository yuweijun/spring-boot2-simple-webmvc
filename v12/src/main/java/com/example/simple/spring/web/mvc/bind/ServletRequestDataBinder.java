  

package com.example.simple.spring.web.mvc.bind;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindException;

import javax.servlet.ServletRequest;
  
public class ServletRequestDataBinder extends WebDataBinder {
  
	public ServletRequestDataBinder(Object target) {
		super(target);
	}
  
	public ServletRequestDataBinder(Object target, String objectName) {
		super(target, objectName);
	}
  
	public void bind(ServletRequest request) {
		MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
		addBindValues(mpvs, request);
		doBind(mpvs);
	}
  
	protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
	}
  
	public void closeNoCatch() throws ServletRequestBindingException {
		if (getBindingResult().hasErrors()) {
			throw new ServletRequestBindingException(
					"Errors binding onto object '" + getBindingResult().getObjectName() + "'",
					new BindException(getBindingResult()));
		}
	}

}
