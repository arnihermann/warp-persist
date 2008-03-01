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
    private static volatile UnitOfWork unitOfWork;

	public void destroy() {}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {

		if (!unitOfWork.equals(UnitOfWork.REQUEST)) {
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

	public static void setUnitOfWork(UnitOfWork unitOfWork) {
		SessionPerRequestFilter.unitOfWork = unitOfWork;
	}
}
