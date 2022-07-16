package com.example.simple.spring.web.mvc.servlet.handler.mapping;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

    @Override
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
