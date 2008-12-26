/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideplay.warp.persist.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.wideplay.warp.persist.*;
import com.wideplay.warp.persist.dao.Finder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Test(suiteName = "jpa")
public class ManagedLocalTransactionsAcrossRequestTest {
    private Injector injector;
    private static final String UNIQUE_TEXT = "some unique text" + new Date();
    private static final String UNIQUE_TEXT_MERGE = "meRG_Esome unique text" + new Date();
    private static final String UNIQUE_TEXT_MERGE_FORDF = "aSdoaksdoaksdmeRG_Esome unique text" + new Date();
    private static final String TRANSIENT_UNIQUE_TEXT = "some other unique text" + new Date();


    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(
                new AbstractModule() {

                    protected void configure() {
                        //tell Warp the name of the jpa persistence unit
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                },
                PersistenceService.usingJpa()
            .across(UnitOfWork.REQUEST)

            .forAll(Matchers.any())
            .buildModule());

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }


    @AfterTest   //cleanup entitymanager in case some of the rollback tests left it in an open state
    public final void post() {
        injector.getInstance(WorkManager.class).endWork();
    }

    @AfterClass
    public final void postClass() {
        injector.getInstance(EntityManagerFactory.class).close();
    }

    @Test
    public void testSimpleTransaction() {
        injector.getInstance(TransactionalObject.class).runOperationInTxn();

        EntityManager em = injector.getInstance(EntityManager.class);
        assert !em.getTransaction().isActive() : "txn was not closed by transactional service";

        //test that the data has been stored
        Object result = em.createQuery("from JpaTestEntity where text = :text").setParameter("text", UNIQUE_TEXT).getSingleResult();
        injector.getInstance(WorkManager.class).endWork();

        assert result instanceof JpaTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT.equals(((JpaTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";
    }

    @Test
    public void testSimpleTransactionWithMerge() {
        EntityManager emOrig = injector.getInstance(EntityManager.class);
        JpaTestEntity entity = injector.getInstance(TransactionalObject.class).runOperationInTxnWithMerge();

        assert null != entity.getId() : "Entity was not given an id (was not persisted correctly?)";

        EntityManager em = injector.getInstance(EntityManager.class);
        assert !em.getTransaction().isActive() : "txn was not closed by transactional service";


        //test that the data has been stored
        assert em.isOpen() : "Em was closed after txn!";
        // TODO commented out because of multiple modules refactoring
        //assert em.equals(EntityManagerFactoryHolder.checkCurrentEntityManager()) : "Em was not kept open across txns";
        assert emOrig.equals(em) : "Em was not kept open across txns";
        assert em.contains(entity) : "Merge did not store state or did not return persistent copy";

        Object result = em.createQuery("from JpaTestEntity where text = :text").setParameter("text", UNIQUE_TEXT_MERGE).getSingleResult();
        injector.getInstance(WorkManager.class).endWork();

        assert result instanceof JpaTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT_MERGE.equals(((JpaTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";
    }

    @Test
    public void testSimpleTransactionWithMergeAndDF() {
        EntityManager emOrig = injector.getInstance(EntityManager.class);
        JpaTestEntity entity = injector.getInstance(TransactionalObject.class).runOperationInTxnWithMergeForDf();


        EntityManager em = injector.getInstance(EntityManager.class);
        assert !em.getTransaction().isActive() : "txn was not closed by transactional service";

        //test that the data has been stored
        assert em.isOpen() : "Em was closed after txn!";
        // TODO commented out because of multiple modules refactoring
        //assert em.equals(EntityManagerFactoryHolder.checkCurrentEntityManager()) : "Em was not kept open across txns";
        assert emOrig.equals(em) : "Em was not kept open across txns";
        assert em.contains(entity) : "Merge did not store state or did not return persistent copy";

        Object result = injector.getInstance(TransactionalObject.class).find(UNIQUE_TEXT_MERGE_FORDF); 
        injector.getInstance(WorkManager.class).endWork();

        assert result instanceof JpaTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT_MERGE_FORDF.equals(((JpaTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";
    }

    @Test(expectedExceptions = NoResultException.class)
    public void testSimpleTransactionRollbackOnChecked() {
        try {
            injector.getInstance(TransactionalObject.class).runOperationInTxnThrowingChecked();
        } catch(IOException e) {
            //ignore
            System.out.println("caught (expecting rollback) " + e);

            injector.getInstance(WorkManager.class).endWork();
        }

        EntityManager em = injector.getInstance(EntityManager.class);

        assert !em.getTransaction().isActive() : "Previous EM was not closed by transactional service (rollback didnt happen?)";

        //test that the data has been stored
        Object result = em.createQuery("from JpaTestEntity where text = :text").setParameter("text", TRANSIENT_UNIQUE_TEXT).getSingleResult();
        injector.getInstance(WorkManager.class).endWork();

        assert null == result : "a result was returned! rollback sure didnt happen!!!";
    }

    @Test(expectedExceptions = NoResultException.class)
    public void testSimpleTransactionRollbackOnUnchecked() {
        try {
            injector.getInstance(TransactionalObject.class).runOperationInTxnThrowingUnchecked();
        } catch(RuntimeException re) {
            //ignore
            System.out.println("caught (expecting rollback) " + re);
            injector.getInstance(WorkManager.class).endWork();
        }

        EntityManager em = injector.getInstance(EntityManager.class);
        assert !em.getTransaction().isActive() : "Session was not closed by transactional service (rollback didnt happen?)";

        //test that the data has been stored
        Object result = em.createQuery("from JpaTestEntity where text = :text").setParameter("text", TRANSIENT_UNIQUE_TEXT).getSingleResult();
        injector.getInstance(WorkManager.class).endWork();

        assert null == result : "a result was returned! rollback sure didnt happen!!!";
    }

    public static class TransactionalObject {
        private final EntityManager em;

        @Inject
        public TransactionalObject(EntityManager em) {
            this.em = em;
        }

        @Transactional
        public void runOperationInTxn() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT);
            em.persist(entity);
        }

        @Transactional
        public JpaTestEntity runOperationInTxnWithMerge() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT_MERGE);
            return em.merge(entity);
        }

        @Transactional
        public JpaTestEntity runOperationInTxnWithMergeForDf() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT_MERGE_FORDF);
            return em.merge(entity);
        }

        @Transactional(rollbackOn = IOException.class)
        public void runOperationInTxnThrowingChecked() throws IOException {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(TRANSIENT_UNIQUE_TEXT);
            em.persist(entity);

            throw new IOException();
        }

        @Transactional
        public void runOperationInTxnThrowingUnchecked() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(TRANSIENT_UNIQUE_TEXT);
            em.persist(entity);

            throw new IllegalStateException();
        }

        @Finder(query = "from JpaTestEntity where text = :text")
        public JpaTestEntity find(@Named("text") String text) {
            return null;
        }
    }
}