package com.wideplay.warp.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 1/06/2007
 * Time: 11:40:36
 * <p/>
 * TODO: Describe me!
 *
 * @author dprasanna
 * @since 1.0
 */
public class SessionFactoryProvisionTest {
    private Injector injector;

    @BeforeTest
    public void pre() {
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


    @AfterClass
    void post() {
        injector.getInstance(SessionFactory.class).close();
    }

    @Test
    public void testSessionCreateOnInjection() {
        assert injector.getInstance(SessionFactoryHolder.class).equals(injector.getInstance(SessionFactoryHolder.class));

        assert injector.getInstance(PersistenceService.class).equals(injector.getInstance(PersistenceService.class)) : "SINGLETON VIOLATION " + PersistenceService.class.getName() ;

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();

        //obtain session
        assert injector.getInstance(Session.class).isOpen() : "session is not open!";
    }
}
