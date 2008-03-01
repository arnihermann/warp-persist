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

import org.aopalliance.intercept.MethodInterceptor;

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.WorkManager;
import net.jcip.annotations.Immutable;

/**
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 */
@Immutable
public final class Db4oBindingSupport {

	private Db4oBindingSupport() {}

	public static void addBindings(Binder binder) {
		binder.bind(ObjectServerHolder.class).in(Singleton.class);
		binder.bind(ObjectServer.class).toProvider(ObjectServerProvider.class);
		binder.bind(ObjectContainer.class).toProvider(ObjectContainerProvider.class);

		binder.bind(PersistenceService.class).to(Db4oPersistenceService.class).in(Singleton.class);
		binder.bind(WorkManager.class).to(Db4oWorkManager.class);
	}

	public static void setUnitOfWork(UnitOfWork unitOfWork) {
		Db4oLocalTxnInterceptor.setUnitOfWork(unitOfWork);
        SessionPerRequestFilter.setUnitOfWork(unitOfWork);
    }

	public static MethodInterceptor getInterceptor(TransactionStrategy transactionStrategy) {
		switch (transactionStrategy) {
			case LOCAL: return new Db4oLocalTxnInterceptor();
		}

		throw new IllegalArgumentException("No such transaction strategy known: " + transactionStrategy);
	}
}
