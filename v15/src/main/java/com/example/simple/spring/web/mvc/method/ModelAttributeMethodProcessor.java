package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import com.example.simple.spring.web.mvc.bind.WebDataBinder;
import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

public class ModelAttributeMethodProcessor implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    protected Log logger = LogFactory.getLog(this.getClass());

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ModelAttribute.class);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(ModelAttribute.class);
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        String name = ModelFactory.getNameForParameter(parameter);
        final ModelMap model = ModelAndView.getModel(request);
        Object target = (model.containsAttribute(name)) ? model.get(name) : createAttribute(name, parameter, request);
        logger.info("resolveArgument target is " + target + ", name is " + name);

        return target;
    }

    protected Object createAttribute(String attributeName, MethodParameter parameter, HttpServletRequest request) throws Exception {
        logger.info("create attribute " + attributeName);
        return BeanUtils.instantiateClass(parameter.getParameterType());
    }

    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation annot : annotations) {
            if (annot.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = AnnotationUtils.getValue(annot);
                binder.validate(hints instanceof Object[] ? hints : new Object[]{hints});
            }
        }
    }

    protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
        int i = parameter.getParameterIndex();
        Class<?>[] paramTypes = parameter.getMethod().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));

        return !hasBindingResult;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (returnValue != null) {
            String name = ModelFactory.getNameForReturnValue(returnValue, returnType);
            ModelAndView.addAttribute(request, name, returnValue);
        }
    }
}