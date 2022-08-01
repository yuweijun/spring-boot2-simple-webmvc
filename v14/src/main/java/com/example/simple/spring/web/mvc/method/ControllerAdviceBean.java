package com.example.simple.spring.web.mvc.method;

import com.example.simple.spring.web.mvc.bind.annotation.ControllerAdvice;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

public class ControllerAdviceBean implements Ordered {

    private final Object beanOrName;

    private final boolean isSingleton;
    private final Class<?> beanType;
    private final HandlerTypePredicate beanTypePredicate;
    private final BeanFactory beanFactory;
    private Object resolvedBean;
    private Integer order;

    public ControllerAdviceBean(Object bean) {
        Assert.notNull(bean, "Bean must not be null");
        this.beanOrName = bean;
        this.isSingleton = true;
        this.resolvedBean = bean;
        this.beanType = ClassUtils.getUserClass(bean.getClass());
        this.beanTypePredicate = createBeanTypePredicate(this.beanType);
        this.beanFactory = null;
    }

    public ControllerAdviceBean(String beanName, BeanFactory beanFactory) {
        this(beanName, beanFactory, null);
    }

    public ControllerAdviceBean(String beanName, BeanFactory beanFactory, ControllerAdvice controllerAdvice) {
        Assert.hasText(beanName, "Bean name must contain text");
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        Assert.isTrue(beanFactory.containsBean(beanName), () -> "BeanFactory [" + beanFactory +
            "] does not contain specified controller advice bean '" + beanName + "'");

        this.beanOrName = beanName;
        this.isSingleton = beanFactory.isSingleton(beanName);
        this.beanType = getBeanType(beanName, beanFactory);
        this.beanTypePredicate = (controllerAdvice != null ? createBeanTypePredicate(controllerAdvice) :
            createBeanTypePredicate(this.beanType));
        this.beanFactory = beanFactory;
    }

    public static List<ControllerAdviceBean> findAnnotatedBeans(ApplicationContext context) {
        List<ControllerAdviceBean> adviceBeans = new ArrayList<>();
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, Object.class)) {
            if (!ScopedProxyUtils.isScopedTarget(name)) {
                ControllerAdvice controllerAdvice = context.findAnnotationOnBean(name, ControllerAdvice.class);
                if (controllerAdvice != null) {
                    // Use the @ControllerAdvice annotation found by findAnnotationOnBean()
                    // in order to avoid a subsequent lookup of the same annotation.
                    adviceBeans.add(new ControllerAdviceBean(name, context, controllerAdvice));
                }
            }
        }
        OrderComparator.sort(adviceBeans);
        return adviceBeans;
    }

    private static Class<?> getBeanType(String beanName, BeanFactory beanFactory) {
        Class<?> beanType = beanFactory.getType(beanName);
        return (beanType != null ? ClassUtils.getUserClass(beanType) : null);
    }

    private static HandlerTypePredicate createBeanTypePredicate(Class<?> beanType) {
        ControllerAdvice controllerAdvice = (beanType != null ?
            AnnotatedElementUtils.findMergedAnnotation(beanType, ControllerAdvice.class) : null);
        return createBeanTypePredicate(controllerAdvice);
    }

    private static HandlerTypePredicate createBeanTypePredicate(ControllerAdvice controllerAdvice) {
        if (controllerAdvice != null) {
            return HandlerTypePredicate.builder()
                                       .basePackage(controllerAdvice.basePackages())
                                       .basePackageClass(controllerAdvice.basePackageClasses())
                                       .assignableType(controllerAdvice.assignableTypes())
                                       .annotation(controllerAdvice.annotations())
                                       .build();
        }
        return HandlerTypePredicate.forAnyHandlerType();
    }

    @Override
    public int getOrder() {
        if (this.order == null) {
            Object resolvedBean = null;
            if (this.beanFactory != null && this.beanOrName instanceof String) {
                String beanName = (String) this.beanOrName;
                String targetBeanName = ScopedProxyUtils.getTargetBeanName(beanName);
                boolean isScopedProxy = this.beanFactory.containsBean(targetBeanName);
                // Avoid eager @ControllerAdvice bean resolution for scoped proxies,
                // since attempting to do so during context initialization would result
                // in an exception due to the current absence of the scope. For example,
                // an HTTP request or session scope is not active during initialization.
                if (!isScopedProxy && !ScopedProxyUtils.isScopedTarget(beanName)) {
                    resolvedBean = resolveBean();
                }
            } else {
                resolvedBean = resolveBean();
            }

            if (resolvedBean instanceof Ordered) {
                this.order = ((Ordered) resolvedBean).getOrder();
            } else if (this.beanType != null) {
                this.order = OrderUtils.getOrder(this.beanType, Ordered.LOWEST_PRECEDENCE);
            } else {
                this.order = Ordered.LOWEST_PRECEDENCE;
            }
        }
        return this.order;
    }

    public Class<?> getBeanType() {
        return this.beanType;
    }

    public Object resolveBean() {
        if (this.resolvedBean == null) {
            // this.beanOrName must be a String representing the bean name if
            // this.resolvedBean is null.
            Object resolvedBean = obtainBeanFactory().getBean((String) this.beanOrName);
            // Don't cache non-singletons (e.g., prototypes).
            if (!this.isSingleton) {
                return resolvedBean;
            }
            this.resolvedBean = resolvedBean;
        }
        return this.resolvedBean;
    }

    private BeanFactory obtainBeanFactory() {
        Assert.state(this.beanFactory != null, "No BeanFactory set");
        return this.beanFactory;
    }

    public boolean isApplicableToBeanType(Class<?> beanType) {
        return this.beanTypePredicate.test(beanType);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ControllerAdviceBean)) {
            return false;
        }
        ControllerAdviceBean otherAdvice = (ControllerAdviceBean) other;
        return (this.beanOrName.equals(otherAdvice.beanOrName) && this.beanFactory == otherAdvice.beanFactory);
    }

    @Override
    public int hashCode() {
        return this.beanOrName.hashCode();
    }

    @Override
    public String toString() {
        return this.beanOrName.toString();
    }

}
