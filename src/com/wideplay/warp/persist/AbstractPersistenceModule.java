package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.cglib.proxy.Proxy;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import com.wideplay.warp.persist.dao.Finder;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * Base module for persistence strategies that holds a bunch
 * of utility methods for easier configuration.
 * @author Robbie Vanbrabant
 */
public abstract class AbstractPersistenceModule extends AbstractModule {
    protected abstract void configure();

    /**
     * Bind with an optional binding annotation type. A binding annotation needs
     * to be specified when using two Hibernate configurations within the same Injector.
     */
    protected <T> com.google.inject.binder.LinkedBindingBuilder<T> bindSpecial(PersistenceConfiguration config, java.lang.Class<T> tClass) {
        if (config.hasBindingAnnotation()) {
            return super.bind(tClass).annotatedWith(config.getBindingAnnotationClass());
        } else {
            return super.bind(tClass);
        }
    }
    
    protected void bindDynamicAccessors(PersistenceConfiguration config, MethodInterceptor finderInterceptor) {
        for (Class accessor : config.getAccessors()) {
            if (accessor.isInterface()) {
                bindSpecial(config, accessor).toInstance(Proxy.newProxyInstance(accessor.getClassLoader(),
                        new Class<?>[] { accessor }, new AopAllianceJdkProxyAdapter(finderInterceptor)));
            } else {
                //use cglib adapter to subclass the accessor (this lets us intercept abstract classes)
                bindSpecial(config, accessor).toInstance(com.google.inject.cglib.proxy.Enhancer.create(accessor,
                        new AopAllianceCglibAdapter(finderInterceptor)));
            }
        }
    }

    /**
     * Binds a finder interceptor with support for multiple modules. When the user specifies
     * an annotation to bind the module to, we match on {@code @Finder(unit=UserAnnotation.class)}.
     */
    protected void bindFinderInterceptor(PersistenceConfiguration config, MethodInterceptor finderInterceptor) {
        if (config.hasBindingAnnotation()) {
            bindInterceptor(any(), finderWithUnitIdenticalTo(config.getBindingAnnotationClass()), finderInterceptor);
        } else {
            bindInterceptor(any(), annotatedWith(Finder.class), finderInterceptor);
        }
    }

    private Matcher<Method> finderWithUnitIdenticalTo(final Class<?> annotation) {
        return new AbstractMatcher<Method>() {
            public boolean matches(Method method) {
                return annotatedWith(Finder.class).matches(method) &&
                       method.getAnnotation(Finder.class).unit() == annotation;
            }
        };
    }
}
