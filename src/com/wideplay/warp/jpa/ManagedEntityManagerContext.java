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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility analogous to Hibernate's {@link org.hibernate.context.ManagedSessionContext}.
 * In {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, this class is used by the
 * {@link com.wideplay.warp.jpa.JpaWorkManager}, in {@link com.wideplay.warp.persist.UnitOfWork#TRANSACTION} by
 * {@link com.wideplay.warp.jpa.EntityManagerProvider} and {@link com.wideplay.warp.jpa.JpaLocalTxnInterceptor}.
 *
 * @author Robbie Vanbrabant
 */
public class ManagedEntityManagerContext {
    private ManagedEntityManagerContext() {}
    
    private static final ThreadLocal<Map<EntityManagerFactory, EntityManager>> emfToEm =
            new ThreadLocal<Map<EntityManagerFactory, EntityManager>>();

    public static EntityManager bind(EntityManagerFactory emf, EntityManager em) {
        return getContext(Wish.EXISTING_OR_NEW).put(emf, em);
    }

    public static EntityManager unbind(EntityManagerFactory emf) {
        Map<EntityManagerFactory, EntityManager> contextMap = getContext(Wish.EXISTING_OR_NULL);
        if (contextMap == null) return null;

        try {
            return contextMap.remove(emf);
        } finally {
            if (contextMap.isEmpty())
                emfToEm.remove();
        }
    }

    public static EntityManager getBind(EntityManagerFactory emf) {
        Map<EntityManagerFactory, EntityManager> contextMap = getContext(Wish.EXISTING_OR_NULL);
        if (contextMap == null) return null;
        return contextMap.get(emf);
    }

    public static boolean hasBind(EntityManagerFactory emf) {
        Map<EntityManagerFactory, EntityManager> contextMap = getContext(Wish.EXISTING_OR_NULL);
        return contextMap != null && contextMap.get(emf) != null;
    }

    private static Map<EntityManagerFactory, EntityManager> getContext(Wish wish) {
        Map<EntityManagerFactory, EntityManager> contextMap = emfToEm.get();
        if (contextMap == null && Wish.EXISTING_OR_NEW == wish) {
            contextMap = new HashMap<EntityManagerFactory, EntityManager>();
            emfToEm.set(contextMap);
        }
        return contextMap;
    }

    private enum Wish {
        EXISTING_OR_NEW, EXISTING_OR_NULL
    }    
}
