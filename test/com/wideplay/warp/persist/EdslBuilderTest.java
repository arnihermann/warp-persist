package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.hibernate.HibernateTestEntity;
import com.wideplay.warp.persist.dao.HibernateTestAccessor;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class EdslBuilderTest {
    @Test public void testEdslLanguage() {
        PersistenceService.usingHibernate().buildModule();

        PersistenceService.usingHibernate().across(UnitOfWork.REQUEST)
                .addAccessor(HibernateTestAccessor.class)
                .buildModule();

        PersistenceService.usingHibernate().across(UnitOfWork.TRANSACTION).forAll(Matchers.any()).buildModule();
    }

    @Test public void testHibernateConfig() {
        Injector injector = Guice.createInjector(PersistenceService.usingHibernate().across(UnitOfWork.TRANSACTION)
                .transactedWith(TransactionStrategy.JTA).buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration().addAnnotatedClass(HibernateTestEntity.class)
                                .setProperties(Initializer.loadProperties("spt-persistence.properties")));
                    }
                });

        injector.getInstance(PersistenceService.class).start();

        injector.getInstance(TransactionalObject.class).txnMethod();
    }

    static class TransactionalObject {
        @Transactional public void txnMethod() {
            
        }
    }
}
