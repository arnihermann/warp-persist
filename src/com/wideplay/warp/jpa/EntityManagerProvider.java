package com.wideplay.warp.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:26:28 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
class EntityManagerProvider implements Provider<EntityManager> {
    private final EntityManagerFactoryHolder holder;

    @Inject
    public EntityManagerProvider(EntityManagerFactoryHolder holder) {
        this.holder = holder;
    }

    public EntityManager get() {
        return holder.getEntityManager();
    }
}
