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

package com.wideplay.warp.persist.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.persist.PersistenceService;
import static com.wideplay.warp.persist.TransactionType.READ_ONLY;
import static com.wideplay.warp.persist.TransactionType.READ_WRITE;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.context.ManagedSessionContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * On: 2/06/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class ManualLocalReadOnlyTransactionsTest {
    private Injector injector;
    private static final String UNIQUE_TEXT_PERSISTENT = "some unique text originally" + new Date();
    private static final String UNIQUE_TEXT_PERSISTENT2 = "some unique text taht should change" + new Date();
    private static final String UNIQUE_TEXT_TRANSIENT = "some unique text that you should never see!" + new Date();

    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.REQUEST)
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


    @AfterClass
    void post() {
        final SessionFactory sessionFactory = injector.getInstance(SessionFactory.class);
        sessionFactory.close();

        ManagedSessionContext.unbind(sessionFactory);
    }

    @Test
    public void testSimpleCrossTxnWork() {
        Session session1 = injector.getInstance(SessionFactory.class).openSession();
        ManagedSessionContext.bind(session1);
        final TransactionalObject txnal = injector.getInstance(TransactionalObject.class);
        final HibernateTestEntity entity = txnal.runOperationInTxn();

        //save the id
        txnal.id = entity.getId();

        assert session1.contains(entity) : "Entity was not persisted!";
        assert UNIQUE_TEXT_PERSISTENT.equals(entity.getText()) : "entity was not stored correctly";

        //run read-only txn
        txnal.runReadOnlyTxn();

        assert UNIQUE_TEXT_TRANSIENT.equals(entity.getText()) : "entity dirty state was not modified in read-only txn";
        Query query = session1.createQuery("from HibernateTestEntity where text = :text");
        query.setParameter("text", UNIQUE_TEXT_TRANSIENT);

        assert null == query.uniqueResult() : "Text from read-only txn was found in persistent store!";

        query = session1.createQuery("from HibernateTestEntity where text = :text");
        query.setParameter("text", UNIQUE_TEXT_PERSISTENT);

        assert null != query.uniqueResult() : "Read-only txn affected persistent store!";



        //run read-write txn
        txnal.runReadWriteTxn();

        assert UNIQUE_TEXT_PERSISTENT2.equals(entity.getText()) : "entity was not modified in read-write txn!!";
        query = session1.createQuery("from HibernateTestEntity where text = :text");
        query.setParameter("text", UNIQUE_TEXT_PERSISTENT);

        assert null == query.uniqueResult() : "Text from original txn was found in persistent store!";




        //run read-only txn again, after having run a read-write txn
        txnal.runReadOnlyTxn();

        assert UNIQUE_TEXT_TRANSIENT.equals(entity.getText()) : "entity appears modified in read-only txn!!";
        query = session1.createQuery("from HibernateTestEntity where text = :text");
        query.setParameter("text", UNIQUE_TEXT_TRANSIENT);

        assert null == query.uniqueResult() : "Text from read-only txn was found in persistent store!";
        session1.close();

        //open a new session
        session1 = injector.getInstance(SessionFactory.class).openSession();
        ManagedSessionContext.bind(session1);

        //assert that the persistent copy of the entity is as it should be
        query = session1.createQuery("from HibernateTestEntity where text = :text");
        query.setParameter("text", UNIQUE_TEXT_PERSISTENT2);

        final HibernateTestEntity persistentCopy = (HibernateTestEntity) query.uniqueResult();
        assert null != persistentCopy : "Text from read-only txn was found in persistent store!";
        System.out.println(persistentCopy.getText());
        assert UNIQUE_TEXT_PERSISTENT2.equals(persistentCopy.getText()) : "Persistent copy of entity appears modified by read-only txn";

        session1.close();
    }



    public static class TransactionalObject {
        @Inject
        org.hibernate.Session session;
        private Long id;

        @Transactional
        public HibernateTestEntity runOperationInTxn() {
            HibernateTestEntity entity = new HibernateTestEntity();
            entity.setText(UNIQUE_TEXT_PERSISTENT);
            session.persist(entity);


            return entity;
        }

        @Transactional(type = READ_ONLY)
        public void runReadOnlyTxn() {
            HibernateTestEntity entity = (HibernateTestEntity) session.get(HibernateTestEntity.class, id);

            entity.setText(UNIQUE_TEXT_TRANSIENT);
        }

        @Transactional(type = READ_WRITE)
        public void runReadWriteTxn() {
            HibernateTestEntity entity = (HibernateTestEntity) session.get(HibernateTestEntity.class, id);

            entity.setText(UNIQUE_TEXT_PERSISTENT2);
        }
    }
}