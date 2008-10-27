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
package com.wideplay.warp.persist;

import com.wideplay.warp.util.Lifecycle;
import com.wideplay.warp.util.LifecycleAdapter;
import com.wideplay.warp.util.Lifecycles;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SessionFilter that starts and stops all registered {@link PersistenceService} instances
 * upon {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and
 * {@link javax.servlet.Filter#destroy()}.
 * 
 * @author Robbie Vanbrabant
 * @see com.wideplay.warp.persist.SessionFilter
 */
@ThreadSafe
public class LifecycleSessionFilter extends SessionFilter {
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("LifecycleSessionFilter.lock")
    private static final List<Lifecycle> persistenceServices = new ArrayList<Lifecycle>();

    private static final LifecycleAdapter<PersistenceService> lifecycleAdapter = new LifecycleAdapter<PersistenceService>() {
        public Lifecycle asLifecycle(final PersistenceService instance) {
            return new Lifecycle() {
                public void start() {
                    instance.start();
                }
                public void stop() {
                    instance.shutdown();
                }
            };
        }
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        lock.readLock().lock();
        try {
            Lifecycles.failEarly(persistenceServices);
        } finally {
            lock.readLock().unlock();
        }
        super.init(filterConfig);
    }

    @Override
    public void destroy() {
        super.destroy();

        lock.readLock().lock();
        try {
            Lifecycles.leaveNoOneBehind(persistenceServices);
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            persistenceServices.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * The different persistence strategies should add their
     * {@link com.wideplay.warp.persist.PersistenceService} here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param ps the {@code PersistenceService} to register
     */
    public static void registerPersistenceService(PersistenceService ps) {
        lock.writeLock().lock();
        try {
            persistenceServices.add(lifecycleAdapter.asLifecycle(ps));
        } finally {
            lock.writeLock().unlock();
        }
    }
}
