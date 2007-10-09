package com.wideplay.warp.persist.dao;

import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.hibernate.HibernateTestEntity;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.codemonkey.web.startup.Initializer;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 9, 2007
 * Time: 1:44:56 PM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
public class HibernateAbstractClassDFTest {
    private Injector injector;

    @BeforeTest
    public final void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.TRANSACTION)
            .transactedWith(TransactionStrategy.LOCAL)
            .forAll(Matchers.any())

            .buildModule(),

                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration()
                            .addAnnotatedClass(HibernateTestEntity.class)
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));

                    }
                });
    }

    @Test
    public final void testAbstractClassDynamicFinder() {
        AbstractWithDF withDF = injector.getInstance(AbstractWithDF.class);

        assert null != withDF : "no instance returned";
    }

    public static abstract class AbstractWithDF {

    }
}
