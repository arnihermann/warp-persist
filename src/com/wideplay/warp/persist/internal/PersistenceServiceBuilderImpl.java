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

package com.wideplay.warp.persist.internal;

import com.google.inject.Module;
import com.google.inject.matcher.Matcher;
import com.wideplay.warp.persist.spi.PersistenceModuleVisitor;
import com.wideplay.warp.persist.*;
import com.wideplay.warp.persist.internal.HasPersistenceStrategy;
import com.wideplay.warp.persist.internal.PersistenceConfigurationImpl;
import com.wideplay.warp.persist.internal.PersistenceFlavor;
import com.wideplay.warp.persist.spi.TransactionMatcher;
import com.wideplay.warp.persist.spi.PersistenceModule;
import net.jcip.annotations.NotThreadSafe;

import java.lang.reflect.Method;

/**
 * Configures and builds a Module for use in a Guice injector to enable the PersistenceService.
 * see the website for the EDSL binder language.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@NotThreadSafe
public class PersistenceServiceBuilderImpl implements SessionStrategyBuilder, PersistenceModuleBuilder, TransactionStrategyBuilder {
    private final PersistenceConfigurationImpl.PersistenceConfigurationBuilder persistenceConfiguration = PersistenceConfigurationImpl.builder();
    private final HasPersistenceStrategy flavor;

    public PersistenceServiceBuilderImpl(PersistenceFlavor flavor) {
        this.flavor = flavor;
    }

    public PersistenceServiceBuilderImpl(final PersistenceStrategy persistenceStrategy) {
        this.flavor = new HasPersistenceStrategy() {
            public PersistenceStrategy getPersistenceStrategy() {
                return persistenceStrategy;
            }
        };
    }

    public TransactionStrategyBuilder across(UnitOfWork unitOfWork) {
        persistenceConfiguration.unitOfWork(unitOfWork);

        return this;
    }

    public Module buildModule() {
        PersistenceModule bindings = flavor.getPersistenceStrategy().getBindings(persistenceConfiguration.build());
        bindings.visit(new PersistenceModuleVisitor() {
            public void publishWorkManager(WorkManager wm) {
                 PersistenceFilter.registerWorkManager(wm);
            }
            public void publishPersistenceService(PersistenceService persistenceService) {
                PersistenceFilter.registerPersistenceService(persistenceService);
            }
        });
        return bindings;
    }

    public TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy) {
        persistenceConfiguration.transactionStrategy(transactionStrategy);

        return this;
    }


    public TransactionStrategyBuilder addAccessor(Class<?> daoInterface) {
        persistenceConfiguration.accessor(daoInterface);

        return this;
    }

    public TransactionStrategyBuilder forAll(Matcher<? super Class<?>> classMatcher) {
        persistenceConfiguration.transactionMatcher(new TransactionMatcher(classMatcher));

        return this;
    }

    public TransactionStrategyBuilder forAll(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher) {
        persistenceConfiguration.transactionMatcher(new TransactionMatcher(classMatcher, methodMatcher));

        return this;
    }
}
