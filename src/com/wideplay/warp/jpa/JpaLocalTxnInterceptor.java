package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:07:46 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
class JpaLocalTxnInterceptor implements MethodInterceptor {
    private static UnitOfWork unitOfWork;

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        EntityManager em = EntityManagerFactoryHolder.getCurrentEntityManager();

        //start txn
        final EntityTransaction txn = em.getTransaction();
        txn.begin();

        Object result;
        try {
            result = methodInvocation.proceed();

            txn.commit();
        } catch (Exception e) {
            Transactional transactional = methodInvocation.getMethod().getAnnotation(Transactional.class);

            //rollback only on specified exceptions
            for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {
                if (rollBackOn.isInstance(e)) {
                    txn.rollback();
                    break;
                }
            }

            //propagate whatever exception is thrown anyway
            throw e;
        } finally {
            //close the em if necessary
            if (isUnitOfWorkTransaction()) {
                EntityManagerFactoryHolder.closeCurrentEntityManager();
            }
        }

        //or return result
        return result;
    }

    private static boolean isUnitOfWorkTransaction() {
        return UnitOfWork.TRANSACTION.equals(unitOfWork);
    }


    static void setUnitOfWork(UnitOfWork unitOfWork) {
        JpaLocalTxnInterceptor.unitOfWork = unitOfWork;
    }
}
