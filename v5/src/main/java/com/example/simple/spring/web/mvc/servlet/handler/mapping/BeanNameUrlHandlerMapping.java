package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanNameUrlHandlerMapping extends AbstractUrlHandlerMapping {

    private boolean detectHandlersInAncestorContexts = false;

    public void setDetectHandlersInAncestorContexts(boolean detectHandlersInAncestorContexts) {
        this.detectHandlersInAncestorContexts = detectHandlersInAncestorContexts;
    }

    @Override
    public void initApplicationContext() throws ApplicationContextException {
        super.initApplicationContext();
        detectHandlers();
    }

    protected void detectHandlers() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for URL mappings in application context: " + getApplicationContext());
        }
        String[] beanNames = (this.detectHandlersInAncestorContexts ?
            BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
            getApplicationContext().getBeanNamesForType(Object.class));

        // Take any bean name that we can determine URLs for.
        for (String beanName : beanNames) {
            String[] urls = determineUrlsForHandler(beanName);
            if (!ObjectUtils.isEmpty(urls)) {
                // URL paths found: Let's consider it a handler.
                registerHandler(urls, beanName);
            } else {
                logger.trace("Rejected bean name '" + beanName + "': no URL paths identified");
            }
        }
    }

    protected String[] determineUrlsForHandler(String beanName) {
        List<String> urls =  new ArrayList<>();
        if (beanName.startsWith("/")) {
            logger.debug("bean name is " + beanName);
            urls.add(beanName);
        }
        String[] aliases = getApplicationContext().getAliases(beanName);
        for (String alias : aliases) {
            if (alias.startsWith("/")) {
                urls.add(alias);
            }
        }
        return StringUtils.toStringArray(urls);
    }

}
