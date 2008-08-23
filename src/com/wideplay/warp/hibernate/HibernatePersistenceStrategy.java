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

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.wideplay.warp.persist.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author Robbie Vanbrabant
 */
public class HibernatePersistenceStrategy implements PersistenceStrategy {
    public Module getBindings(final PersistenceConfiguration config) {
        return new AbstractPersistenceModule() {
            @Override
            protected void configure() {
                String annotationDebugString = config.getAnnotationDebugStringOrNull();
                // Need instance here for the work manager.
                SessionFactoryProvider sfProvider =
                        new SessionFactoryProvider(getConfigurationKey(), annotationDebugString);
                // Need instance here for the interceptors.
                Provider<Session> sessionProvider = new SessionProvider(sfProvider);
                // Need WorkManager here so we can register it on the SPR filter if the UnitOfWork is REQUEST
                WorkManager workManager = new HibernateWorkManager(sfProvider, config.getUnitOfWork(),
                                                                   annotationDebugString);
                // Needs to be able to initialize Provider<SessionFactory>
                PersistenceService pService = new HibernatePersistenceService(sfProvider);

                if (UnitOfWork.REQUEST == config.getUnitOfWork())
                    SessionFilter.registerWorkManager(workManager);

                bindSpecial(config, SessionFactory.class).toProvider(sfProvider);
                bindSpecial(config, Session.class).toProvider(sessionProvider);
                bindSpecial(config, WorkManager.class).toInstance(workManager);
                bindSpecial(config, PersistenceService.class).toInstance(pService);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported Hibernate transaction strategy: " + config.getTransactionStrategy());
                // IMPORTANT: the user should configure transactions manually when using multiple Hibernate modules.
                MethodInterceptor txInterceptor = new HibernateLocalTxnInterceptor(sessionProvider);
                bindInterceptor(config.getTransactionClassMatcher(),
                                config.getTransactionMethodMatcher(), 
                                txInterceptor);

                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new HibernateFinderInterceptor(sessionProvider);
                bindFinderInterceptor(config, finderInterceptor);
                bindDynamicAccessors(config, finderInterceptor);
            }

            /**
             * Gets the Key to which the Hibernate Configuration has been bound.
             */
            private Key<Configuration> getConfigurationKey() {
                if (config.hasBindingAnnotation()) {
                    return Key.get(Configuration.class, config.getBindingAnnotationClass());
                } else {
                    return Key.get(Configuration.class);
                }
            }
        };
    }
}
