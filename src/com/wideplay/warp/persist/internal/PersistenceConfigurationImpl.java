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

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.*;

import com.wideplay.warp.persist.spi.PersistenceConfiguration;
import com.wideplay.warp.persist.spi.TransactionMatcher;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.Defaults;

/**
 * Value object that indicates how a persistence service should be configured.
 * To create an instance, obtain a builder by calling {@link #builder()}, set
 * all the properties and call {@link PersistenceConfigurationImpl.PersistenceConfigurationBuilder#build()}.
 * 
 * @author Robbie Vanbrabant
 */
@ThreadSafe
@Immutable
class PersistenceConfigurationImpl implements PersistenceConfiguration {
    private final UnitOfWork unitOfWork;
    private final Set<Class<?>> accessors;
    private final List<TransactionMatcher> transactionMatchers;

    private PersistenceConfigurationImpl(PersistenceConfigurationBuilder builder) {
        this.unitOfWork = builder.unitOfWork;
        this.transactionMatchers = Collections.unmodifiableList(builder.transactionMatchers);
        this.accessors = Collections.unmodifiableSet(builder.accessors);
    }

    public UnitOfWork getUnitOfWork() {
        return this.unitOfWork;
    }
    public Set<Class<?>> getDynamicAccessors() {
        return this.accessors;
    }
    public List<TransactionMatcher> getTransactionMatchers() {
        return transactionMatchers;
    }

    public static PersistenceConfigurationBuilder builder() {
        return new PersistenceConfigurationBuilder();
    }

    static class PersistenceConfigurationBuilder {
        // default values
        private UnitOfWork unitOfWork = Defaults.UNIT_OF_WORK;
        private List<TransactionMatcher> transactionMatchers = new ArrayList<TransactionMatcher>();

        private final Set<Class<?>> accessors = new LinkedHashSet<Class<?>>();

        public PersistenceConfigurationBuilder unitOfWork(UnitOfWork unitOfWork) {
            this.unitOfWork = unitOfWork;
            return this;
        }
        public PersistenceConfigurationBuilder transactionMatcher(TransactionMatcher txMatcher) {
            this.transactionMatchers.add(txMatcher);
            return this;
        }
        public PersistenceConfigurationBuilder accessor(Class<?> accessor) {
            accessors.add(accessor);
            return this;
        }

        public PersistenceConfiguration build() {
            return new PersistenceConfigurationImpl(this);
        }
    }

}
