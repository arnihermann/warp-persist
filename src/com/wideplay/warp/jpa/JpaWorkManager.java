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
import net.jcip.annotations.ThreadSafe;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
@Immutable
@ThreadSafe // Thread confinement through ThreadLocal
class JpaWorkManager implements WorkManager {
    private final Provider<EntityManagerFactory> entityManagerFactoryProvider;

    public JpaWorkManager(Provider<EntityManagerFactory> entityManagerFactoryProvider) {
        this.entityManagerFactoryProvider = entityManagerFactoryProvider;
    }

    public void beginWork() {
        EntityManagerFactory emf = this.entityManagerFactoryProvider.get();
        if (!ManagedEntityManagerContext.hasBind(emf)) {
            ManagedEntityManagerContext.bind(emf, emf.createEntityManager());
        }
    }

    public void endWork() {
        EntityManagerFactory emf = this.entityManagerFactoryProvider.get();
        if (ManagedEntityManagerContext.hasBind(emf)) {
            EntityManager em = ManagedEntityManagerContext.unbind(emf);
            if (em != null && em.isOpen()) em.close();
        }
    }

    public String toString() {
        return super.toString();
    }
}