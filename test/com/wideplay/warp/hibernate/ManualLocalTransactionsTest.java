package com.wideplay.warp.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.ManagedSessionContext;
import org.hibernate.criterion.Expression;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class ManualLocalTransactionsTest {
    private Injector injector;
    private static final String UNIQUE_TEXT = "some unique text" + new Date();
    private static final String UNIQUE_TEXT_2 = "some other unique text" + new Date();

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
                            .setProperties(Initializer.loadProperties("spr-managed-persistence.properties")));
                    }
                });

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @Test
    public void testSimpleCrossTxnWork() {
        org.hibernate.classic.Session session1 = injector.getInstance(SessionFactory.class).openSession();
        ManagedSessionContext.bind(session1);
        HibernateTestEntity entity = injector.getInstance(TransactionalObject.class).runOperationInTxn();
        injector.getInstance(ManualLocalTransactionsTest.TransactionalObject.class).runOperationInTxn2();

        assert injector.getInstance(Session.class).contains(entity) : "Session appears to have been closed across txns!";
        session1.close();

        //try to query them back out

        Session session = injector.getInstance(SessionFactory.class).openSession();
        assert null != session.createCriteria(HibernateTestEntity.class).add(Expression.eq("text", UNIQUE_TEXT)).uniqueResult();
        assert null != session.createCriteria(HibernateTestEntity.class).add(Expression.eq("text", UNIQUE_TEXT_2)).uniqueResult();
        session.close();
    }


    public static class TransactionalObject {
        @Inject
        Session session;

        @Transactional
        public HibernateTestEntity runOperationInTxn() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT);
            session.persist(entity);

            return entity;
        }

        @Transactional
        public void runOperationInTxn2() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            session.persist(entity);
        }

    }
}
