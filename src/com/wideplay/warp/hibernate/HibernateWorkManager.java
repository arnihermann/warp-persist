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

import com.wideplay.warp.persist.WorkManager;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.SessionFactory;
import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 8, 2007
 * Time: 7:30:05 AM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
@Immutable
class HibernateWorkManager implements WorkManager {


    public void beginWork() {
        if (UnitOfWork.TRANSACTION.equals(SessionPerRequestFilter.getUnitOfWork()))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        //do nothing if a session is already open
        if (ManagedSessionContext.hasBind(SessionFactoryHolder.getCurrentSessionFactory()))
            return;

        //open session;
        ManagedSessionContext.bind(SessionFactoryHolder.getCurrentSessionFactory().openSession());
    }

    public void endWork() {
        if (UnitOfWork.TRANSACTION.equals(SessionPerRequestFilter.getUnitOfWork()))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        //do nothing if there is no session open
        SessionFactory sessionFactory = SessionFactoryHolder.getCurrentSessionFactory();
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
}
