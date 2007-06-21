package com.wideplay.warp.jpa;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
public class JpaBindingSupport {

    private JpaBindingSupport() {
    }

    public static void addBindings(Binder binder) {

        binder.bind(EntityManagerFactoryHolder.class).in(Singleton.class);

        binder.bind(EntityManagerFactory.class).toProvider(EntityManagerFactoryProvider.class);
        binder.bind(EntityManager.class).toProvider(EntityManagerProvider.class);

        binder.bind(PersistenceService.class).to(JpaPersistenceService.class).in(Singleton.class);
    }

    //how ugly this static setup stuff is, ick. Please give us managed interceptors already guice!!!
    public static void setUnitOfWork(UnitOfWork unitOfWork) {
        //set the default unit-of-work strategy
        JpaLocalTxnInterceptor.setUnitOfWork(unitOfWork);
    }

    public static MethodInterceptor getInterceptor(TransactionStrategy transactionStrategy) {

        switch (transactionStrategy) {
            case LOCAL:
                return new JpaLocalTxnInterceptor();
            case JTA:
//                return new HibernateJtaTxnInterceptor();
        }

        return null;
    }
}
