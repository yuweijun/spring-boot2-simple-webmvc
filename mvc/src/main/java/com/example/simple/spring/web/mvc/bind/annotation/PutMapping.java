package com.example.simple.spring.web.mvc.bind.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.PUT)
public @interface PutMapping {
 
	@AliasFor(annotation = RequestMapping.class)
	String name() default "";
 
	@AliasFor(annotation = RequestMapping.class)
	String[] value() default {};
 
	@AliasFor(annotation = RequestMapping.class)
	String[] path() default {};
 
	@AliasFor(annotation = RequestMapping.class)
	String[] params() default {};
 
	@AliasFor(annotation = RequestMapping.class)
	String[] headers() default {};
 
	@AliasFor(annotation = RequestMapping.class)
	String[] consumes() default {};
 
	@AliasFor(annotation = RequestMapping.class)
	String[] produces() default {};

}
