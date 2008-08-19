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

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Immutable
class SessionFactoryProvider implements Provider<SessionFactory> {
    private static final String JTA_USER_TRANSACTION = "jta.UserTransaction";
    private static final Object LOCK = new Object();

    @Inject // injecting finals works and has the same thread safety guarantees as constructors.
    private final Configuration configuration = null;

    // DCL on a volatile
    private volatile SessionFactory sessionFactory = null;

    public SessionFactory get() {
        if (sessionFactory == null) {
            synchronized (LOCK) {
                if (sessionFactory == null) {
                    sessionFactory = configuration.buildSessionFactory();
                }
            }
        }

        // TODO (Robbie) Dhanji, do we really need this legacy stuff?
        //if necessary, set the JNDI lookup name of the JTA txn
        HibernateJtaTxnInterceptor.setUserTransactionJndiName(configuration.getProperty(JTA_USER_TRANSACTION));

        return sessionFactory;
    }    
}
