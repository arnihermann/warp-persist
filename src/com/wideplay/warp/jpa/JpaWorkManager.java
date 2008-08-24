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

import com.google.inject.Provider;
import com.wideplay.warp.persist.WorkManager;
import net.jcip.annotations.Immutable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 8, 2007
 * Time: 7:30:05 AM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
@Immutable
class JpaWorkManager implements WorkManager {
    private final Provider<EntityManagerFactory> entityManagerFactoryProvider;
    private final EntityManagerProvider emProvider;

    public JpaWorkManager(Provider<EntityManagerFactory> entityManagerFactoryProvider, EntityManagerProvider emProvider) {
        this.entityManagerFactoryProvider = entityManagerFactoryProvider;
        this.emProvider = emProvider;
    }

    public void beginWork() {
        //create if absent
        if (!emProvider.isEntityManagerSet()) {
            emProvider.setEntityManager(
                    entityManagerFactoryProvider.get().createEntityManager());
        }
    }

    public void endWork() {
        if (emProvider.isEntityManagerSet()) {
            EntityManager em = emProvider.get();
            try {
                if (em.isOpen())
                    em.close();
            } finally {
                emProvider.clearEntityManager();
            }
        }
    }

    public String toString() {
        return super.toString();
    }
}