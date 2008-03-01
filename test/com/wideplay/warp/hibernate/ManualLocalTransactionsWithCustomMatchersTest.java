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

package com.wideplay.warp.hibernate;

import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.codemonkey.web.startup.Initializer;

import java.util.Date;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.context.ManagedSessionContext;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * This test is identical to ManualLocalTransactionsTest but with a custom method matcher
 * instead of the traditional @Transactional annotation.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class ManualLocalTransactionsWithCustomMatchersTest {
    private Injector injector;
    private static final String UNIQUE_TEXT = "some unique text12121" + new Date();
    private static final String UNIQUE_TEXT_2 = "some other unique text121212" + new Date();
    private static final String UNIQUE_TEXT_3 = "CONSTRAINT_VIOLATING some other unique text" + new Date();

    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.REQUEST)
            .transactedWith(TransactionStrategy.LOCAL)
            .forAll(Matchers.subclassesOf(TransactionalObject.class), Matchers.any())
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


    @AfterClass
    void post() {
        injector.getInstance(SessionFactory.class).close();
    }

    @Test
    public void testSimpleCrossTxnWork() {
        org.hibernate.classic.Session session1 = injector.getInstance(SessionFactory.class).openSession();
        ManagedSessionContext.bind(session1);
        HibernateTestEntity entity = injector.getInstance(ManualLocalTransactionsWithCustomMatchersTest.TransactionalObject.class).runOperationInTxn();
        injector.getInstance(ManualLocalTransactionsWithCustomMatchersTest.TransactionalObject.class).runOperationInTxn2();

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

        public HibernateTestEntity runOperationInTxn() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT);
            session.persist(entity);

            return entity;
        }

        public void runOperationInTxn2() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            session.persist(entity);
        }

        public void runOperationInTxn3() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT_2);
            session.persist(entity);
        }

    }
}
