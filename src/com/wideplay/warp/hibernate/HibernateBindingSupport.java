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

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.WorkManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Immutable
public final class HibernateBindingSupport {
    private HibernateBindingSupport() {
    }

    public static void addBindings(Binder binder) {

        binder.bind(SessionFactoryHolder.class).in(Singleton.class);

        binder.bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
        binder.bind(Session.class).toProvider(SessionProvider.class);

        binder.bind(PersistenceService.class).to(HibernatePersistenceService.class).in(Singleton.class);
        binder.bind(WorkManager.class).to(HibernateWorkManager.class);
    }

    public static MethodInterceptor getInterceptor(TransactionStrategy transactionStrategy) {
        switch (transactionStrategy) {
            case LOCAL:
                return new HibernateLocalTxnInterceptor();
//            case JTA:
//                return new HibernateJtaTxnInterceptor();
        }

        throw new IllegalArgumentException("No such transaction strategy known: " + transactionStrategy);
    }

    public static MethodInterceptor getFinderInterceptor() {
        return new HibernateFinderInterceptor();
    }

    public static void setUnitOfWork(UnitOfWork unitOfWork) {
        HibernateLocalTxnInterceptor.setUnitOfWork(unitOfWork);
    }
}
