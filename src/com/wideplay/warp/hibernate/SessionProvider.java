package com.wideplay.warp.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:26:28 PM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
class SessionProvider implements Provider<Session> {
    private final SessionFactory factory;

    @Inject
    public SessionProvider(SessionFactory factory) {
        this.factory = factory;
    }

    public Session get() {
        return factory.getCurrentSession();
    }
}
