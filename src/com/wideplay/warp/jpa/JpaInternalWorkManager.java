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
import com.wideplay.warp.persist.InternalWorkManager;
import com.wideplay.warp.persist.ManagedContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Robbie Vanbrabant
 */
public class JpaInternalWorkManager implements InternalWorkManager<EntityManager> {
    private final Provider<EntityManagerFactory> emfProvider;

    public JpaInternalWorkManager(Provider<EntityManagerFactory> emfProvider) {
        this.emfProvider = emfProvider;
    }

    public EntityManager beginWork() {
        EntityManagerFactory emf = this.emfProvider.get();
        EntityManager em = ManagedContext.getBind(EntityManager.class, emf);
        if (em == null) {
            em = emf.createEntityManager();
            ManagedContext.bind(EntityManager.class, emf, em);
        }
        return em;
    }

    public void endWork() {
        EntityManagerFactory emf = this.emfProvider.get();
        if (ManagedContext.hasBind(EntityManager.class, emf)) {
            EntityManager em = ManagedContext.unbind(EntityManager.class, emf);
            if (em != null && em.isOpen()) em.close();
        }
    }
}
