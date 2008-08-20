/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideplay.warp.hibernate;

import com.google.inject.Provider;
import com.wideplay.warp.persist.TransactionType;
import com.wideplay.warp.persist.Transactional;
import net.jcip.annotations.ThreadSafe;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 3:07:46 PM
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
class HibernateLocalTxnInterceptor implements MethodInterceptor {
    private final Provider<Session> sessionProvider;

    //make this customizable if there is a demand for it?
    @Transactional
    private static class Internal { }

    public HibernateLocalTxnInterceptor(Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Session session = sessionProvider.get();

        //allow silent joining of enclosing transactional methods (NOTE: this ignores the current method's txn-al settings)
        if (session.getTransaction().isActive())
            return methodInvocation.proceed();

        //read out transaction settings
        Transactional transactional = readTransactionMetadata(methodInvocation);

        //read-only txn?
        FlushMode savedFlushMode = FlushMode.AUTO;
        if (TransactionType.READ_ONLY.equals(transactional.type()))
            session.setFlushMode(FlushMode.MANUAL);

        try {
            //no transaction already started, so start one and enforce its semantics
            Transaction txn = session.beginTransaction();
            Object result;

            try {
                result = methodInvocation.proceed();

            } catch(Exception e) {

                //commit transaction only if rollback didnt occur
                if (rollbackIfNecessary(transactional, e, txn))
                    txn.commit();

                //propagate whatever exception is thrown anyway
                throw e;
            }

            //everything was normal so commit the txn (do not move into try block as it interferes with the advised method's throwing semantics)
            Exception commitException = null;
            try {
                txn.commit();
            } catch(RuntimeException re) {
                txn.rollback();
                commitException = re;
            }

            //propagate anyway
            if (null != commitException)
                throw commitException;

            //or return result
            return result;
        } finally {

            //if read-only txn, then restore flushmode, default is automatic flush
            if (TransactionType.READ_ONLY.equals(transactional.type()))
                session.setFlushMode(savedFlushMode);
        }
    }

    private Transactional readTransactionMetadata(MethodInvocation methodInvocation) {
        Transactional transactional;
        Method method = methodInvocation.getMethod();

        //try the class if there's nothing on the method (only go up one level in the hierarchy, to skip the proxy)
        Class<?> targetClass = methodInvocation.getThis().getClass().getSuperclass();

        if (method.isAnnotationPresent(Transactional.class))
            transactional = method.getAnnotation(Transactional.class);
        
        else if (targetClass.isAnnotationPresent(Transactional.class))
            transactional = targetClass.getAnnotation(Transactional.class);
        else
            //if there is no transactional annotation of Warp's present, use the default
            transactional = Internal.class.getAnnotation(Transactional.class);
        return transactional;
    }

    /**
     *
     * @param transactional The metadata annotaiton of the method
     * @param e The exception to test for rollback
     * @param txn A Hibernate Transaction to issue rollbacks against
     * @return returns Returns true if rollback DID NOT HAPPEN (i.e. if commit should continue)
     */
    private boolean rollbackIfNecessary(Transactional transactional, Exception e, Transaction txn) {
        boolean commit = true;

        //check rollback clauses
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

    public String toString() {
        return String.format("%s[session: %s]",super.toString(), this.sessionProvider);
    }
}
