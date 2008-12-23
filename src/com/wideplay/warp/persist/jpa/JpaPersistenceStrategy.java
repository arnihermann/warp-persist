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
package com.wideplay.warp.persist.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.wideplay.warp.persist.*;
import org.aopalliance.intercept.MethodInterceptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
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

    public PersistenceModule getBindings(final PersistenceConfiguration config) {
        return new JpaPersistenceModule(config);
    }

    class JpaPersistenceModule extends AbstractPersistenceModule {
        private final PersistenceConfiguration config;
        private final EntityManagerFactoryProvider emfProvider;
        private final EntityManagerProvider emProvider;
        private final WorkManager workManager;
        private final JpaPersistenceService pService;
        // needed for bindings created in the constructor.
        private final List<Module> scheduledBindings = new ArrayList<Module>();
        private InternalWorkManager<EntityManager> internalWm;

        private JpaPersistenceModule(PersistenceConfiguration configuration) {
            super(configuration, annotation);
            this.config = configuration;
            this.emfProvider = new EntityManagerFactoryProvider(getPersistenceUnitKey(),
                                                                getExtraPersistencePropertiesKey());
            internalWm = new JpaInternalWorkManager(emfProvider);
            this.emProvider = new EntityManagerProvider(internalWm);
            this.workManager = new JpaWorkManager(internalWm);
            this.pService = new JpaPersistenceService(emfProvider);
        }
        
        protected void configure() {
            for (Module m : scheduledBindings) install(m);
            // Set up JPA.
            bindWithUnitAnnotation(EntityManagerFactory.class).toProvider(emfProvider);
            bindWithUnitAnnotation(EntityManager.class).toProvider(emProvider);
            bindWithUnitAnnotation(PersistenceService.class).toInstance(pService);
            bindWithUnitAnnotation(JpaPersistenceService.class).toInstance(pService);
            bindWithUnitAnnotation(WorkManager.class).toInstance(workManager);

            MethodInterceptor txInterceptor = new JpaLocalTxnInterceptor(this.internalWm, config.getUnitOfWork());
            bindTransactionInterceptor(txInterceptor);

            // Set up Dynamic Finders.
            MethodInterceptor finderInterceptor = new JpaFinderInterceptor(emProvider);
            bindFinderInterceptor(finderInterceptor);
            bindTransactionalDynamicAccessors(finderInterceptor, txInterceptor);
        }

        private Key<String> getPersistenceUnitKey() {
            if (!inMultiModulesMode()) {
                return Key.get(String.class, JpaUnit.class);
            } else {
                final Key<String> key = Key.get(String.class, JpaUnitInstance.of(annotation));
                if (unit != null) {
                    scheduledBindings.add(new AbstractModule() {
                        protected void configure() {
                            bind(key).toInstance(unit);
                        }
                    });
                }
                return key;
            }
        }

        private Key<Properties> getExtraPersistencePropertiesKey() {
            if (!inMultiModulesMode()) {
                return Key.get(Properties.class, JpaUnit.class);
            } else {
                final Key<Properties> key = Key.get(Properties.class, JpaUnitInstance.of(JpaPersistenceStrategy.this.annotation));
                if (jpaProperties != null) {
                    scheduledBindings.add(new AbstractModule() {
                        protected void configure() {
                            bind(key).toInstance(jpaProperties);
                        }
                    });
                }
                return key;
            }
        }

        public void visit(PersistenceModuleVisitor visitor) {
            if (unitOfWorkRequest()) {
                visitor.publishWorkManager(this.workManager);
                visitor.publishPersistenceService(this.pService);
            }
        }
    }

    public static JpaPersistenceStrategyBuilder builder() {
        return new JpaPersistenceStrategyBuilder();
    }

    public static class JpaPersistenceStrategyBuilder implements PersistenceStrategyBuilder<JpaPersistenceStrategy> {
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
