package com.wideplay.warp.jpa;

import com.google.inject.Inject;
import com.wideplay.warp.persist.PersistenceService;

import javax.persistence.Persistence;
import java.util.Properties;

import net.jcip.annotations.ThreadSafe;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
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
