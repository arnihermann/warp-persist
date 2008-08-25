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
import net.jcip.annotations.Immutable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 */
@Immutable
class EntityManagerProvider implements Provider<EntityManager> {
    /**
     * ThreadLocal we manage manually. Opened automatically
     * by this class or JpaWorkManager, closed by JpaWorkManager
     * or JpaLocalTxnInterceptor. It is vital that we unset the
     * ThreadLocal so we avoid memory leaks!
     * TODO Logic to handle this ThreadLocal is scattered, 
     *      it's probably hosted in the wrong class.
     */
    private final ThreadLocal<EntityManager> entityManager =
            new ThreadLocal<EntityManager>();
    
    private final Provider<EntityManagerFactory> emfProvider;

    public EntityManagerProvider(Provider<EntityManagerFactory> emfProvider) {
        this.emfProvider = emfProvider;
    }

    public EntityManager get() {
        if (!isEntityManagerSet()) {
            setEntityManager(emfProvider.get().createEntityManager());
        }
        return entityManager.get();
    }

    boolean isEntityManagerSet() {
        return entityManager.get() != null;
    }

    void setEntityManager(EntityManager em) {
        entityManager.set(em);
    }

    void clearEntityManager() {
        entityManager.remove();
    }
}
