package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.RequestParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    private final Log logger = LogFactory.getLog(getClass());

    private final boolean useDefaultResolution;

    public RequestParamMethodArgumentResolver(ConfigurableBeanFactory beanFactory, boolean useDefaultResolution) {
        super(beanFactory);
        this.useDefaultResolution = useDefaultResolution;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        if (parameter.hasParameterAnnotation(RequestParam.class)) {
            if (Map.class.isAssignableFrom(paramType)) {
                String paramName = parameter.getParameterAnnotation(RequestParam.class).value();
                return StringUtils.hasText(paramName);
            } else {
                return true;
            }
        } else {
            if (this.useDefaultResolution) {
                return BeanUtils.isSimpleProperty(paramType);
            } else {
                return false;
            }
        }
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
        return (annotation != null) ? new RequestParamNamedValueInfo(annotation) : new RequestParamNamedValueInfo();
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.debug("resolve parameter name : " + name);
        String[] paramValues = request.getParameterValues(name);
        if (paramValues != null) {
            return paramValues.length == 1 ? paramValues[0] : paramValues;
        }

        return null;
    }

    @Override
    protected void handleMissingValue(String paramName, MethodParameter parameter) throws ServletException {
        logger.info("missing param : " + paramName);
    }

    private class RequestParamNamedValueInfo extends NamedValueInfo {
        private RequestParamNamedValueInfo() {
            super("", false, DEFAULT_NONE);
        }

        private RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.value(), annotation.required(), annotation.defaultValue());
        }
    }
}