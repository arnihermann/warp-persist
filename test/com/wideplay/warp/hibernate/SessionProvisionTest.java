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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;

/**
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 1/06/2007
 * Time: 11:40:36
 *
 * A test around providing sessions (starting, closing etc.)
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class SessionProvisionTest {
    private Injector injector;

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
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));
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
    public void testSessionLifecyclePerTxn() {
        //obtain session
        Session session = injector.getInstance(Session.class);
        assert session.isOpen() : "session is not open!";

        session.beginTransaction();
        assert session.getTransaction().isActive() : "no active txn!";

        //obtain same session again (bound to txn)
        HibernateTestEntity te = new HibernateTestEntity();
        session.persist(te);

        assert session.contains(te) : "Persisting object failed";
        assert injector.getInstance(Session.class).contains(te) : "Duplicate sessions crossing-scope";

        session.getTransaction().commit();

        assert !session.isOpen() : "Session did not close on txn commit--current_session_context_class=thread may not be set";

        //try to start a new session in a new txn
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        assert !session.contains(te) : "Session wasnt closed and reopened properly (persistent object persists)";

        session.getTransaction().rollback();

        assert !session.isOpen() : "Session not closed on rollback!!";
    }



}
