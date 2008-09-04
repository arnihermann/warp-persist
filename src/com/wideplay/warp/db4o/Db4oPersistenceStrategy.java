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
package com.wideplay.warp.db4o;

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.*;

/**
 * @author Robbie Vanbrabant
 */
public class Db4oPersistenceStrategy implements PersistenceStrategy {
    public PersistenceModule getBindings(final PersistenceConfiguration config) {
        return new AbstractPersistenceModule(null) {
            protected void configure() {
                // Set up Db4o.
                bind(ObjectServerHolder.class).in(Singleton.class);
		        bind(ObjectServer.class).toProvider(ObjectServerProvider.class);
		        bind(ObjectContainer.class).toProvider(ObjectContainerProvider.class);
		        bind(PersistenceService.class).to(Db4oPersistenceService.class).in(Singleton.class);
		        bind(WorkManager.class).to(Db4oWorkManager.class);

                // Set up transactions. Only local transactions are supported.
                if (TransactionStrategy.LOCAL != config.getTransactionStrategy())
                    throw new IllegalArgumentException("Unsupported Db4o transaction strategy: " + config.getTransactionStrategy());
                Db4oLocalTxnInterceptor.setUnitOfWork(config.getUnitOfWork());
                bindInterceptor(config.getTransactionClassMatcher(),
                                config.getTransactionMethodMatcher(),
                                new Db4oLocalTxnInterceptor());

                // No Dynamic Finders yet.
            }

            public WorkManager getWorkManager() {
                return null;
            }

            public PersistenceService getPersistenceService() {
                return null;
            }
        };

    }
}
