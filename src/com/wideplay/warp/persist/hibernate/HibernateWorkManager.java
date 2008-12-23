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

package com.wideplay.warp.persist.hibernate;

import com.google.inject.Provider;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.WorkManager;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.hibernate.SessionFactory;
import org.hibernate.context.ManagedSessionContext;

/**
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
@Immutable
@ThreadSafe // Thread confinement through ThreadLocal
class HibernateWorkManager implements WorkManager {
    private final Provider<SessionFactory> sessionFactoryProvider;
    private final UnitOfWork unitOfWork;
    private final String annotationDebugString;

    public HibernateWorkManager(Provider<SessionFactory> sessionFactoryProvider, UnitOfWork unitOfWork,
                                String annotationDebugString) {
        this.sessionFactoryProvider = sessionFactoryProvider;
        this.unitOfWork = unitOfWork;
        this.annotationDebugString = annotationDebugString;
    }

    public void beginWork() {
        if (UnitOfWork.TRANSACTION.equals(unitOfWork))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        //do nothing if a session is already open
        if (ManagedSessionContext.hasBind(sessionFactoryProvider.get()))
            return;

        //open session;
        ManagedSessionContext.bind(sessionFactoryProvider.get().openSession());
    }

    public void endWork() {
        if (UnitOfWork.TRANSACTION.equals(unitOfWork))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        //do nothing if there is no session open
        SessionFactory sessionFactory = sessionFactoryProvider.get();
        if (!ManagedSessionContext.hasBind(sessionFactory))
            return;

        //close up session when done
        try {
            sessionFactory.getCurrentSession().close();
        } finally {

            //discard session;
            ManagedSessionContext.unbind(sessionFactory);
        }
    }

    public String toString() {
        return String.format("%s[boundTo: %s, unitOfWork: %s]",super.toString(),
                             this.annotationDebugString, this.unitOfWork);
    }
}
