package com.example.simple.spring.web.mvc.servlet.view;

import org.springframework.util.ClassUtils;

public class InternalResourceViewResolver extends UrlBasedViewResolver {

    private static final boolean jstlPresent = ClassUtils.isPresent("javax.servlet.jsp.jstl.core.Config", InternalResourceViewResolver.class.getClassLoader());

    private Boolean alwaysInclude;

    private Boolean exposeContextBeansAsAttributes;

    private String[] exposedContextBeanNames;

    public InternalResourceViewResolver() {
        Class viewClass = requiredViewClass();
        if (viewClass.equals(InternalResourceView.class) && jstlPresent) {
            viewClass = JstlView.class;
        }
        setViewClass(viewClass);
    }

    @Override
    protected Class requiredViewClass() {
        return InternalResourceView.class;
    }

    public void setAlwaysInclude(boolean alwaysInclude) {
        this.alwaysInclude = Boolean.valueOf(alwaysInclude);
    }

    public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
        this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
    }

    public void setExposedContextBeanNames(String[] exposedContextBeanNames) {
        this.exposedContextBeanNames = exposedContextBeanNames;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        InternalResourceView view = (InternalResourceView) super.buildView(viewName);
        if (this.alwaysInclude != null) {
            view.setAlwaysInclude(this.alwaysInclude);
        }
        if (this.exposeContextBeansAsAttributes != null) {
            view.setExposeContextBeansAsAttributes(this.exposeContextBeansAsAttributes);
        }
        if (this.exposedContextBeanNames != null) {
            view.setExposedContextBeanNames(this.exposedContextBeanNames);
        }
        view.setPreventDispatchLoop(true);
        return view;
    }

}
