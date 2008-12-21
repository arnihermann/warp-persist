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

package com.wideplay.warp.persist;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import static com.google.inject.matcher.Matchers.*;
import com.wideplay.codemonkey.web.startup.Initializer;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

public class AdvancedTransactionsConfigTest {
    private Injector injector;
    private static final String UNIQUE_TEXT = "some unique text" + new Date();
    private static final String UNIQUE_TEXT_2 = UNIQUE_TEXT + "_2";
    private static final String TRANSIENT_UNIQUE_TEXT = "some other unique text" + new Date();

    @BeforeMethod
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.TRANSACTION)
                .forAll(annotatedWith(Transactional.class), any())
                .forAll(not(annotatedWith(Transactional.class)),
                        annotatedWith(Transactional.class))
            .buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration()
                            .addAnnotatedClass(HibernateAdvancedTxTestEntity.class)
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));
                    }
                }
        );

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @AfterMethod
    void post() {
        injector.getInstance(PersistenceService.class).shutdown();
        injector = null;
    }

    @Test
    public void testSimpleMethodLevelTransaction() {
        injector.getInstance(MethodLevelTransactionalObject.class).runOperationInTxn();

        Session session = injector.getInstance(Session.class);
        assert !session.getTransaction().isActive() : "Session was not closed by transactional service";

        //test that the data has been stored
        session.beginTransaction();
        Object result = session.createCriteria(HibernateAdvancedTxTestEntity.class).add(Expression.eq("text", UNIQUE_TEXT)).uniqueResult();
        session.getTransaction().commit();

        assert result instanceof HibernateAdvancedTxTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT.equals(((HibernateAdvancedTxTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";
        
        try {
            injector.getInstance(MethodLevelTransactionalObject.class).runOperationWithoutTxn();
            fail("Transaction was there while it shouldn't have been.");
        } catch (HibernateException he) {
        }

        injector.getInstance(ClassLevelTransactionalObject.class).runOperationInTxn();

        session = injector.getInstance(Session.class);
        assert !session.getTransaction().isActive() : "Session was not closed by transactional service";

        //test that the data has been stored
        session.beginTransaction();
        result = session.createCriteria(HibernateAdvancedTxTestEntity.class).add(Expression.eq("text", UNIQUE_TEXT_2)).uniqueResult();
        session.getTransaction().commit();

        assert result instanceof HibernateAdvancedTxTestEntity : "odd result returned fatal";

        assert UNIQUE_TEXT_2.equals(((HibernateAdvancedTxTestEntity)result).getText()) : "queried entity did not match--did automatic txn fail?";

    }

    public static class MethodLevelTransactionalObject {
        @Inject Session session;

        @Transactional
        public void runOperationInTxn() {
            HibernateAdvancedTxTestEntity entity = new HibernateAdvancedTxTestEntity();
            entity.setText(UNIQUE_TEXT);
            session.persist(entity);
        }

        public void runOperationWithoutTxn() {
            HibernateAdvancedTxTestEntity entity = new HibernateAdvancedTxTestEntity();
            entity.setText(TRANSIENT_UNIQUE_TEXT);
            session.persist(entity);
        }
    }

    @Transactional
    public static class ClassLevelTransactionalObject {
        @Inject Session session;

        public void runOperationInTxn() {
            HibernateAdvancedTxTestEntity entity = new HibernateAdvancedTxTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            session.persist(entity);
        }
    }
}