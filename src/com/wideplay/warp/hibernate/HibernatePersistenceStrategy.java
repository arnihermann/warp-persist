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
import com.google.inject.Provider;
import com.wideplay.warp.persist.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Annotation;

/**
 * @author Robbie Vanbrabant
 */
public class HibernatePersistenceStrategy implements PersistenceStrategy {
    private final Configuration configuration;
    private final Class<? extends Annotation> annotation;

    private HibernatePersistenceStrategy(HibernatePersistenceStrategyBuilder builder) {
        this.configuration = builder.configuration;
        this.annotation = builder.annotation;
    }

    public PersistenceModule getBindings(final PersistenceConfiguration config) {
        return new AbstractPersistenceModule(annotation) {
            @Override
            protected void configure() {
                // TODO move to super class?
                String annotationDebugString = annotation!=null?annotation.getSimpleName():"";
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

                bindSpecial(SessionFactory.class).toProvider(sfProvider);
                bindSpecial(Session.class).toProvider(sessionProvider);
                bindSpecial(WorkManager.class).toInstance(workManager);
                bindSpecial(PersistenceService.class).toInstance(pService);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported Hibernate transaction strategy: " + config.getTransactionStrategy());
                MethodInterceptor txInterceptor = new HibernateLocalTxnInterceptor(sessionProvider);
                bindTransactionInterceptor(config, txInterceptor);

                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new HibernateFinderInterceptor(sessionProvider);
                bindFinderInterceptor(finderInterceptor);
                bindDynamicAccessors(config.getAccessors(), finderInterceptor);

                if (UnitOfWork.REQUEST == config.getUnitOfWork()) {
                    // statics -- we don't have a choice.
                    SessionFilter.registerWorkManager(workManager);
                    LifecycleSessionFilter.registerPersistenceService(pService);
                }
            }

            /**
             * Gets the Key to which the Hibernate Configuration has been bound.
             */
            private Key<Configuration> getConfigurationKey() {
                Key<Configuration> key = key(Configuration.class);
                if (inMultiModulesMode()) bind(key).toInstance(configuration);
                return key;
            }

            public WorkManager getWorkManager() {
                return null;
            }

            public PersistenceService getPersistenceService() {
                return null;
            }
        };
    }

    public static HibernatePersistenceStrategyBuilder builder() {
        return new HibernatePersistenceStrategyBuilder();
    }

    public static class HibernatePersistenceStrategyBuilder {
        private Configuration configuration;
        private Class<? extends Annotation> annotation;

        public HibernatePersistenceStrategyBuilder configuration(Configuration config) {
            this.configuration = config;
            return this;
        }
        public HibernatePersistenceStrategyBuilder annotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }
        public HibernatePersistenceStrategy build() {
            return new HibernatePersistenceStrategy(this);
        }
    }
}
