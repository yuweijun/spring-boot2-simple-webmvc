package com.example.simple.spring.v1.web.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class SimpleAnnotationConfigServletWebServerApplicationContext extends SimpleServletWebServerApplicationContext
    implements AnnotationConfigRegistry {

    private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

    private String[] basePackages;

    public SimpleAnnotationConfigServletWebServerApplicationContext() {
    }

    public SimpleAnnotationConfigServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    public SimpleAnnotationConfigServletWebServerApplicationContext(Class<?>... annotatedClasses) {
        this();
        register(annotatedClasses);
        refresh();
    }

    public SimpleAnnotationConfigServletWebServerApplicationContext(String... basePackages) {
        this();
        scan(basePackages);
        refresh();
    }

    @Override
    public void setEnvironment(ConfigurableEnvironment environment) {
    }

    /**
     * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader} and/or {@link ClassPathBeanDefinitionScanner}, if any.
     * <p>
     * Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
     * <p>
     * Any call to this method must occur prior to calls to {@link #register(Class...)} and/or {@link #scan(String...)}.
     *
     * @param beanNameGenerator the bean name generator
     * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
     * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
     */
    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
    }

    /**
     * Set the {@link ScopeMetadataResolver} to use for detected bean classes.
     * <p>
     * The default is an {@link AnnotationScopeMetadataResolver}.
     * <p>
     * Any call to this method must occur prior to calls to {@link #register(Class...)} and/or {@link #scan(String...)}.
     *
     * @param scopeMetadataResolver the scope metadata resolver
     */
    public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
    }

    /**
     * Register one or more annotated classes to be processed. Note that {@link #refresh()} must be called in order for the context to fully process the new class.
     * <p>
     * Calls to {@code #register} are idempotent; adding the same annotated class more than once has no additional effect.
     *
     * @param annotatedClasses one or more annotated classes, e.g. {@code @Configuration} classes
     * @see #scan(String...)
     * @see #refresh()
     */
    @Override
    public final void register(Class<?>... annotatedClasses) {
        Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
        this.annotatedClasses.addAll(Arrays.asList(annotatedClasses));
    }

    /**
     * Perform a scan within the specified base packages. Note that {@link #refresh()} must be called in order for the context to fully process the new class.
     *
     * @param basePackages the packages to check for annotated classes
     * @see #register(Class...)
     * @see #refresh()
     */
    @Override
    public final void scan(String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        this.basePackages = basePackages;
    }

    protected void prepareRefresh() {
        super.prepareRefresh();
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
    }

    /**
     * {@link ApplicationContextFactory} registered in {@code spring.factories} to support {@link SimpleAnnotationConfigServletWebServerApplicationContext}.
     */
    static class Factory implements ApplicationContextFactory {

        @Override
        public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
            return (webApplicationType != WebApplicationType.SERVLET) ? null
                : new SimpleAnnotationConfigServletWebServerApplicationContext();
        }

    }

}
