package com.wideplay.warp.hibernate;

import com.wideplay.warp.persist.Transactional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Transaction;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:07:46 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
class HibernateLocalTxnInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Transaction txn = SessionFactoryHolder.getCurrentSessionFactory().getCurrentSession().beginTransaction();
        Object result;
        try {
            result = methodInvocation.proceed();

            txn.commit();
        } catch(Exception e) {
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
        }

        //or return result
        return result;
    }
}
