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

package com.wideplay.warp.jpa;

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
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 *
 * This test asserts class level @Transactional annotation behavior (see the forAll(..) statement
 * below in method pre()).
 *
 * Class-level @Transactional is a shortcut if all non-private
 * methods in the class are meant to be transactional. 
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class ClassLevelManagedLocalTransactionsTest {
    private Injector injector;
    private static final String UNIQUE_TEXT = "JPAsome unique text88888" + new Date();
    private static final String UNIQUE_TEXT_2 = "JPAsome asda unique teasdalsdplasdxt" + new Date();
    private static final String TRANSIENT_UNIQUE_TEXT = "JPAsome other unique texaksoksojadasdt" + new Date();

    @BeforeMethod
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingJpa()
            .across(UnitOfWork.TRANSACTION)
            .transactedWith(TransactionStrategy.LOCAL)
            .forAll(Matchers.annotatedWith(Transactional.class), Matchers.any())
            .buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        //tell Warp the name of the jpa persistence unit
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                }
        );

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @AfterMethod
    void post() {
        injector.getInstance(EntityManagerFactory.class).close();
        injector = null;
    }

    @Test
    public void testSimpleTransaction() {
        injector.getInstance(TransactionalObject.class).runOperationInTxn();

        EntityManager session = injector.getInstance(EntityManager.class);
        assert !session.getTransaction().isActive() : "EntityManager was not closed by transactional service";

        //test that the data has been stored
        session.getTransaction().begin();
        Object result = session.createQuery("from JpaTestEntity where text = :text")
                .setParameter("text", UNIQUE_TEXT)
                .getSingleResult();

        session.getTransaction().commit();

        assert result instanceof JpaTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT.equals(((JpaTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";
    }

    @Test
    public void testSimpleTransactionRollbackOnChecked() {
        try {
            injector.getInstance(TransactionalObject2.class).runOperationInTxnThrowingChecked();
        } catch(IOException e) {
            //ignore
        }

        EntityManager session = injector.getInstance(EntityManager.class);
        assert !session.getTransaction().isActive() : "EntityManager was not closed by transactional service (rollback didnt happen?)";

        //test that the data has been stored
        session.getTransaction().begin();
        List<?> result = session.createQuery("from JpaTestEntity where text = :text")
                .setParameter("text", TRANSIENT_UNIQUE_TEXT)
                .getResultList();

        session.getTransaction().commit();

        assert result.isEmpty() : "a result was returned! rollback sure didnt happen!!!";
    }

    @Test
    public void testSimpleTransactionRollbackOnCheckedExcepting() {
        Exception ex = null;
        try {
            injector.getInstance(TransactionalObject3.class).runOperationInTxnThrowingCheckedExcepting();
        } catch(IOException e) {
            //ignore
            ex = e;

        }
        assert null != ex: "Exception was not thrown by test txn-al method!";

        EntityManager session = injector.getInstance(EntityManager.class);
        assert !session.getTransaction().isActive() : "Txn was not closed by transactional service (commit didnt happen?)";

        //test that the data has been stored
        session.getTransaction().begin();
        Object result = session.createQuery("from JpaTestEntity where text = :text")
                .setParameter("text", UNIQUE_TEXT_2)
                .getSingleResult();

        session.getTransaction().commit();

        assert null != result : "a result was not returned! rollback happened anyway (exceptOn failed)!!!";
    }

    @Test
    public void testSimpleTransactionRollbackOnUnchecked() {
        try {
            injector.getInstance(TransactionalObject4.class).runOperationInTxnThrowingUnchecked();
        } catch(RuntimeException re) {
            //ignore
        }

        EntityManager session = injector.getInstance(EntityManager.class);
        assert !session.getTransaction().isActive() : "EntityManager was not closed by transactional service (rollback didnt happen?)";

        //test that the data has been stored
        session.getTransaction().begin();
        List<?> result = session.createQuery("from JpaTestEntity where text = :text")
                .setParameter("text", TRANSIENT_UNIQUE_TEXT)
                .getResultList();

        session.getTransaction().commit();

        assert result.isEmpty() : "a result was returned! rollback sure didnt happen!!!";
    }


    @Transactional
    public static class TransactionalObject {
        @Inject EntityManager session;

        public void runOperationInTxn() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT);
            session.persist(entity);
        }

    }


    @Transactional
    public static class TransactionalObject4 {
        @Inject EntityManager session;

        @Transactional
        public void runOperationInTxnThrowingUnchecked() {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(TRANSIENT_UNIQUE_TEXT);
            session.persist(entity);

            throw new IllegalStateException();
        }
    }


    @Transactional(rollbackOn = IOException.class, exceptOn = FileNotFoundException.class)
    public static class TransactionalObject3 {
        @Inject EntityManager session;

        public void runOperationInTxnThrowingCheckedExcepting() throws IOException {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            session.persist(entity);

            throw new FileNotFoundException();
        }
    }


    @Transactional(rollbackOn = IOException.class)
    public static class TransactionalObject2 {
        @Inject EntityManager session;

        public void runOperationInTxnThrowingChecked() throws IOException {
            JpaTestEntity entity = new JpaTestEntity();
            entity.setText(TRANSIENT_UNIQUE_TEXT);
            session.persist(entity);

            throw new IOException();
        }
    }
}