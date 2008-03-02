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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.wideplay.warp.persist.UnitOfWork;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 *
 * <p>
 * Apply this filter in web.xml to enable the HTTP Request unit of work.
 * </p>
 * 
 * @author Jeffrey Chung (lwbruce@gmail.com)
 * @see com.wideplay.warp.persist.UnitOfWork
 */
@ThreadSafe
public class SessionPerRequestFilter implements Filter {

	public void destroy() {}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {

		if (!UnitOfWork.REQUEST.equals(Db4oLocalTxnInterceptor.getUnitOfWork())) {
			throw new ServletException("UnitOfWork must be REQUEST to use this filter. Did you mean to use Hibernate or JPA instead?");
		}

		ObjectServerHolder.getCurrentObjectContainer();

		try {
			filterChain.doFilter(servletRequest, servletResponse);
		} finally {
			ObjectServerHolder.closeCurrentObjectContainer();
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {}

}
