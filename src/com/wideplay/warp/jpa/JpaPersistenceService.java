package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.PersistenceService;
import com.google.inject.Inject;

import javax.persistence.Persistence;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
class JpaPersistenceService extends PersistenceService {
    private final EntityManagerFactoryHolder emFactoryHolder;
    private final String persistenceUnitName;

    private static final String JTA_USER_TRANSACTION = "jta.UserTransaction";

    @Inject
    public JpaPersistenceService(EntityManagerFactoryHolder sessionFactoryHolder, @JpaUnit String persistenceUnitName) {
        this.emFactoryHolder = sessionFactoryHolder;
        this.persistenceUnitName = persistenceUnitName;

        assert null != persistenceUnitName && (!"".equals(persistenceUnitName.trim()))
                : "Persistence unit name was not set! (please bindConstant().annotatedWith(JpaUnit.class) to the name of a persistence unit";
    }

    public void start() {
        emFactoryHolder.setEntityManagerFactory(Persistence.createEntityManagerFactory(persistenceUnitName));

        //if necessary, set the JNDI lookup name of the JTA txn
    }

    @Override
    public boolean equals(Object obj) {
        return emFactoryHolder.equals( ((JpaPersistenceService) obj).emFactoryHolder);
    }

    @Override
    public int hashCode() {
        return (emFactoryHolder != null ? emFactoryHolder.hashCode() : 0);
    }
}
