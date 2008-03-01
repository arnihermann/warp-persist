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
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class ManualLocalTransactionsConfidenceTest {
    private Injector injector;
    private static final String UNIQUE_TEXT_3 = ManualLocalTransactionsConfidenceTest.class.getSimpleName()
            + "CONSTRAINT_VIOLATING some other unique text" + new Date();

    @BeforeClass
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
                            .addAnnotatedClass(HibernateParentTestEntity.class)
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
    public void testThrowingCleanupInterceptorConfidence() {
        Exception e = null;
        try {
            injector.getInstance(ManualLocalTransactionsConfidenceTest.TransactionalObject.class).runOperationInTxn();
        } catch(RuntimeException re) {
            e = re;
            System.out.println("\n\n******************************* EXPECTED EXCEPTION NORMAL TEST BEHAVIOR **********");
            re.printStackTrace(System.out);
            System.out.println("\n\n**********************************************************************************");
        }

        assert null != e : "No exception was thrown!";
        assert e instanceof HibernateException : "Exception thrown was not what was expected (i.e. commit-time)";
        assert !injector.getInstance(Session.class).getTransaction().isActive() : "Session was open when it should have been closed on roll back";
    }




    public static class TransactionalObject {
        @Inject
        Session session;

        @Transactional
        public void runOperationInTxn() {
            HibernateParentTestEntity entity = new HibernateParentTestEntity();
            HibernateTestEntity child = new HibernateTestEntity();

            child.setText(UNIQUE_TEXT_3);
            session.persist(child);

            entity.getChildren().add(child);
            session.persist(entity);

            entity = new HibernateParentTestEntity();
            entity.getChildren().add(child);
            session.persist(entity);
        }
    }
}
