package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.ServletRequestDataBinder;
import com.example.simple.spring.web.mvc.bind.ValueConstants;
import com.example.simple.spring.web.mvc.context.support.RequestScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNamedValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final Log logger = LogFactory.getLog(getClass());

    private final ConfigurableBeanFactory configurableBeanFactory;

    private final BeanExpressionContext expressionContext;

    private final Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>();

    public AbstractNamedValueMethodArgumentResolver() {
        configurableBeanFactory = null;
        expressionContext = null;
    }

    public AbstractNamedValueMethodArgumentResolver(ConfigurableBeanFactory beanFactory) {
        this.configurableBeanFactory = beanFactory;
        this.expressionContext = (beanFactory != null) ? new BeanExpressionContext(beanFactory, new RequestScope()) : null;
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter, HttpServletRequest request, HttpServletResponse response, ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        Class<?> paramType = parameter.getParameterType();

        NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);
        Object arg = resolveName(namedValueInfo.name, parameter, request, response);

        if (arg == null) {
            if (namedValueInfo.defaultValue != null) {
                arg = resolveDefaultValue(namedValueInfo.defaultValue);
            } else if (namedValueInfo.required) {
                handleMissingValue(namedValueInfo.name, parameter);
            }
            arg = handleNullValue(namedValueInfo.name, arg, paramType);
        }

        if (arg != null && servletRequestDataBinder != null) {
            arg = servletRequestDataBinder.convertIfNecessary(arg, paramType, parameter);
            if (arg != null) {
                logger.debug("arg convert type : " + arg.getClass().getSimpleName());
            }
        }

        handleResolvedValue(arg, namedValueInfo.name, parameter, request, response);

        return arg;
    }

    private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter);
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo);
            this.namedValueInfoCache.put(parameter, namedValueInfo);
        }
        return namedValueInfo;
    }

    protected abstract NamedValueInfo createNamedValueInfo(MethodParameter parameter);

    private NamedValueInfo updateNamedValueInfo(MethodParameter parameter, NamedValueInfo info) {
        String name = info.name;
        if (info.name.length() == 0) {
            name = parameter.getParameterName();
            Assert.notNull(name,
                "Name for argument type [" + parameter.getParameterType().getName() + "] not available, and parameter name information not found in class file either.");
        }
        String defaultValue = (ValueConstants.DEFAULT_NONE.equals(info.defaultValue) ? null : info.defaultValue);
        return new NamedValueInfo(name, info.required, defaultValue);
    }

    protected abstract Object resolveName(String name, MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) throws Exception;

    private Object resolveDefaultValue(String defaultValue) {
        if (this.configurableBeanFactory == null) {
            return defaultValue;
        }
        String placeholdersResolved = this.configurableBeanFactory.resolveEmbeddedValue(defaultValue);
        BeanExpressionResolver exprResolver = this.configurableBeanFactory.getBeanExpressionResolver();
        if (exprResolver == null) {
            return defaultValue;
        }
        return exprResolver.evaluate(placeholdersResolved, this.expressionContext);
    }

    protected abstract void handleMissingValue(String name, MethodParameter parameter) throws ServletException;

    private Object handleNullValue(String name, Object value, Class<?> paramType) {
        if (value == null) {
            if (Boolean.TYPE.equals(paramType)) {
                return Boolean.FALSE;
            } else if (paramType.isPrimitive()) {
                throw new IllegalStateException(
                    "Optional " + paramType + " parameter '" + name + "' is present but cannot be translated into a null value due to being declared as a "
                        + "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
            }
        }
        return value;
    }

    protected void handleResolvedValue(Object arg, String name, MethodParameter parameter, HttpServletRequest request, HttpServletResponse response) {
    }

    protected static class NamedValueInfo {

        private final String name;

        private final boolean required;

        private final String defaultValue;

        protected NamedValueInfo(String name, boolean required, String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }
}