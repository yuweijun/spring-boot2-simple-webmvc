package com.example.simple.spring.web.mvc.bind.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SessionAttributes {
 
	@AliasFor("names")
	String[] value() default {};
 
	@AliasFor("value")
	String[] names() default {};
 
	Class<?>[] types() default {};

}
