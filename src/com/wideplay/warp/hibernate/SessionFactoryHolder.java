package com.wideplay.warp.hibernate;

import org.hibernate.SessionFactory;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 31/05/2007
 * Time: 15:26:06
 * <p>
 *
 * A placeholder that frees me from having to use statics to make a singleton session factory,
 * so I can use per-injector singletons vs. per JVM/classloader singletons.
 * </p>
 *
 * @author dprasanna
 * @since 1.0
 */
@ThreadSafe
class SessionFactoryHolder {
    @GuardedBy("setSessionFactory()") //BUT open for concurrent reads, so is volatile
    private volatile SessionFactory sessionFactory;

    //A hack to provide the session factory statically to non-guice objects (interceptors), that can be thrown away come guice1.1
    private static volatile SessionFactoryHolder singletonSessionFactoryHolder;

    //store singleton
    public SessionFactoryHolder() {
        singletonSessionFactoryHolder = this;
    }

    SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    synchronized void setSessionFactory(SessionFactory sessionFactory) {
        if (null != this.sessionFactory)
            throw new RuntimeException("Duplicate session factory creation! Only one session factory is allowed per injector");
        
        this.sessionFactory = sessionFactory;
    }

    static SessionFactory getCurrentSessionFactory() {
        return singletonSessionFactoryHolder.getSessionFactory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! (o instanceof SessionFactoryHolder)) return false;

        SessionFactoryHolder that = (SessionFactoryHolder) o;

        return (sessionFactory == null ? that.sessionFactory == null : sessionFactory.equals(that.sessionFactory));

    }

    @Override
    public int hashCode() {
        return (sessionFactory != null ? sessionFactory.hashCode() : 0);
    }
}
