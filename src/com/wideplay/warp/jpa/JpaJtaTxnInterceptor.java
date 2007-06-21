package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:07:46 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
class JpaJtaTxnInterceptor implements MethodInterceptor {
    private static UnitOfWork unitOfWork;

    //do not make this final! the default must be overridable (ugh, hacky! but no other choice until guice1.1)
    private static String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        //create new entity manager if necessary
        if (isUnitOfWorkTransaction())
            EntityManagerFactoryHolder.getCurrentEntityManager();

        UserTransaction txn = (UserTransaction) new InitialContext().lookup(USER_TRANSACTION_JNDI_NAME);

        Object result;
        try {
            txn.begin();
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
        } finally {
            
            //close the em if necessary
            if (isUnitOfWorkTransaction())
                EntityManagerFactoryHolder.closeCurrentEntityManager();
        }

        //or return result
        return result;
    }


    private static boolean isUnitOfWorkTransaction() {
        return UnitOfWork.TRANSACTION.equals(unitOfWork);
    }


    static void setUnitOfWork(UnitOfWork unitOfWork) {
        JpaJtaTxnInterceptor.unitOfWork = unitOfWork;
    }
}
