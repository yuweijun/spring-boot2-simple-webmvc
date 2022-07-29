package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ModelFactory {

    private final List<InvocableHandlerMethod> attributeMethods;

    // private final WebDataBinderFactory binderFactory;

    // private final SessionAttributesHandler sessionAttributesHandler;

    public ModelFactory(List<InvocableHandlerMethod> attributeMethods) {
        // SessionAttributesHandler sessionAttributesHandler
        this.attributeMethods = (attributeMethods != null) ? attributeMethods : new ArrayList<InvocableHandlerMethod>();
        // this.sessionAttributesHandler = sessionAttributesHandler;
    }

    public void initModel(HttpServletRequest request, HandlerMethod handlerMethod) throws Exception {
        // Map<String, ?> attributesInSession = this.sessionAttributesHandler.retrieveAttributes(request);
        // mavContainer.mergeAttributes(attributesInSession);

        invokeModelAttributeMethods(request);

        // for (String name : findSessionAttributeArguments(handlerMethod)) {
        //     if (!mavContainer.containsAttribute(name)) {
        //         Object value = this.sessionAttributesHandler.retrieveAttribute(request, name);
        //         if (value == null) {
        //             throw new HttpSessionRequiredException("Expected session attribute '" + name + "'");
        //         }
        //         ModelAndView.addAttribute(request, name, value);
        //     }
        // }

    }

    private void invokeModelAttributeMethods(HttpServletRequest request) throws Exception {
        for (InvocableHandlerMethod attrMethod : this.attributeMethods) {
            String modelName = attrMethod.getMethodAnnotation(ModelAttribute.class).value();
            if (ModelAndView.containsAttribute(request, modelName)) {
                continue;
            }

            Object returnValue = attrMethod.invokeForRequest(request);

            if (!attrMethod.isVoid()) {
                String returnValueName = getNameForReturnValue(returnValue, attrMethod.getReturnType());
                if (!ModelAndView.containsAttribute(request, returnValueName)) {
                    ModelAndView.addAttribute(request, returnValueName, returnValue);
                }
            }
        }
    }

    // private List<String> findSessionAttributeArguments(HandlerMethod handlerMethod) {
    // 	List<String> result = new ArrayList<String>();
    // 	for (MethodParameter param : handlerMethod.getMethodParameters()) {
    // 		if (param.hasParameterAnnotation(ModelAttribute.class)) {
    // 			String name = getNameForParameter(param);
    // 			if (this.sessionAttributesHandler.isHandlerSessionAttribute(name, param.getParameterType())) {
    // 				result.add(name);
    // 			}
    // 		}
    // 	}
    // 	return result;
    // }

    public static String getNameForReturnValue(Object returnValue, MethodParameter returnType) {
        ModelAttribute annot = returnType.getMethodAnnotation(ModelAttribute.class);
        if (annot != null && StringUtils.hasText(annot.value())) {
            return annot.value();
        } else {
            Method method = returnType.getMethod();
            Class<?> resolvedType = GenericTypeResolver.resolveReturnType(method, returnType.getDeclaringClass());
            return Conventions.getVariableNameForReturnType(method, resolvedType, returnValue);
        }
    }

    public static String getNameForParameter(MethodParameter parameter) {
        ModelAttribute annot = parameter.getParameterAnnotation(ModelAttribute.class);
        String attrName = (annot != null) ? annot.value() : null;
        return StringUtils.hasText(attrName) ? attrName : Conventions.getVariableNameForParameter(parameter);
    }

    public void updateModel(HttpServletRequest request) throws Exception {

        // if (mavContainer.getSessionStatus().isComplete()){
        // 	this.sessionAttributesHandler.cleanupAttributes(request);
        // }
        // else {
        // 	this.sessionAttributesHandler.storeAttributes(request, mavContainer.getModel());
        // }

        updateBindingResult(request);
    }

    private void updateBindingResult(HttpServletRequest request) throws Exception {
        final ModelAndView modelAndView = ModelAndView.get(request);
        if (modelAndView == null) {
            return;
        }
        final ModelMap model = modelAndView.getModelMap();
        List<String> keyNames = new ArrayList<>(model.keySet());
        for (String name : keyNames) {
            Object value = model.get(name);

            if (isBindingCandidate(name, value)) {
                String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + name;

                // if (!model.containsAttribute(bindingResultKey)) {
                // 	WebDataBinder dataBinder = binderFactory.createBinder(request, value, name);
                // 	model.put(bindingResultKey, dataBinder.getBindingResult());
                // }

            }
        }
    }

    private boolean isBindingCandidate(String attributeName, Object value) {
        if (attributeName.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
            return false;
        }

        Class<?> attrType = (value != null) ? value.getClass() : null;

        // if (this.sessionAttributesHandler.isHandlerSessionAttribute(attributeName, attrType)) {
        // 	return true;
        // }

        return (value != null && !value.getClass().isArray() && !(value instanceof Collection) &&
            !(value instanceof Map) && !BeanUtils.isSimpleValueType(value.getClass()));
    }

}