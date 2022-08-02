package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.ModelAttribute;
import com.example.simple.spring.web.mvc.bind.support.SessionStatus;
import com.example.simple.spring.web.mvc.context.request.SessionAttributesHandler;
import com.example.simple.spring.web.mvc.servlet.view.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ModelFactory {

    private final Log logger = LogFactory.getLog(getClass());

    private final List<InvocableHandlerMethod> attributeMethods;

    // private final WebDataBinderFactory binderFactory;

    private final SessionAttributesHandler sessionAttributesHandler;

    public ModelFactory(List<InvocableHandlerMethod> attributeMethods, SessionAttributesHandler sessionAttributesHandler) {
        this.attributeMethods = (attributeMethods != null) ? attributeMethods : new ArrayList<InvocableHandlerMethod>();
        this.sessionAttributesHandler = sessionAttributesHandler;
    }

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

    public void initModel(HttpServletRequest request, HandlerMethod handlerMethod) throws Exception {
        Map<String, ?> sessionAttributes = this.sessionAttributesHandler.retrieveAttributes(request);
        final ModelMap model = ModelAndView.getModel(request);
        model.mergeAttributes(sessionAttributes);

        invokeModelAttributeMethods(request);

        for (String name : findSessionAttributeArguments(handlerMethod)) {
            if (!model.containsAttribute(name)) {
                Object value = this.sessionAttributesHandler.retrieveAttribute(request, name);
                if (value == null) {
                    throw new ServletException("Expected session attribute '" + name + "'");
                }
                ModelAndView.addAttribute(request, name, value);
            }
        }

    }

    private void invokeModelAttributeMethods(HttpServletRequest request) throws Exception {
        for (InvocableHandlerMethod attrMethod : this.attributeMethods) {
            String modelName = attrMethod.getMethodAnnotation(ModelAttribute.class).value();
            if (ModelAndView.containsAttribute(request, modelName)) {
                logger.debug(modelName + " attribute has been exist in model, skip method invocation: " + attrMethod.getMethod() + " invokeForRequest(request)");
                continue;
            }

            Object returnValue = attrMethod.invokeForRequest(request);

            if (!attrMethod.isVoid()) {
                String returnValueName = getNameForReturnValue(returnValue, attrMethod.getReturnType());
                if (!ModelAndView.containsAttribute(request, returnValueName)) {
                    logger.debug("add return value to model : " + returnValueName);
                    ModelAndView.addAttribute(request, returnValueName, returnValue);
                }
            }
        }
    }

    private List<String> findSessionAttributeArguments(HandlerMethod handlerMethod) {
        List<String> result = new ArrayList<>();
        for (MethodParameter param : handlerMethod.getMethodParameters()) {
            if (param.hasParameterAnnotation(ModelAttribute.class)) {
                String name = getNameForParameter(param);
                if (this.sessionAttributesHandler.isHandlerSessionAttribute(name, param.getParameterType())) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public void updateModel(HttpServletRequest request) throws Exception {
        final ModelMap model = ModelAndView.getModel(request);
        final SessionStatus sessionStatus = ModelAndView.getSessionStatus(request);
        if (sessionStatus.isComplete()) {
            this.sessionAttributesHandler.cleanupAttributes(request);
        } else {
            this.sessionAttributesHandler.storeAttributes(request, model);
        }

    }

}