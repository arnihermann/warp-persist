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

import com.wideplay.warp.util.ExceptionalRunnable;
import com.wideplay.warp.util.Lifecycle;
import com.wideplay.warp.util.LifecycleAdapter;
import com.wideplay.warp.util.Lifecycles;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Apply this filter in web.xml to enable the HTTP Request unit of work.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 2.0
 * @see UnitOfWork
 */
@ThreadSafe
public class SessionFilter implements Filter {
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("SessionFilter.lock")
    private static final List<Lifecycle> workManagers = new ArrayList<Lifecycle>();

    private static final LifecycleAdapter<WorkManager> lifecycleAdapter = new LifecycleAdapter<WorkManager>() {
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
    
    public void init(FilterConfig filterConfig) throws ServletException {}
    public void destroy() {}

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

        lock.readLock().lock();
        try {
            Lifecycles.failEarlyAndLeaveNoOneBehind(workManagers, exceptionalRunnable);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * The different persistence strategies should add their WorkManager here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param wm the {@code WorkManager} to register
     */
    public static void registerWorkManager(WorkManager wm) {
        lock.writeLock().lock();
        try {
            workManagers.add(lifecycleAdapter.asLifecycle(wm));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Only use when you know what you're doing. Mainly for testing. */
    public static void clearWorkManagers() {
        lock.writeLock().lock();
        try {
            workManagers.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}