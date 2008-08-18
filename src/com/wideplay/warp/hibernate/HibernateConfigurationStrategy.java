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
                // Set up Hibernate.
                bind(SessionFactoryHolder.class).in(Singleton.class);
                bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
                bind(Session.class).toProvider(SessionProvider.class);
                bind(PersistenceService.class).to(HibernatePersistenceService.class).in(Singleton.class);
                bind(WorkManager.class).to(HibernateWorkManager.class);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported Hibernate transaction strategy: " + config.getTransactionStrategy());
                HibernateLocalTxnInterceptor.setUnitOfWork(config.getUnitOfWork());
                bindInterceptor(config.getTransactionClassMatcher(),
                                config.getTransactionMethodMatcher(), 
                                new HibernateLocalTxnInterceptor());

                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new HibernateFinderInterceptor();
                DynamicFinders.bindInterceptor(binder(), finderInterceptor);
                DynamicFinders.bindDynamicAccessors(binder(), config.getAccessors(), finderInterceptor);
            }
        };
    }
}
