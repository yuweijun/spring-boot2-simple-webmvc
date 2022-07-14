package com.example.simple.spring.boot.config;

import com.example.simple.spring.web.mvc.servlet.DispatcherServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import java.util.Arrays;
import java.util.List;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
public class SimpleDispatcherServletAutoConfiguration {

    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";
    public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = "dispatcherServletRegistration";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDispatcherServletAutoConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @Conditional(SimpleDispatcherServletAutoConfiguration.DefaultDispatcherServletCondition.class)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(WebMvcProperties.class)
    protected static class DispatcherServletConfiguration {

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public DispatcherServlet dispatcherServlet(WebMvcProperties webMvcProperties) {
            LOGGER.info("config bean dispatcherServlet");
            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            return dispatcherServlet;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(SimpleDispatcherServletAutoConfiguration.DispatcherServletRegistrationCondition.class)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(WebMvcProperties.class)
    @Import(SimpleDispatcherServletAutoConfiguration.DispatcherServletConfiguration.class)
    protected static class DispatcherServletRegistrationConfiguration {

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
        @ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public SimpleDispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet,
            WebMvcProperties webMvcProperties, ObjectProvider<MultipartConfigElement> multipartConfig) {
            final String path = webMvcProperties.getServlet().getPath();
            LOGGER.info("config bean dispatcherServletRegistration for path : [{}]", path);
            SimpleDispatcherServletRegistrationBean registration = new SimpleDispatcherServletRegistrationBean(dispatcherServlet, path);
            registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
            multipartConfig.ifAvailable(registration::setMultipartConfig);
            return registration;
        }

    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    private static class DefaultDispatcherServletCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition("Default DispatcherServlet");
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            List<String> dispatchServletBeans = Arrays.asList(beanFactory.getBeanNamesForType(DispatcherServlet.class, false, false));
            if (dispatchServletBeans.isEmpty()) {
                return ConditionOutcome.match(message.didNotFind("dispatcher servlet beans").atAll());
            }
            if (dispatchServletBeans.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("dispatcher servlet bean").items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            if (beanFactory.containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("non dispatcher servlet bean").items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            return ConditionOutcome.match(message.found("dispatcher servlet bean", "dispatcher servlet beans")
                                                 .items(ConditionMessage.Style.QUOTE, dispatchServletBeans)
                                                 .append("and none is named " + DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
        }

    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    private static class DispatcherServletRegistrationCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            ConditionOutcome outcome = checkDefaultDispatcherName(beanFactory);
            if (!outcome.isMatch()) {
                return outcome;
            }
            return checkServletRegistration(beanFactory);
        }

        private ConditionOutcome checkDefaultDispatcherName(ConfigurableListableBeanFactory beanFactory) {
            List<String> servlets = Arrays
                .asList(beanFactory.getBeanNamesForType(DispatcherServlet.class, false, false));
            boolean containsDispatcherBean = beanFactory.containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            if (containsDispatcherBean && !servlets.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome.noMatch(
                    startMessage().found("non dispatcher servlet").items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            return ConditionOutcome.match();
        }

        private ConditionOutcome checkServletRegistration(ConfigurableListableBeanFactory beanFactory) {
            ConditionMessage.Builder message = startMessage();
            List<String> registrations = Arrays
                .asList(beanFactory.getBeanNamesForType(ServletRegistrationBean.class, false, false));
            boolean containsDispatcherRegistrationBean = beanFactory
                .containsBean(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
            if (registrations.isEmpty()) {
                if (containsDispatcherRegistrationBean) {
                    return ConditionOutcome.noMatch(message.found("non servlet registration bean")
                                                           .items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
                }
                return ConditionOutcome.match(message.didNotFind("servlet registration bean").atAll());
            }
            if (registrations.contains(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("servlet registration bean")
                                                       .items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
            }
            if (containsDispatcherRegistrationBean) {
                return ConditionOutcome.noMatch(message.found("non servlet registration bean")
                                                       .items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
            }
            return ConditionOutcome.match(message.found("servlet registration beans").items(ConditionMessage.Style.QUOTE, registrations)
                                                 .append("and none is named " + DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
        }

        private ConditionMessage.Builder startMessage() {
            return ConditionMessage.forCondition("DispatcherServlet Registration");
        }

    }

}
