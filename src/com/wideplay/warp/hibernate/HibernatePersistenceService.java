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

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.wideplay.warp.persist.PersistenceService;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Immutable
class HibernatePersistenceService extends PersistenceService {
    private final SessionFactoryHolder sessionFactoryHolder;
    private final Configuration configuration;

    private static final String JTA_USER_TRANSACTION = "jta.UserTransaction";

    @Inject
    public HibernatePersistenceService(SessionFactoryHolder sessionFactoryHolder, Configuration configuration) {
        this.sessionFactoryHolder = sessionFactoryHolder;
        this.configuration = configuration;
    }

    public void start() {
        sessionFactoryHolder.setSessionFactory(configuration.buildSessionFactory());

        //if necessary, set the JNDI lookup name of the JTA txn
        HibernateJtaTxnInterceptor.setUserTransactionJndiName(configuration.getProperty(JTA_USER_TRANSACTION));
    }

    @BindingAnnotation
    @Retention(RUNTIME)
    public @interface PersistenceProperties { }


    @Override
    public boolean equals(Object obj) {
        return sessionFactoryHolder.equals( ((HibernatePersistenceService) obj).sessionFactoryHolder);
    }

    @Override
    public int hashCode() {
        return (sessionFactoryHolder != null ? sessionFactoryHolder.hashCode() : 0);
    }
}
