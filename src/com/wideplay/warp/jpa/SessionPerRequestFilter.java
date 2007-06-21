package com.wideplay.warp.jpa;

import com.wideplay.warp.persist.UnitOfWork;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class SessionPerRequestFilter implements Filter {
    private static UnitOfWork unitOfWork;

    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!UnitOfWork.REQUEST.equals(unitOfWork))
            throw new ServletException("UnitOfWork *must* be REQUEST to use this filter (did you mean to use hibernate instead)?");

        //open a new EM
        EntityManagerFactoryHolder.getCurrentEntityManager();

        //continue operations
        filterChain.doFilter(servletRequest, servletResponse);

        //close up em when done
        EntityManagerFactoryHolder.closeCurrentEntityManager();
    }

    public void init(FilterConfig filterConfig) throws ServletException {}

    static void setUnitOfWork(UnitOfWork unitOfWork) {
        SessionPerRequestFilter.unitOfWork = unitOfWork;
    }
}
