package com.wideplay.warp.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.*;
import org.testng.annotations.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * For instance, a session-per-request strategy will control the opening and closing of the EM
 * at its own (manual) discretion. As opposed to a transactional unit of work.
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
        injector = Guice.createInjector(PersistenceService.usingJpa()
            .across(UnitOfWork.REQUEST)
            .transactedWith(TransactionStrategy.LOCAL)
            .forAll(Matchers.any())
            .buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        //tell Warp the name of the jpa persistence unit
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                });

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @AfterClass
    public void postClass() {
        injector.getInstance(EntityManagerFactory.class).close();
    }

    @BeforeMethod
    public void startRequest() {
        injector.getInstance(WorkManager.class).beginWork();
    }

    @AfterMethod
    public void endRequest() {
        injector.getInstance(WorkManager.class).endWork();
    }

    @Test
    public void testSimpleCrossTxnWork() {
        //pretend that the request was started here
        EntityManager em = injector.getInstance(EntityManager.class);

        JpaTestEntity entity = injector.getInstance(TransactionalObject.class).runOperationInTxn();
        injector.getInstance(TransactionalObject.class).runOperationInTxn2();

        //persisted entity should remain in the same em (which should still be open)
        assert injector.getInstance(EntityManager.class).contains(entity) : "EntityManager  appears to have been closed across txns!";
        assert em.contains(entity) : "EntityManager  appears to have been closed across txns!";
        assert em.isOpen() : "EntityManager appears to have been closed across txns!";

        EntityManagerFactoryHolder.closeCurrentEntityManager();
        EntityManagerFactoryHolder.getCurrentEntityManager();

        //try to query them back out
        em = injector.getInstance(EntityManager.class);
        assert null != em.createQuery("from JpaTestEntity where text = :text").setParameter("text", UNIQUE_TEXT).getSingleResult();
        assert null != em.createQuery("from JpaTestEntity where text = :text").setParameter("text", UNIQUE_TEXT_2).getSingleResult();
        em.close();
    }


    public static class TransactionalObject {
        @Inject
        EntityManager em;

        @Transactional
        public JpaTestEntity runOperationInTxn() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT);
            em.persist(entity);

            return entity;
        }

        @Transactional
        public void runOperationInTxn2() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            em.persist(entity);
        }

    }
}