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

import com.wideplay.warp.util.*;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Apply this filter to enable the HTTP Request unit of work.
 * The filter automatically starts and stops all registered {@link PersistenceService} instances
 * upon {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and
 * {@link javax.servlet.Filter#destroy()}. To disable the managing of PersistenceService instances,
 * set the {code managePersistenceServices} init-param to {@code false} or override
 * {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and {@link javax.servlet.Filter#destroy()}.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 2.0
 * @see UnitOfWork
 */
@ThreadSafe
public class PersistenceFilter implements Filter {
    private static final ReadWriteLock workManagersLock = new ReentrantReadWriteLock();
    private static final ReadWriteLock persistenceServicesLock = new ReentrantReadWriteLock();

    @GuardedBy("PersistenceFilter.workManagersLock")
    private static final List<Lifecycle> workManagers = new ArrayList<Lifecycle>();

    @GuardedBy("PersistenceFilter.persistenceServicesLock")
    private static final List<Lifecycle> persistenceServices = new ArrayList<Lifecycle>();    

    private static final LifecycleAdapter<WorkManager> wmLifecycleAdapter = new LifecycleAdapter<WorkManager>() {
        public Lifecycle asLifecycle(final WorkManager instance) {
            return new Lifecycle() {
                public void start() {
                    instance.beginWork();
                }
                public void stop() {
                    instance.endWork();
                }
            };
        }
    };

    private static final LifecycleAdapter<PersistenceService> psLifecycleAdapter = new LifecycleAdapter<PersistenceService>() {
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

    private boolean managed = true;

    /**
     * Starts all registered {@link com.wideplay.warp.persist.PersistenceService} instances unless
     * the {code managePersistenceServices} init-param has been set to {@code false}.
     * @param filterConfig the filter config
     * @throws ServletException when one or more {code PersistenceService} instances could not be started
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        String managePersistenceServices = filterConfig.getInitParameter("managePersistenceServices");
        if (!Text.empty(managePersistenceServices)) {
            managed = managePersistenceServices.equalsIgnoreCase("true");
        }

        if (managed) {
            persistenceServicesLock.readLock().lock();
            try {
                Lifecycles.failEarly(persistenceServices);
            } finally {
                persistenceServicesLock.readLock().unlock();
            }
        }
    }

    /**
     * Stops all registered {@link com.wideplay.warp.persist.PersistenceService} instances unless
     * the {code managePersistenceServices} init-param has been set to {@code false}.
     */
    public void destroy() {
        if (managed) {
            persistenceServicesLock.readLock().lock();
            try {
                Lifecycles.leaveNoOneBehind(persistenceServices);
            } finally {
                persistenceServicesLock.readLock().unlock();
            }
        }
        persistenceServicesLock.writeLock().lock();
        try {
            persistenceServices.clear();
        } finally {
            persistenceServicesLock.writeLock().unlock();
        }
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        ExceptionalRunnable<ServletException> exceptionalRunnable = new ExceptionalRunnable<ServletException>() {
            public void run() throws ServletException {
                try {
                    filterChain.doFilter(servletRequest, servletResponse);
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            }
        };

        workManagersLock.readLock().lock();
        try {
            if (workManagers.size() == 0) {
                // getClass() so that users see the correct name when using subclasses
                throw new ServletException(String.format("You have registered the %s but you have not configured" +
                        " a persistence strategy that uses UnitOfWork.REQUEST", getClass().getSimpleName()));
            }
            Lifecycles.failEarlyAndLeaveNoOneBehind(workManagers, exceptionalRunnable);
        } finally {
            workManagersLock.readLock().unlock();
        }
    }

    /**
     * The different persistence strategies should add their WorkManager here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param wm the {@code WorkManager} to register
     */
    public static void registerWorkManager(WorkManager wm) {
        workManagersLock.writeLock().lock();
        try {
            workManagers.add(wmLifecycleAdapter.asLifecycle(wm));
        } finally {
            workManagersLock.writeLock().unlock();
        }
    }

    /**
     * The different persistence strategies should add their
     * {@link com.wideplay.warp.persist.PersistenceService} here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param ps the {@code PersistenceService} to register
     */
    public static void registerPersistenceService(PersistenceService ps) {
        persistenceServicesLock.writeLock().lock();
        try {
            persistenceServices.add(psLifecycleAdapter.asLifecycle(ps));
        } finally {
            persistenceServicesLock.writeLock().unlock();
        }
    }

    /**
     * Removes all registered WorkManagers from the filter.
     * Only use when you know what you're doing. Mainly for testing.
     */
    static void clearWorkManagers() {
        workManagersLock.writeLock().lock();
        try {
            workManagers.clear();
        } finally {
            workManagersLock.writeLock().unlock();
        }
    }
}