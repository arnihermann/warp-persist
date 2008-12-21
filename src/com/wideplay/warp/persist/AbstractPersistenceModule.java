/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import com.wideplay.warp.persist.dao.Finder;
import com.wideplay.warp.util.WarpPersistNamingPolicy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Proxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base module for persistence strategies that holds a bunch
 * of utility methods for easier configuration.
 * @author Robbie Vanbrabant
 */
public abstract class AbstractPersistenceModule extends AbstractModule implements PersistenceModule {
    private final Class<? extends Annotation> annotation;

    protected AbstractPersistenceModule(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    protected abstract void configure();

    /**
     * Bind with an optional binding annotation type. A binding annotation needs
     * to be specified when using two Hibernate configurations within the same Injector.
     */
    protected <T> com.google.inject.binder.LinkedBindingBuilder<T> bindSpecial(java.lang.Class<T> tClass) {
        if (inMultiModulesMode()) {
            return super.bind(tClass).annotatedWith(annotation);
        } else {
            return super.bind(tClass);
        }
    }

    @SuppressWarnings("unchecked") // Proxies are not generic.
    private void bindDynamicAccessors(PersistenceConfiguration config, MethodInterceptor finderInterceptor,
                                      MethodInterceptor transactionalFinderInterceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(new WarpPersistNamingPolicy());

        for (Class accessor : config.getAccessors()) {
            if (accessor.isInterface()) {
                for (Method method : accessor.getMethods()) {
                    Finder finder = method.getAnnotation(Finder.class);
                    if (finder == null) {
                        addError(method + " has been specified as a Dynamic Accessor but does not have the @Finder annotation.");
                    } else {
                        validateFinder(finder, method);
                    }
                    validateTransactional(method);
                }
                MethodInterceptor interceptorToUse =
                        determineDynamicAccessorInterceptor(config, finderInterceptor, transactionalFinderInterceptor,
                                accessor);
                bindSpecial(accessor).toInstance(Proxy.newProxyInstance(accessor.getClassLoader(),
                        new Class<?>[] { accessor }, new AopAllianceJdkProxyAdapter(interceptorToUse)));
            } else {
                for (Method method : accessor.getMethods()) {
                    validateFinder(method.getAnnotation(Finder.class), method);
                    validateTransactional(method);
                }
                MethodInterceptor interceptorToUse =
                    determineDynamicAccessorInterceptor(config, finderInterceptor, transactionalFinderInterceptor,
                            accessor);

                //use cglib adapter to subclass the accessor (this lets us intercept abstract classes)
                enhancer.setCallback(new AopAllianceCglibAdapter(interceptorToUse));
                enhancer.setSuperclass(accessor);
                bindSpecial(accessor).toInstance(enhancer.create());
            }
        }
    }

    private MethodInterceptor determineDynamicAccessorInterceptor(PersistenceConfiguration config,
                                                                  MethodInterceptor finderInterceptor,
                                                                  MethodInterceptor transactionalFinderInterceptor,
                                                                  Class<?> accessor) {
        boolean matches = false;
        for (TransactionMatcher matcher : config.getTransactionMatchers()) {
            matches |= matcher.getTxClassMatcher().matches(accessor);
        }
        return matches ? transactionalFinderInterceptor : finderInterceptor;
    }

    private void validateFinder(Finder finder, Method method) {
        if (finder != null && finder.unit() == Defaults.DefaultUnit.class && inMultiModulesMode()) {
             addError(String.format("%s is a Dynamic Finder but does not have the unit annotation '%s'. " +
                     "Specify as @Finder(unit=%s.class)", method, this.annotation, this.annotation.getSimpleName()));
        }
    }

    private void validateTransactional(Method method) {
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional != null && transactional.unit() == Defaults.DefaultUnit.class &&
                inMultiModulesMode()) {
             addError(String.format("%s is @Transactional but does not have the unit annotation '%s'. " +
                     "Specify as @Transactional(unit=%s.class)", method, this.annotation, this.annotation.getSimpleName()));
        }
    }

    protected void bindTransactionalDynamicAccessors(final PersistenceConfiguration config,
                                                     final MethodInterceptor finderInterceptor,
                                                     final MethodInterceptor txInterceptor) {
        MethodInterceptor transactionalFinderInterceptor = new MethodInterceptor() {
            private ConcurrentMap<Method, Boolean> matcherCache = new ConcurrentHashMap<Method, Boolean>();
            public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
                // Don't care about a theoretical extra write, so we don't lock
                if (!matcherCache.containsKey(methodInvocation.getMethod())) {
                    boolean matches = false;
                    for (TransactionMatcher matcher : config.getTransactionMatchers()) {
                        matches |= matcher.getTxMethodMatcher().matches(methodInvocation.getMethod());
                    }
                    matcherCache.putIfAbsent(methodInvocation.getMethod(), matches);
                }
                
                if (matcherCache.get(methodInvocation.getMethod())) {
                    return txInterceptor.invoke(new MethodInvocation() {
                        public Object[] getArguments() {
                            return methodInvocation.getArguments();
                        }
                        public Method getMethod() {
                            return methodInvocation.getMethod();
                        }
                        public Object proceed() throws Throwable {
                            return finderInterceptor.invoke(methodInvocation);
                        }
                        public Object getThis() {
                            return methodInvocation.getThis();
                        }
                        public AccessibleObject getStaticPart() {
                            return methodInvocation.getStaticPart();
                        }
                    });
                } else {
                    return finderInterceptor.invoke(methodInvocation);
                }
            }
        };
        bindDynamicAccessors(config, finderInterceptor, transactionalFinderInterceptor);
    }

    protected void bindTransactionInterceptor(PersistenceConfiguration config, MethodInterceptor txInterceptor) {
        if (inMultiModulesMode()) {
            // We support forAll, and assume the user knows what he/she is doing.
            if (config.getTransactionMatchers().size() > 0) {
                for (TransactionMatcher matcher : config.getTransactionMatchers()) {
                    bindInterceptor(matcher.getTxClassMatcher(), matcher.getTxMethodMatcher(), txInterceptor);
                }
            } else {
                TransactionMatcher matcher = new TransactionMatcher();
                bindInterceptor(matcher.getTxClassMatcher(),
                                PersistenceMatchers.transactionalWithUnit(annotation),
                                txInterceptor);
            }
        } else {
            if (config.getTransactionMatchers().size() > 0) {
                for (TransactionMatcher matcher : config.getTransactionMatchers()) {
                    bindInterceptor(matcher.getTxClassMatcher(), matcher.getTxMethodMatcher(), txInterceptor);
                }
            } else {
                TransactionMatcher matcher = new TransactionMatcher();
                bindInterceptor(matcher.getTxClassMatcher(), matcher.getTxMethodMatcher(), txInterceptor);
            }
        }
    }

    /**
     * Binds a finder interceptor with support for multiple modules. When the user specifies
     * an annotation to bind the module to, we match on {@code @Finder(unit=UserAnnotation.class)}.
     */
    protected void bindFinderInterceptor(MethodInterceptor finderInterceptor) {
        if (inMultiModulesMode()) {
            bindInterceptor(any(), PersistenceMatchers.finderWithUnit(annotation), finderInterceptor);
        } else {
            bindInterceptor(any(), annotatedWith(Finder.class), finderInterceptor);
        }
    }

    protected boolean inMultiModulesMode() {
        return this.annotation != null;
    }

    protected boolean unitOfWorkRequest(PersistenceConfiguration config) {
        return UnitOfWork.REQUEST == config.getUnitOfWork();
    }

    protected <T> Key<T> key(Class<T> clazz) {
        if (annotation != null) {
            return Key.get(clazz, annotation);
        }
        return Key.get(clazz);
    }
}
