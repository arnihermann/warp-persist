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
package com.wideplay.warp.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Robbie Vanbrabant
 */
public class HibernateConfigurationStrategy implements ConfigurationStrategy {
    public Module getBindings(final Configuration config) {
        return new AbstractModule() {
            protected void configure() {
                // Need instance here for the work manager.
                SessionFactoryProvider sfProvider = new SessionFactoryProvider();
                // Need instance here for the interceptors.
                Provider<Session> sessionProvider = new SessionProvider();
                // Need WorkManager here so we can register it on the SPR filter if the UnitOfWork is REQUEST
                WorkManager workManager = new HibernateWorkManager(sfProvider, config.getUnitOfWork());
                if (UnitOfWork.REQUEST == config.getUnitOfWork())
                    SessionPerRequestFilter.registerWorkManager(workManager);

                bindSpecial(SessionFactory.class).toProvider(sfProvider);
                bindSpecial(Session.class).toProvider(sessionProvider);
                bindSpecial(WorkManager.class).toInstance(workManager);

                bindSpecial(PersistenceService.class).to(HibernatePersistenceService.class).in(Singleton.class);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported Hibernate transaction strategy: " + config.getTransactionStrategy());
                // IMPORTANT: the user should configure transactions manually when using multiple Hibernate modules.
                MethodInterceptor txInterceptor = new HibernateLocalTxnInterceptor(sessionProvider);
                bindInterceptor(config.getTransactionClassMatcher(),
                                config.getTransactionMethodMatcher(), 
                                txInterceptor);

                // TODO Review Dynamic Finders to fit multiple module config.
                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new HibernateFinderInterceptor(sessionProvider);
                DynamicFinders.bindInterceptor(binder(), finderInterceptor);
                DynamicFinders.bindDynamicAccessors(binder(), config.getAccessors(), finderInterceptor);
            }

            /**
             * Bind with an optional binding annotation instance or type, depending on the configuration.
             * A binding annotation needs to be specified when using two Hibernate configuration within the
             * same Injector. Only use this method for bindings exposed to users. Useless otherwise.
             */
            protected <T> com.google.inject.binder.LinkedBindingBuilder<T> bindSpecial(java.lang.Class<T> tClass) {
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
        };
    }
}
