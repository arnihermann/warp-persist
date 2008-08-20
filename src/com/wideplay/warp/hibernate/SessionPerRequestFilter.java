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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Apply this filter in web.xml to enable the HTTP Request unit of work.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 1.0
 * @see com.wideplay.warp.persist.UnitOfWork
 */
@Immutable
public class SessionPerRequestFilter implements Filter {
    private static final List<WorkManager> workManagers = new CopyOnWriteArrayList<WorkManager>();

    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // Make a copy of the current workmanager list to avoid a global lock.
        // This ensures that the list does not change in between start and end.
        List<WorkManager> localWorkManagers = new ArrayList<WorkManager>(workManagers);

        // Iterate with index so we can clean up if needed.
        for (int i=0, size=localWorkManagers.size(); i < size; i++) {
            try {
                localWorkManagers.get(i).beginWork();
            } catch (RuntimeException e) {
                // clean up what we did so far and end this madness.
                endAsMuchWorkAsPossible(localWorkManagers.subList(0, i-1));
                throw e;
            }
        }
        try {
            //continue operations
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            endAsMuchWorkAsPossible(localWorkManagers);
        }
    }

    /**
     * Tries to end work for as many WorkManagers as possible, in order.
     * Accumulates exceptions and rethrows them in a RuntimeException.
     */
    private void endAsMuchWorkAsPossible(List<WorkManager> workManagers) {
        StringBuilder exceptionMessages = new StringBuilder();
        for (WorkManager wm : workManagers) {
            try {
                wm.endWork();
            } catch (RuntimeException e) {
                // record the exception and proceed
                exceptionMessages.append(String.format("Could not end work for WorkManager '%s':%n%s%n%s%n",
                                                       wm.toString(), e.getMessage(), stackTraceAsString(e)));
            }
        }
        if (exceptionMessages.length() > 0) {
            throw new RuntimeException(exceptionMessages.toString());
        }
    }

    private String stackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            e.printStackTrace(pw);
            return sw.getBuffer().toString();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException ignored) {
                } finally {
                    if (pw != null) pw.close();
                }
            }
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {}

    /**
     * The difference persistence strategies should add their WorkManager here
     * at configuration time if they support {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}.
     */
    static void registerWorkManager(WorkManager wm) {
        workManagers.add(wm);
    }
}
