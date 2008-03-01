package com.wideplay.warp.jpa;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.WorkManager;
import org.aopalliance.intercept.MethodInterceptor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
@Immutable
public class JpaBindingSupport {

    private JpaBindingSupport() {
    }

    public static void addBindings(Binder binder) {

        binder.bind(EntityManagerFactoryHolder.class).in(Singleton.class);

        binder.bind(EntityManagerFactory.class).toProvider(EntityManagerFactoryProvider.class);
        binder.bind(EntityManager.class).toProvider(EntityManagerProvider.class);

        binder.bind(PersistenceService.class).to(JpaPersistenceService.class).in(Singleton.class);
        binder.bind(WorkManager.class).to(JpaWorkManager.class);
    }

    //how ugly this static setup stuff is, ick. Please give us managed interceptors already guice!!! And use Warp people!
    public static void setUnitOfWork(UnitOfWork unitOfWork) {
        //set the default unit-of-work strategy
        JpaLocalTxnInterceptor.setUnitOfWork(unitOfWork);
        SessionPerRequestFilter.setUnitOfWork(unitOfWork);
    }

    public static MethodInterceptor getInterceptor(TransactionStrategy transactionStrategy) {

        switch (transactionStrategy) {
            case LOCAL:
                return new JpaLocalTxnInterceptor();
//            case JTA:
//                return new JpaJtaTxnInterceptor();
        }

        throw new IllegalArgumentException("No such transaction strategy known: " + transactionStrategy);
    }

    public static MethodInterceptor getFinderInterceptor() {
        return new JpaFinderInterceptor();
    }
}
