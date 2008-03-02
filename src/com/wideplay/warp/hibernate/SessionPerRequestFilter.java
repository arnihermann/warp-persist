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

import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.context.ManagedSessionContext;

import javax.servlet.*;
import java.io.IOException;

import net.jcip.annotations.ThreadSafe;
import net.jcip.annotations.Immutable;

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
    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!UnitOfWork.REQUEST.equals(HibernateLocalTxnInterceptor.getUnitOfWork()))
            throw new ServletException("UnitOfWork *must* be REQUEST to use this filter (did you mean to use jpa instead)?");

        //open session;
        ManagedSessionContext.bind(SessionFactoryHolder.getCurrentSessionFactory().openSession());

        try {
            //continue operations
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            //close up session when done
            SessionFactoryHolder.getCurrentSessionFactory().getCurrentSession().close();
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {}
}
