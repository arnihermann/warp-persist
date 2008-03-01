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

import com.google.inject.Module;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

import java.lang.reflect.Method;

import net.jcip.annotations.NotThreadSafe;

/**
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 31/05/2007
 * Time: 11:54:45
 * <p>
 *
 * Configures and builds a Module for use in a Guice injector to enable the PersistenceService.
 * see the website for the EDSL binder language.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@NotThreadSafe
class PersistenceServiceBuilderImpl implements SessionStrategyBuilder, PersistenceModuleBuilder, TransactionStrategyBuilder {
    private final PersistenceModule persistenceModule;

    PersistenceServiceBuilderImpl(PersistenceModule persistenceModule) {
        this.persistenceModule = persistenceModule;
    }

    public TransactionStrategyBuilder across(UnitOfWork unitOfWork) {
        persistenceModule.setUnitOfWork(unitOfWork);

        return this;
    }

    public Module buildModule() {
        return persistenceModule;
    }


    public TransactionStrategyBuilder transactedWith(TransactionStrategy transactionStrategy) {
        persistenceModule.setTransactionStrategy(transactionStrategy);

        return this;
    }


    public TransactionStrategyBuilder addAccessor(Class<?> daoInterface) {
        persistenceModule.addAccessor(daoInterface);

        return this;
    }

    public PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher) {
        persistenceModule.setClassMatcher(classMatcher);
        persistenceModule.setMethodMatcher(Matchers.annotatedWith(Transactional.class));

        return this;
    }


    public PersistenceModuleBuilder forAll(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher) {
        persistenceModule.setClassMatcher(classMatcher);
        persistenceModule.setMethodMatcher(methodMatcher);

        return this;
    }
}
