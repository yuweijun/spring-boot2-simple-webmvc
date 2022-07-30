  

package com.example.simple.spring.web.mvc.bind;

import org.springframework.core.NestedRuntimeException;
  
public class ServletRequestBindingException extends NestedRuntimeException {
  
	public ServletRequestBindingException(String msg) {
		super(msg);
	}
  
	public ServletRequestBindingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
