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
package com.wideplay.warp.jpa;

import com.google.inject.Key;
import com.google.inject.Module;
import com.wideplay.warp.persist.*;
import org.aopalliance.intercept.MethodInterceptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * @author Robbie Vanbrabant
 */
public class JpaPersistenceStrategy implements PersistenceStrategy {
    public Module getBindings(final PersistenceConfiguration config) {
        return new AbstractPersistenceModule() {
            protected void configure() {
                // TODO (Robbie) keys for multiple modules
                EntityManagerFactoryProvider emfProvider = new EntityManagerFactoryProvider(getPersistenceUnitKey(),
                                                                                            getExtraPersistencePropertiesKey());
                EntityManagerProvider emProvider = new EntityManagerProvider(emfProvider);
                WorkManager workManager = new JpaWorkManager(emfProvider);
                JpaPersistenceService pService = new JpaPersistenceService(emfProvider);

                // Set up JPA.
                bindSpecial(config, EntityManagerFactory.class).toProvider(emfProvider);
                bindSpecial(config, EntityManager.class).toProvider(emProvider);
                bindSpecial(config, PersistenceService.class).toInstance(pService);
                bindSpecial(config, JpaPersistenceService.class).toInstance(pService);
                bindSpecial(config, WorkManager.class).toInstance(workManager);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported JPA transaction strategy: " + config.getTransactionStrategy());

                bindTransactionInterceptor(config, new JpaLocalTxnInterceptor(emfProvider, emProvider, config.getUnitOfWork()));

                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new JpaFinderInterceptor(emProvider);
                bindFinderInterceptor(config, finderInterceptor);
                bindDynamicAccessors(config, finderInterceptor);
                
                if (UnitOfWork.REQUEST == config.getUnitOfWork()) {
                    // statics -- we don't have a choice.
                    SessionFilter.registerWorkManager(workManager);
                    LifecycleSessionFilter.registerPersistenceService(pService);
                }
            }

            private Key<String> getPersistenceUnitKey() {
                if (config.hasBindingAnnotation()) {
                    return Key.get(String.class, JpaUnitInstance.of(config.getBindingAnnotationClass()));
                } else {
                    return Key.get(String.class, JpaUnit.class);
                }
            }

            private Key<Properties> getExtraPersistencePropertiesKey() {
                if (config.hasBindingAnnotation()) {
                    return Key.get(Properties.class, JpaUnitInstance.of(config.getBindingAnnotationClass()));
                } else {
                    return Key.get(Properties.class, JpaUnit.class);
                }
            }
        };
    }
}
