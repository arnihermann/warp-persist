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

import org.hibernate.SessionFactory;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 31/05/2007
 * Time: 15:26:06
 * <p>
 *
 * A placeholder that frees me from having to use statics to make a singleton session factory,
 * so I can use per-injector singletons vs. per JVM/classloader singletons.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@ThreadSafe
class SessionFactoryHolder {
    @GuardedBy("setSessionFactory()") //BUT open for concurrent reads, so is volatile
    private volatile SessionFactory sessionFactory;

    //A hack to provide the session factory statically to non-guice objects (interceptors), that can be thrown away come guice1.1
    private static volatile SessionFactoryHolder singletonSessionFactoryHolder;

    //store singleton
    public SessionFactoryHolder() {
        singletonSessionFactoryHolder = this;
    }

    SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    synchronized void setSessionFactory(SessionFactory sessionFactory) {
        if (null != this.sessionFactory)
            throw new RuntimeException("Duplicate session factory creation! Only one session factory is allowed per injector");
        
        this.sessionFactory = sessionFactory;
    }

    static SessionFactory getCurrentSessionFactory() {
        final SessionFactory sessionFactory = singletonSessionFactoryHolder.getSessionFactory();

        if (null == sessionFactory) {
            throw new RuntimeException("No SessionFactory was found. Did you remember to call " +
                    "PersistenceService.start() *before* using the EntityManager? In servlet environments, this is typically " +
                    "done in the init() lifecycle method of a servlet (or equivalent webapp initialization scheme).");
        }
        return sessionFactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! (o instanceof SessionFactoryHolder)) return false;

        SessionFactoryHolder that = (SessionFactoryHolder) o;

        return (sessionFactory == null ? that.sessionFactory == null : sessionFactory.equals(that.sessionFactory));

    }

    @Override
    public int hashCode() {
        return (sessionFactory != null ? sessionFactory.hashCode() : 0);
    }
}
