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

package com.wideplay.warp.jpa;

import com.google.inject.Inject;
import com.wideplay.warp.persist.PersistenceService;
import net.jcip.annotations.ThreadSafe;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@ThreadSafe
class JpaPersistenceService extends PersistenceService {
    private final EntityManagerFactoryHolder emFactoryHolder;
    private final String persistenceUnitName;
    private volatile Properties customProperties;

    @Deprecated
    private static final String JTA_USER_TRANSACTION = "jta.UserTransaction";

    @Inject
    public JpaPersistenceService(EntityManagerFactoryHolder sessionFactoryHolder, @JpaUnit String persistenceUnitName) {
        this.emFactoryHolder = sessionFactoryHolder;
        this.persistenceUnitName = persistenceUnitName;

        assert null != persistenceUnitName && (!"".equals(persistenceUnitName.trim()))
                : "Persistence unit name was not set! (please bindConstant().annotatedWith(JpaUnit.class) to the name of a persistence unit";
    }

    public synchronized void start() {
        //create with custom properties if necessary
        if (null != customProperties)
            emFactoryHolder.setEntityManagerFactory(Persistence.createEntityManagerFactory(persistenceUnitName, customProperties));
        else
            emFactoryHolder.setEntityManagerFactory(Persistence.createEntityManagerFactory(persistenceUnitName));

        //if necessary, set the JNDI lookup name of the JTA txn
    }

    public void shutdown() {
        // SPRInterceptor syncs on the same instance.
        synchronized(emFactoryHolder.getEntityManagerFactory()) {
            EntityManagerFactory emf = emFactoryHolder.getEntityManagerFactory();
            if (emf.isOpen()) emf.close();
        }
    }


    @Inject(optional = true)
    public void setCustomProperties(@JpaUnit Properties customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return  obj instanceof JpaPersistenceService &&

                emFactoryHolder.equals( ((JpaPersistenceService) obj).emFactoryHolder);
    }

    @Override
    public int hashCode() {
        return (emFactoryHolder != null ? emFactoryHolder.hashCode() : 0);
    }
}
