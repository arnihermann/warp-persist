package com.wideplay.warp.jpa;

import com.google.inject.Provider;
import com.google.inject.Inject;

import javax.persistence.EntityManagerFactory;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
    private final EntityManagerFactoryHolder emFactoryHolder;

    @Inject
    public EntityManagerFactoryProvider(EntityManagerFactoryHolder sessionFactoryHolder) {
        this.emFactoryHolder = sessionFactoryHolder;
    }

    public EntityManagerFactory get() {
        return this.emFactoryHolder.getEntityManagerFactory();
    }
}
