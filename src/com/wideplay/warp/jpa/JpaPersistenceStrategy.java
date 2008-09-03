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
import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * @author Robbie Vanbrabant
 */
public class JpaPersistenceStrategy implements PersistenceStrategy {
    private final Properties jpaProperties;
    private final String unit;
    private final Class<? extends Annotation> annotation;
    
    private JpaPersistenceStrategy(JpaPersistenceStrategyBuilder builder) {
        this.jpaProperties = builder.jpaProperties;
        this.unit = builder.unit;
        this.annotation = builder.annotation;
    }

    public Module getBindings(final PersistenceConfiguration config) {
        return new AbstractPersistenceModule(annotation) {
            protected void configure() {
                EntityManagerFactoryProvider emfProvider = new EntityManagerFactoryProvider(getPersistenceUnitKey(),
                                                                                            getExtraPersistencePropertiesKey());
                EntityManagerProvider emProvider = new EntityManagerProvider(emfProvider);
                WorkManager workManager = new JpaWorkManager(emfProvider);
                JpaPersistenceService pService = new JpaPersistenceService(emfProvider);

                // Set up JPA.
                bindSpecial(EntityManagerFactory.class).toProvider(emfProvider);
                bindSpecial(EntityManager.class).toProvider(emProvider);
                bindSpecial(PersistenceService.class).toInstance(pService);
                bindSpecial(JpaPersistenceService.class).toInstance(pService);
                bindSpecial(WorkManager.class).toInstance(workManager);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported JPA transaction strategy: " + config.getTransactionStrategy());

                bindTransactionInterceptor(config, new JpaLocalTxnInterceptor(emfProvider, emProvider, config.getUnitOfWork()));

                // Set up Dynamic Finders.
                MethodInterceptor finderInterceptor = new JpaFinderInterceptor(emProvider);
                bindFinderInterceptor(finderInterceptor);
                bindDynamicAccessors(config.getAccessors(), finderInterceptor);
                
                if (UnitOfWork.REQUEST == config.getUnitOfWork()) {
                    // statics -- we don't have a choice.
                    SessionFilter.registerWorkManager(workManager);
                    LifecycleSessionFilter.registerPersistenceService(pService);
                }
            }

            private Key<String> getPersistenceUnitKey() {
                if (!inMultiModulesMode()) {
                    return Key.get(String.class, JpaUnit.class);
                } else {
                    Key<String> key = Key.get(String.class, JpaUnitInstance.of(annotation));
                    bind(key).toInstance(unit);
                    return key;
                }
            }

            private Key<Properties> getExtraPersistencePropertiesKey() {
                if (!inMultiModulesMode()) {
                    return Key.get(Properties.class, JpaUnit.class);
                } else {
                    Key<Properties> key = Key.get(Properties.class, JpaUnitInstance.of(JpaPersistenceStrategy.this.annotation));
                    bind(key).toInstance(jpaProperties);
                    return key;
                }
            }
        };
    }

    public static JpaPersistenceStrategyBuilder builder() {
        return new JpaPersistenceStrategyBuilder();
    }

    public static class JpaPersistenceStrategyBuilder {
        private Properties jpaProperties;
        private String unit;
        private Class<? extends Annotation> annotation;

        public JpaPersistenceStrategyBuilder properties(Properties jpaProperties) {
            this.jpaProperties = jpaProperties;
            return this;
        }

        public JpaPersistenceStrategyBuilder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public JpaPersistenceStrategyBuilder annotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        public JpaPersistenceStrategy build() {
            return new JpaPersistenceStrategy(this);
        }
    }
}
