package com.wideplay.warp.hibernate;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.wideplay.warp.persist.PersistenceService;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@Immutable
class HibernatePersistenceService extends PersistenceService {
    private final SessionFactoryHolder sessionFactoryHolder;
    private final Configuration configuration;

    private static final String JTA_USER_TRANSACTION = "jta.UserTransaction";

    @Inject
    public HibernatePersistenceService(SessionFactoryHolder sessionFactoryHolder, Configuration configuration) {
        this.sessionFactoryHolder = sessionFactoryHolder;
        this.configuration = configuration;
    }

    public void start() {
        sessionFactoryHolder.setSessionFactory(configuration.buildSessionFactory());

        //if necessary, set the JNDI lookup name of the JTA txn
        HibernateJtaTxnInterceptor.setUserTransactionJndiName(configuration.getProperty(JTA_USER_TRANSACTION));
    }

    @BindingAnnotation
    @Retention(RUNTIME)
    public @interface PersistenceProperties { }


    @Override
    public boolean equals(Object obj) {
        return sessionFactoryHolder.equals( ((HibernatePersistenceService) obj).sessionFactoryHolder);
    }

    @Override
    public int hashCode() {
        return (sessionFactoryHolder != null ? sessionFactoryHolder.hashCode() : 0);
    }
}
