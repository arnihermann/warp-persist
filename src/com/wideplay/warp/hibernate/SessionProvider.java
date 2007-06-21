package com.wideplay.warp.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.google.inject.Provider;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * On: May 26, 2007 2:26:28 PM
 *
 * @author Dhanji R. Prasanna
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
