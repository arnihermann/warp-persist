package com.wideplay.warp.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.SessionFactory;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
class SessionFactoryProvider implements Provider<SessionFactory> {
    private SessionFactoryHolder sessionFactoryHolder;

    @Inject
    public SessionFactoryProvider(SessionFactoryHolder sessionFactoryHolder) {
        this.sessionFactoryHolder = sessionFactoryHolder;
    }

    public SessionFactory get() {
        return this.sessionFactoryHolder.getSessionFactory();
    }    
}
