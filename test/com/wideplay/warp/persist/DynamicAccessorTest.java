package com.wideplay.warp.persist;

import com.google.inject.BindingAnnotation;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.wideplay.warp.hibernate.HibernatePersistenceStrategy;
import com.wideplay.warp.hibernate.HibernateTestEntity;
import com.wideplay.warp.persist.dao.Finder;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author Robbie Vanbrabant
 */
public class DynamicAccessorTest {
    @Test(expectedExceptions = CreationException.class)
    public void testDynamicAccessorWithoutFinderAnnotation() {
        Guice.createInjector(PersistenceService.usingHibernate().across(UnitOfWork.REQUEST)
                .addAccessor(InvalidHibernateTestAccessor.class)
                .buildModule());
    }

    @Test(expectedExceptions = CreationException.class)
    public void testDynamicAccessorWithoutFinderUnit() {
        HibernatePersistenceStrategy hibernate = HibernatePersistenceStrategy.builder().annotatedWith(MyUnit.class).build();
        Guice.createInjector(PersistenceService.using(hibernate).across(UnitOfWork.REQUEST)
                .addAccessor(InvalidHibernateTestAccessor.class)
                .buildModule());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @interface MyUnit {}
    
    public interface InvalidHibernateTestAccessor {
        // Invalid when in multi-modules mode, needs unit=...
        @Finder(query = "from HibernateTestEntity")
        List<HibernateTestEntity> listAll();

        // Invalid, has to have a @Finder annotation.
        List<HibernateTestEntity> listAll2();
    }

}
