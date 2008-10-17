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
import net.jcip.annotations.ThreadSafe;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private static final List<WorkManager> workManagers = new CopyOnWriteArrayList<WorkManager>();
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
    private static final Lifecycles<WorkManager> lifecycles = new Lifecycles<WorkManager>(lifecycleAdapter);

    public void init(FilterConfig filterConfig) throws ServletException {}
    public void destroy() {}

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        // Make a copy of the current workmanager list to avoid a global lock.
        // This ensures that the list does not change in between start and end.
        List<WorkManager> localWorkManagers = new ArrayList<WorkManager>(workManagers);

        ExceptionalRunnable<ServletException> exceptionalRunnable = new ExceptionalRunnable<ServletException>() {
            public void run() throws ServletException {
                try {
                    filterChain.doFilter(servletRequest, servletResponse);
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            }
        };

        lifecycles.failEarlyAndLeaveNoOneBehind(localWorkManagers, exceptionalRunnable);
    }

    /**
     * The different persistence strategies should add their WorkManager here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param wm the {@code WorkManager} to register
     */
    public static void registerWorkManager(WorkManager wm) {
        workManagers.add(wm);
    }

    /** Only use when you know what you're doing. Mainly for testing. */
    public static void clearWorkManagers() {
        workManagers.clear();
    }
}