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

import com.wideplay.warp.persist.internal.*;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Apply this filter to enable the HTTP Request unit of work and to have Warp Persist manage the lifecycle of
 * all the active (module installed) {@code PersistenceService} instances.
 * The filter automatically starts and stops all registered {@link PersistenceService} instances
 * upon {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and
 * {@link javax.servlet.Filter#destroy()}. To disable the managing of PersistenceService instances,
 * override {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and {@link javax.servlet.Filter#destroy()}.
 * <p>
 * To be able to use {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, register this filter <b>once</b> in the
 * {@code web.xml} or using Guice's 2.0 {@code ServletModule}. It is important that you register this filter
 * before any other framework filter (except the Guice servlet filter). Example configuration:
 * <pre>{@code <filter>
 *   <filter-name>persistenceFilter</filter-name>
 *   <filter-class>com.wideplay.warp.persist.PersistenceFilter</filter-class>
 * </filter>
 *
 * <filter-mapping>
 *   <filter-name>persistenceFilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 * </p>
 * <p>
 * Important note: {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} will have no effect if Guice has
 * not been started before it gets called. Usually this means Guice should be started in a
 * {@link javax.servlet.ServletContextListener}. If you can't for some reason, don't worry; all
 * {@link com.wideplay.warp.persist.PersistenceService} instances will automatically start when they first get used.
 * Just make sure you don't get any incoming request before Guice starts.
 * </p>
 * <p>
 * Even though all mutable state is package local, this Filter is thread safe. This allows people to create
 * injectors concurrently and deploy multiple Warp Persist applications within the same VM / web container.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 2.0
 * @see UnitOfWork
 */
@ThreadSafe
public class PersistenceFilter implements Filter {
    private static final ReadWriteLock workManagersLock = new ReentrantReadWriteLock();

    // If we don't use this lock, we have to copy the list to make sure it does not change in between
    // all the beginWork() and endWork() calls.
    @GuardedBy("PersistenceFilter.workManagersLock")
    private static final List<Lifecycle> workManagers = new ArrayList<Lifecycle>();

    private static final List<Lifecycle> persistenceServices = new CopyOnWriteArrayList<Lifecycle>();    

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

    /**
     * Starts all registered {@link com.wideplay.warp.persist.PersistenceService} instances.
     * @param filterConfig the filter config
     * @throws ServletException when one or more {code PersistenceService} instances could not be started
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        Lifecycles.failEarly(persistenceServices);
    }

    /**
     * Stops all registered {@link com.wideplay.warp.persist.PersistenceService} instances.
     */
    public void destroy() {
        Lifecycles.leaveNoOneBehind(persistenceServices);
        persistenceServices.clear();
    }

    /**
     * Activates the HTTP request unit of work.
     * @param servletRequest HTTP request
     * @param servletResponse HTTP response
     * @param filterChain filter chain
     * @throws IOException
     * @throws ServletException
     */
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
     * Internal use only, NOT part of the SPI.
     * The different persistence strategies should add their WorkManager here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param wm the {@code WorkManager} to register
     */
    static void registerWorkManager(WorkManager wm) {
        workManagersLock.writeLock().lock();
        try {
            workManagers.add(wmLifecycleAdapter.asLifecycle(wm));
        } finally {
            workManagersLock.writeLock().unlock();
        }
    }

    /**
     * Internal use only, NOT part of the SPI.
     * The different persistence strategies should add their
     * {@link com.wideplay.warp.persist.PersistenceService} here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param ps the {@code PersistenceService} to register
     */
    static void registerPersistenceService(PersistenceService ps) {
        persistenceServices.add(psLifecycleAdapter.asLifecycle(ps));
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