package com.wideplay.warp.hibernate;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class HibernateBindingSupport {
    public static void addBindings(Binder binder) {

        binder.bind(SessionFactoryHolder.class).in(Singleton.class);

        binder.bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
        binder.bind(Session.class).toProvider(SessionProvider.class);

        binder.bind(PersistenceService.class).to(HibernatePersistenceService.class).in(Singleton.class);
    }

    public static MethodInterceptor getInterceptor(TransactionStrategy transactionStrategy) {
        switch (transactionStrategy) {
            case LOCAL:
                return new HibernateLocalTxnInterceptor();
            case JTA:
                return new HibernateJtaTxnInterceptor();
        }

        throw new IllegalArgumentException("No such transaction strategy known: " + transactionStrategy);
    }

    public static MethodInterceptor getFinderInterceptor() {
        return new HibernateFinderInterceptor();
    }

    public static void setUnitOfWork(UnitOfWork unitOfWork) {
        SessionPerRequestFilter.setUnitOfWork(unitOfWork);
    }
}
