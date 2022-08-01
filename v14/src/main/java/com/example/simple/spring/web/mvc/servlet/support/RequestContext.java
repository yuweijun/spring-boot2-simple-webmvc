package com.example.simple.spring.web.mvc.servlet.support;

import com.example.simple.spring.web.mvc.context.SimpleWebApplicationContext;
import com.example.simple.spring.web.mvc.util.UrlPathHelper;
import com.example.simple.spring.web.mvc.util.WebUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.ui.context.Theme;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestContext {

    public static final String DEFAULT_THEME_NAME = "theme";

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = RequestContext.class.getName() + ".CONTEXT";
    protected static final boolean jstlPresent = ClassUtils.isPresent("javax.servlet.jsp.jstl.core.Config", RequestContext.class.getClassLoader());
    private static final String REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME = "requestDataValueProcessor";
    private HttpServletRequest request;

    private HttpServletResponse response;

    private Map<String, Object> model;

    private SimpleWebApplicationContext webApplicationContext;

    private Locale locale;

    private Theme theme;

    private Boolean defaultHtmlEscape;

    private UrlPathHelper urlPathHelper;

    // private RequestDataValueProcessor requestDataValueProcessor;

    private Map<String, Errors> errorsMap;

    public RequestContext(HttpServletRequest request) {
        initContext(request, null, null, null);
    }

    public RequestContext(HttpServletRequest request, ServletContext servletContext) {
        initContext(request, null, servletContext, null);
    }

    public RequestContext(HttpServletRequest request, Map<String, Object> model) {
        initContext(request, null, null, model);
    }

    public RequestContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Map<String, Object> model) {

        initContext(request, response, servletContext, model);
    }

    protected RequestContext() {
    }

    protected void initContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Map<String, Object> model) {

        this.request = request;
        this.response = response;
        this.model = model;

        // Fetch SimpleWebApplicationContext, either from DispatcherServlet or the root context.
        // ServletContext needs to be specified to be able to fall back to the root context!
        this.webApplicationContext = (SimpleWebApplicationContext) request.getAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (this.webApplicationContext == null) {
            this.webApplicationContext = RequestContextUtils.getWebApplicationContext(request, servletContext);
        }

        // Determine default HTML escape setting from the "defaultHtmlEscape"
        // context-param in web.xml, if any.
        this.defaultHtmlEscape = WebUtils.getDefaultHtmlEscape(this.webApplicationContext.getServletContext());

        this.urlPathHelper = new UrlPathHelper();

        try {
            // this.requestDataValueProcessor = this.webApplicationContext.getBean(REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME, RequestDataValueProcessor.class);
        } catch (NoSuchBeanDefinitionException ex) {
            // Ignored
        }
    }

    protected final HttpServletRequest getRequest() {
        return this.request;
    }

    protected final ServletContext getServletContext() {
        return this.webApplicationContext.getServletContext();
    }

    public final SimpleWebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    public final MessageSource getMessageSource() {
        return this.webApplicationContext;
    }

    public final Map<String, Object> getModel() {
        return this.model;
    }

    public final Locale getLocale() {
        return this.locale;
    }

    public boolean isDefaultHtmlEscape() {
        return (this.defaultHtmlEscape != null && this.defaultHtmlEscape.booleanValue());
    }

    public Boolean getDefaultHtmlEscape() {
        return this.defaultHtmlEscape;
    }

    public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
        this.defaultHtmlEscape = defaultHtmlEscape;
    }

    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    // public RequestDataValueProcessor getRequestDataValueProcessor() {
    // 	return this.requestDataValueProcessor;
    // }

    public String getContextPath() {
        return this.urlPathHelper.getOriginatingContextPath(this.request);
    }

    public String getContextUrl(String relativeUrl) {
        String url = getContextPath() + relativeUrl;
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    public String getContextUrl(String relativeUrl, Map<String, ?> params) {
        String url = getContextPath() + relativeUrl;
        // UriTemplate template = new UriTemplate(url);
        // url = template.expand(params).toASCIIString();
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    public String getPathToServlet() {
        return this.urlPathHelper.getOriginatingContextPath(this.request) + this.urlPathHelper.getOriginatingServletPath(this.request);
    }

    public String getRequestUri() {
        return this.urlPathHelper.getOriginatingRequestUri(this.request);
    }

    public String getQueryString() {
        return this.urlPathHelper.getOriginatingQueryString(this.request);
    }

    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage, isDefaultHtmlEscape());
    }

    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, isDefaultHtmlEscape());
    }

    public String getMessage(String code, List args, String defaultMessage) {
        return getMessage(code, (args != null ? args.toArray() : null), defaultMessage, isDefaultHtmlEscape());
    }

    public String getMessage(String code, Object[] args, String defaultMessage, boolean htmlEscape) {
        String msg = this.webApplicationContext.getMessage(code, args, defaultMessage, this.locale);
        return msg;
    }

    public String getMessage(String code) throws NoSuchMessageException {
        return getMessage(code, null, isDefaultHtmlEscape());
    }

    public String getMessage(String code, Object[] args) throws NoSuchMessageException {
        return getMessage(code, args, isDefaultHtmlEscape());
    }

    public String getMessage(String code, List args) throws NoSuchMessageException {
        return getMessage(code, (args != null ? args.toArray() : null), isDefaultHtmlEscape());
    }

    public String getMessage(String code, Object[] args, boolean htmlEscape) throws NoSuchMessageException {
        String msg = this.webApplicationContext.getMessage(code, args, this.locale);
        return msg;
    }

    public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
        return getMessage(resolvable, isDefaultHtmlEscape());
    }

    public String getMessage(MessageSourceResolvable resolvable, boolean htmlEscape) throws NoSuchMessageException {
        String msg = this.webApplicationContext.getMessage(resolvable, this.locale);
        return msg;
    }

    public Errors getErrors(String name) {
        return getErrors(name, isDefaultHtmlEscape());
    }

    public Errors getErrors(String name, boolean htmlEscape) {
        if (this.errorsMap == null) {
            this.errorsMap = new HashMap<>();
        }
        Errors errors = this.errorsMap.get(name);
        boolean put = false;
        if (errors == null) {
            errors = (Errors) getModelObject(BindingResult.MODEL_KEY_PREFIX + name);
            // Check old BindException prefix for backwards compatibility.
            if (errors instanceof BindException) {
                errors = ((BindException) errors).getBindingResult();
            }
            if (errors == null) {
                return null;
            }
            put = true;
        }
        if (put) {
            this.errorsMap.put(name, errors);
        }
        return errors;
    }

    protected Object getModelObject(String modelName) {
        if (this.model != null) {
            return this.model.get(modelName);
        } else {
            return this.request.getAttribute(modelName);
        }
    }

}
