package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;

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

        txn.begin();

        try {
            result = methodInvocation.proceed();

        } catch(Exception e) {
            Transactional transactional = methodInvocation.getMethod().getAnnotation(Transactional.class);

            //commit transaction only if rollback didnt occur
            if (rollbackIfNecessary(transactional, e, txn))
                txn.commit();

            //propagate whatever exception is thrown anyway
            throw e;
        } finally {
            
            //close the em if necessary
            if (isUnitOfWorkTransaction())
                EntityManagerFactoryHolder.closeCurrentEntityManager();
        }

        //everything was normal so commit the txn (do not move into try-block)
        txn.commit();

        //or return result
        return result;
    }

    /**
     *
     * @param transactional The metadata annotaiton of the method
     * @param e The exception to test for rollback
     * @param txn A Hibernate Transaction to issue rollbacks against
     * @return returns Returns true if rollback DID NOT HAPPEN (i.e. if commit should continue)
     *
     * @throws javax.transaction.SystemException If there was an error during rollback
     */
    private boolean rollbackIfNecessary(Transactional transactional, Exception e, UserTransaction txn) throws SystemException {
        boolean commit = true;

        //check rollback clauses                                                                        t
        for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {

            //if one matched, try to perform a rollback
            if (rollBackOn.isInstance(e)) {
                commit = false;

                //check exceptOn clauses (supercedes rollback clause)
                for (Class<? extends Exception> exceptOn : transactional.exceptOn()) {

                    //An exception to the rollback clause was found, DONT rollback (i.e. commit and throw anyway)
                    if (exceptOn.isInstance(e)) {
                        commit = true;
                        break;
                    }
                }

                //rollback only if nothing matched the exceptOn check
                if (!commit) {
                    txn.rollback();
                }
                //otherwise continue to commit

                break;
            }
        }

        return commit;
    }


    private static boolean isUnitOfWorkTransaction() {
        return UnitOfWork.TRANSACTION.equals(unitOfWork);
    }


    static void setUnitOfWork(UnitOfWork unitOfWork) {
        JpaJtaTxnInterceptor.unitOfWork = unitOfWork;
    }
}
