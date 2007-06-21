package com.wideplay.warp.hibernate;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Transaction;
import com.wideplay.warp.persist.Transactional;

import javax.transaction.UserTransaction;
import javax.naming.InitialContext;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:07:46 PM
 *
 * @author Dhanji R. Prasanna
 */
class HibernateJtaTxnInterceptor implements MethodInterceptor {

    //do not make this final! the default must be overridable (ugh, hacky! but no other choice until guice1.1)
    private static String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction"; 

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
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
        }

        //or return result
        return result;
    }

    static void setUserTransactionJndiName(String name) {
        //set name only if valid
        if (null != name && !"".equals(name.trim()))
            USER_TRANSACTION_JNDI_NAME = name;
    }
}
