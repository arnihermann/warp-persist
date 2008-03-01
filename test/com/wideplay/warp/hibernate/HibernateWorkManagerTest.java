package com.wideplay.warp.hibernate;

import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.*;
import com.wideplay.codemonkey.web.startup.Initializer;

import java.util.Date;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class HibernateWorkManagerTest {
    private Injector injector;
    private static final String UNIQUE_TEXT_3 = HibernateWorkManagerTest.class.getSimpleName()
            + "CONSTRAINT_VIOLATING some other unique text" + new Date();

    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.REQUEST)
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

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @AfterClass public void post() {
        injector.getInstance(SessionFactory.class).close();
    }

    @Test
    public void workManagerSessionTest() {
        injector.getInstance(WorkManager.class).beginWork();
        try {
            injector.getInstance(TransactionalObject.class).runOperationInTxn();
        } finally {
            injector.getInstance(WorkManager.class).endWork();

        }


        injector.getInstance(WorkManager.class).beginWork();
        injector.getInstance(Session.class).beginTransaction();
        try {
            final Query query = injector.getInstance(Session.class).createQuery("from HibernateTestEntity where text = :text");

            query.setParameter("text", UNIQUE_TEXT_3);
            final Object o = query.uniqueResult();

            assert null != o : "no result!!";
            assert o instanceof HibernateTestEntity : "Unknown type returned " + o.getClass();
            HibernateTestEntity ent = (HibernateTestEntity)o;

            assert UNIQUE_TEXT_3.equals(ent.getText()) : "Incorrect result returned or not persisted properly"
                    + ent.getText();

        } finally {
            injector.getInstance(WorkManager.class).endWork();
        }
    }




    public static class TransactionalObject {
        @Inject
        Session session;

        @Transactional
        public void runOperationInTxn() {
            HibernateTestEntity testEntity = new HibernateTestEntity();

            testEntity.setText(UNIQUE_TEXT_3);
            session.persist(testEntity);
        }
    }
}