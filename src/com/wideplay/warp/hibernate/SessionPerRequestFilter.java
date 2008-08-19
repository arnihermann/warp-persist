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

import com.wideplay.warp.persist.WorkManager;
import net.jcip.annotations.Immutable;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * <p>
 * Apply this filter in web.xml to enable the HTTP Request unit of work.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 * @see com.wideplay.warp.persist.UnitOfWork
 */
@Immutable
public class SessionPerRequestFilter implements Filter {
    private static final List<WorkManager> workManagers = new CopyOnWriteArrayList<WorkManager>();

    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //open session;
        for (WorkManager wm : workManagers)
            wm.beginWork();
        try {
            //continue operations
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // FIFO
            for (WorkManager wm : workManagers)
                wm.endWork();
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {}

    static void registerWorkManager(WorkManager wm) {
        workManagers.add(wm);
    }
}
