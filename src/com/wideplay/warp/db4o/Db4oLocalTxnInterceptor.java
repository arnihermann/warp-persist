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

package com.wideplay.warp.db4o;

import com.db4o.ObjectContainer;
import com.wideplay.warp.persist.InternalWorkManager;
import com.wideplay.warp.persist.TransactionType;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import net.jcip.annotations.ThreadSafe;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * TODO Rolled-back objects need to be refreshed?
 * 
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 */
@ThreadSafe
class Db4oLocalTxnInterceptor implements MethodInterceptor {
	private final UnitOfWork unitOfWork;
    private final InternalWorkManager<ObjectContainer> internalWorkManager;

    @Transactional
	private static class Internal {}

    public Db4oLocalTxnInterceptor(InternalWorkManager<ObjectContainer> internalWorkManager, UnitOfWork unitOfWork) {
        this.internalWorkManager = internalWorkManager;
        this.unitOfWork = unitOfWork;
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		ObjectContainer oc = internalWorkManager.beginWork();

        Transactional transactional = readTransactionMetadata(methodInvocation);
        if (transactional.type() != null && transactional.type() == TransactionType.READ_ONLY) {
            throw new UnsupportedOperationException("Transaction type READ_ONLY not supported with DB4O");
        }

		Object result;
		try {
			result = methodInvocation.proceed();
		} catch (Exception e) {
            try {
                if (rollbackIfNecessary(transactional, e, oc))
                    oc.commit();

            } finally {
                if (isUnitOfWorkTransaction())
                    endWork();
            }

			throw e;
		}

		// normal, so commit the transaction
		try {
			oc.commit();
		} finally {
            if (isUnitOfWorkTransaction())
                endWork();
        }

		return result;
	}

    private void endWork() {
        this.internalWorkManager.endWork();
    }

    private Transactional readTransactionMetadata(MethodInvocation methodInvocation) {
		Transactional transactional;
		Method method = methodInvocation.getMethod();

		// if none on method, try the class
		Class<?> targetClass = methodInvocation.getThis().getClass().getSuperclass();

		// if there is no transactional annotation of Warp's present, use the default
		if (method.isAnnotationPresent(Transactional.class)) {
			transactional = method.getAnnotation(Transactional.class);
		} else if (targetClass.isAnnotationPresent(Transactional.class)) {
			transactional = targetClass.getAnnotation(Transactional.class);
		} else {
			transactional = Internal.class.getAnnotation(Transactional.class);
		}
		return transactional;
	}

	/**
	 * 
	 * @param transactional
	 * @param e
	 * @return Returns true if rollback did NOT occur
	 */
	private boolean rollbackIfNecessary(Transactional transactional, Exception e, ObjectContainer oc) {
		boolean commit = true;

		for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {
			if (rollBackOn.isInstance(e)) {
				commit = false;

				for (Class<? extends Exception> exceptOn : transactional.exceptOn()) {
					// an exception to the rollback clause was found; don't rollback (i.e., commit and throw anyway)
					if (exceptOn.isInstance(e)) {
						commit = true;
						break;
					}
				}

				// rollback only if nothing matched the exceptOn check
				if (!commit) {
					oc.rollback();
				}

				break;
			}
		}

		return commit;
	}

    private boolean isUnitOfWorkTransaction() {
        return UnitOfWork.TRANSACTION.equals(this.unitOfWork);
    }
}
