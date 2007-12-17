package com.wideplay.warp.persist;

import com.wideplay.warp.persist.dao.Finder;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public abstract class PersistenceService {
    public abstract void start();

    public static SessionStrategyBuilder usingHibernate() {
        return new PersistenceServiceBuilderImpl(new PersistenceModule(PersistenceModule.PersistenceFlavor.HIBERNATE));
    }

    public static SessionStrategyBuilder usingJpa() {
        return new PersistenceServiceBuilderImpl(new PersistenceModule(PersistenceModule.PersistenceFlavor.JPA));
    }

    public static boolean isDynamicFinder(Method method) {
        return method.isAnnotationPresent(Finder.class);
    }

    public static SessionStrategyBuilder usingDb4o() {
        return new PersistenceServiceBuilderImpl(new PersistenceModule(PersistenceModule.PersistenceFlavor.JPA));
    }
}
