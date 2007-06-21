package com.wideplay.warp.hibernate;

import org.hibernate.context.ManagedSessionContext;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna
 * @since 1.0
 */
public class SessionPerRequestFilter implements Filter {


    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //open session;
        ManagedSessionContext.bind(SessionFactoryHolder.getCurrentSessionFactory().openSession());

        //continue operations
        filterChain.doFilter(servletRequest, servletResponse);

        //close up session when done
        SessionFactoryHolder.getCurrentSessionFactory().getCurrentSession().close();
    }

    public void init(FilterConfig filterConfig) throws ServletException {}
}
