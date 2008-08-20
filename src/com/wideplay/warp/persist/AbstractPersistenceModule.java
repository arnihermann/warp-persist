package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.cglib.proxy.Proxy;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * Base module for persistence strategies that holds a bunch
 * of utility methods for easier configuration.
 * @author Robbie Vanbrabant
 */
public abstract class AbstractPersistenceModule extends AbstractModule {
    protected abstract void configure();

    /**
     * Bind with an optional binding annotation instance or type, depending on the configuration.
     * A binding annotation needs to be specified when using two Hibernate configurations within the
     * same Injector.
     */
    protected <T> com.google.inject.binder.LinkedBindingBuilder<T> bindSpecial(PersistenceConfiguration config, java.lang.Class<T> tClass) {
        if (config.hasBindingAnnotation()) {
            if (config.getBindingAnnotationClass() != null) {
                return super.bind(tClass).annotatedWith(config.getBindingAnnotationClass());
            } else {
                // we know it's not null because of hasBindingAnnotation
                return super.bind(tClass).annotatedWith(config.getBindingAnnotation());
            }
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
}
