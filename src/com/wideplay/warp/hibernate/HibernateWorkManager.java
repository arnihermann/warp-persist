package com.wideplay.warp.hibernate;

import com.wideplay.warp.persist.WorkManager;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.SessionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 8, 2007
 * Time: 7:30:05 AM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
class HibernateWorkManager implements WorkManager {


    public void beginWork() {
        if (UnitOfWork.TRANSACTION.equals(SessionPerRequestFilter.getUnitOfWork()))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        if (ManagedSessionContext.hasBind(SessionFactoryHolder.getCurrentSessionFactory()))
            return;

        //open session;
        ManagedSessionContext.bind(SessionFactoryHolder.getCurrentSessionFactory().openSession());
    }

    public void endWork() {
        if (UnitOfWork.TRANSACTION.equals(SessionPerRequestFilter.getUnitOfWork()))
            throw new IllegalStateException("Cannot use WorkManager with UnitOfWork.TRANSACTION");

        SessionFactory sessionFactory = SessionFactoryHolder.getCurrentSessionFactory();
        if (!ManagedSessionContext.hasBind(sessionFactory))
            return;

        //close up session when done
        sessionFactory.getCurrentSession().close();

        
        //open session;
        ManagedSessionContext.unbind(sessionFactory);
    }
}
