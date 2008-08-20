package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.cglib.proxy.Proxy;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import com.wideplay.warp.persist.dao.Finder;
import org.aopalliance.intercept.MethodInterceptor;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

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

    /**
     * Binds a finder interceptor with support for multiple modules. When the user specifies
     * an annotation to bind the module to, we match on the combination {@code @UserAnnotation @Finder}.
     */
    protected void bindFinderInterceptor(PersistenceConfiguration config, MethodInterceptor finderInterceptor) {
        Matcher<AnnotatedElement> annotatedWithFinder = annotatedWith(Finder.class);
        if (config.hasBindingAnnotation()) {
            if (config.getBindingAnnotationClass() != null) {
                bindInterceptor(any(), and(annotatedWithFinder, annotatedWith(config.getBindingAnnotationClass())), finderInterceptor);
            } else {
                // we know it's not null because of hasBindingAnnotation
                bindInterceptor(any(), and(annotatedWithFinder, annotatedWith(config.getBindingAnnotation())), finderInterceptor);
            }
        } else {
            bindInterceptor(any(), annotatedWithFinder, finderInterceptor);
        }
    }

    /**
     * Matches on exclusive AND for the given matchers.
     */
    protected <T> Matcher<T> and(final Matcher<? super T> one, final Matcher<? super T> two) {
      return new And<T>(one, two);
    }

    static class And<T> extends AbstractMatcher<T> implements Serializable {
        private final Matcher<? super T> one;
        private final Matcher<? super T> two;

        public And(Matcher<? super T> one, Matcher<? super T> two) {
            this.one = one;
            this.two = two;
        }
        public boolean matches(T t) {
            return one.matches(t) && two.matches(t);
        }

        @Override public boolean equals(Object other) {
          return other instanceof And
              && ((And) other).one.equals(one)
              && ((And) other).two.equals(two);
        }

        @Override public int hashCode() {
          int result = 17;
          result = 37*result + one.hashCode();
          result = 37*result + two.hashCode();
          return result;
        }

        @Override public String toString() {
          return "and(" + one + ", " + two + ")";
        }
    }
}
