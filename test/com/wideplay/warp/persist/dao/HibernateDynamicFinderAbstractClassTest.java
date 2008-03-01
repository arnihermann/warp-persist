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

package com.wideplay.warp.persist.dao;

import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.codemonkey.web.startup.Initializer;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;

/**
 * Created by IntelliJ IDEA.
 * User: Dhanji R. Prasanna (dhanji@gmail.com)
 * Date: 4/06/2007
 * Time: 15:57:24
 * <p/>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
public class HibernateDynamicFinderAbstractClassTest {
    private Injector injector;
    private static final String TEXT_1 = "unique text1" + new Date();
    private static final String TEXT_2 = "unique text2" + new Date();
    private static final String TEST_STRING_THAT_SHOULD_BE_PASSED_BACK_UNINTERCEPTED = "test string that should be passed back unintercepted";


    @BeforeMethod
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
                .across(UnitOfWork.TRANSACTION)
                .addAccessor(AbstractDF.class)
                .buildModule(),

                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration()
                            .addAnnotatedClass(HibernateTestEntityTxnal.class)
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));
                    }
                });

        injector.getInstance(PersistenceService.class).start();
    }

    @AfterMethod
    public void post() {
        injector.getInstance(SessionFactory.class).close();
        injector = null;
    }

    @Test
    public void testDDDDynamicFinderListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntityTxnal entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        HibernateTestEntityTxnal dddAccessor = injector.getInstance(HibernateTestEntityTxnal.class);
        List<HibernateTestEntityTxnal> results = dddAccessor.listAll();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }


    @Test
    public void testAbstractDynamicFinderListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntityTxnal entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();

        List<HibernateTestEntityTxnal> results;
        AbstractDF abstractDF;

        try {
            abstractDF = injector.getInstance(AbstractDF.class);
            results = abstractDF.listAll();
        } finally {
            session.getTransaction().commit();
        }

        //assert that non-DFs are passed thru
        assert TEST_STRING_THAT_SHOULD_BE_PASSED_BACK_UNINTERCEPTED.equals(abstractDF.passThruMethod()) : "non-abstract method was intercepted!!";

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert abstractDF.someOtherMethod() : "non-abstract primitive method was intercepted!";

        assert results.get(0).getText().equals(TEXT_1) || results.get(0).getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(TEXT_1) || results.get(1).getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    @Test(expectedExceptions = AbstractMethodError.class)
    public void testAbstractNonDynamicFinderFail() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntityTxnal entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntityTxnal();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();

        List<HibernateTestEntityTxnal> results;
        AbstractDF abstractDF;

        try {
            abstractDF = injector.getInstance(AbstractDF.class);
            results = abstractDF.listAll();
        } finally {
            session.getTransaction().commit();
        }

        //assert that non-DFs are passed thru
        assert TEST_STRING_THAT_SHOULD_BE_PASSED_BACK_UNINTERCEPTED.equals(abstractDF.passThruMethod()) : "non-abstract method was intercepted!!";

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        assert abstractDF.listSome() != null;
    }

    public static abstract class AbstractDF {

        public String passThruMethod() {
            return TEST_STRING_THAT_SHOULD_BE_PASSED_BACK_UNINTERCEPTED;
        }

        
        public abstract List<HibernateTestEntityTxnal> listSome();


        @Finder(query = "from HibernateTestEntityTxnal")
        public abstract List<HibernateTestEntityTxnal> listAll();

        public boolean someOtherMethod() {
            return true;
        }
    }
}
