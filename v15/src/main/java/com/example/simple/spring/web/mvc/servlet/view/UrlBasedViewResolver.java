package com.example.simple.spring.web.mvc.servlet.view;

import org.springframework.beans.BeanUtils;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class UrlBasedViewResolver extends AbstractCachingViewResolver implements Ordered {

    public static final String REDIRECT_URL_PREFIX = "redirect:";

    public static final String FORWARD_URL_PREFIX = "forward:";
    private final Map<String, Object> staticAttributes = new HashMap<>();
    private Class viewClass;
    private String prefix = "";
    private String suffix = "";
    private String[] viewNames = null;
    private String contentType;
    private boolean redirectContextRelative = true;
    private boolean redirectHttp10Compatible = true;
    private String requestContextAttribute;
    private int order = Integer.MAX_VALUE;
    private Boolean exposePathVariables;

    protected Class getViewClass() {
        return this.viewClass;
    }

    public void setViewClass(Class viewClass) {
        if (viewClass == null || !requiredViewClass().isAssignableFrom(viewClass)) {
            throw new IllegalArgumentException(
                "Given view class [" + (viewClass != null ? viewClass.getName() : null) +
                    "] is not of type [" + requiredViewClass().getName() + "]");
        }
        this.viewClass = viewClass;
    }

    protected Class requiredViewClass() {
        return AbstractUrlBasedView.class;
    }

    protected String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    protected String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    protected String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected boolean isRedirectContextRelative() {
        return this.redirectContextRelative;
    }

    public void setRedirectContextRelative(boolean redirectContextRelative) {
        this.redirectContextRelative = redirectContextRelative;
    }

    protected boolean isRedirectHttp10Compatible() {
        return this.redirectHttp10Compatible;
    }

    public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
        this.redirectHttp10Compatible = redirectHttp10Compatible;
    }

    protected String getRequestContextAttribute() {
        return this.requestContextAttribute;
    }

    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }

    public void setAttributes(Properties props) {
        CollectionUtils.mergePropertiesIntoMap(props, this.staticAttributes);
    }

    public Map<String, Object> getAttributesMap() {
        return this.staticAttributes;
    }

    public void setAttributesMap(Map<String, ?> attributes) {
        if (attributes != null) {
            this.staticAttributes.putAll(attributes);
        }
    }

    protected String[] getViewNames() {
        return this.viewNames;
    }

    public void setViewNames(String[] viewNames) {
        this.viewNames = viewNames;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setExposePathVariables(Boolean exposePathVariables) {
        this.exposePathVariables = exposePathVariables;
    }

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        if (getViewClass() == null) {
            throw new IllegalArgumentException("Property 'viewClass' is required");
        }
    }

    @Override
    protected Object getCacheKey(String viewName) {
        return viewName;
    }

    @Override
    protected View createView(String viewName) throws Exception {
        // If this resolver is not supposed to handle the given view,
        // return null to pass on to the next resolver in the chain.
        if (!canHandle(viewName)) {
            return null;
        }
        // Check for special "redirect:" prefix.
        if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
            String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
            logger.debug("redirect url is " + redirectUrl);
            RedirectView view = new RedirectView(redirectUrl, isRedirectContextRelative(), isRedirectHttp10Compatible());
            return applyLifecycleMethods(viewName, view);
        }
        // Check for special "forward:" prefix.
        if (viewName.startsWith(FORWARD_URL_PREFIX)) {
            String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length());
            return new InternalResourceView(forwardUrl);
        }
        // Else fall back to superclass implementation: calling loadView.
        return super.createView(viewName);
    }

    protected boolean canHandle(String viewName) {
        String[] viewNames = getViewNames();
        return (viewNames == null || PatternMatchUtils.simpleMatch(viewNames, viewName));
    }

    @Override
    protected View loadView(String viewName) throws Exception {
        AbstractUrlBasedView view = buildView(viewName);
        View result = applyLifecycleMethods(viewName, view);
        return result;
    }

    private View applyLifecycleMethods(String viewName, AbstractView view) {
        return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
    }

    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractUrlBasedView view = (AbstractUrlBasedView) BeanUtils.instantiateClass(getViewClass());
        final String viewUrl = getPrefix() + viewName + getSuffix();
        logger.debug("set view url : " + viewUrl);

        view.setUrl(viewUrl);
        String contentType = getContentType();
        if (contentType != null) {
            view.setContentType(contentType);
        }
        view.setRequestContextAttribute(getRequestContextAttribute());
        view.setAttributesMap(getAttributesMap());
        if (this.exposePathVariables != null) {
            view.setExposePathVariables(exposePathVariables);
        }
        return view;
    }

}
