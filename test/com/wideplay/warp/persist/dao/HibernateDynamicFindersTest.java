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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.hibernate.HibernateTestEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
public class HibernateDynamicFindersTest {
    private Injector injector;
    private static final String TEXT_1 = "unique text1" + new Date();
    private static final String TEXT_2 = "unique text2" + new Date();

    private static final String UNIQUE_TEXT_1 = "some other unique text1" + new Date();
    private static final String UNIQUE_TEXT_2 = "and different unique text2" + new Date();

    @BeforeClass
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
                .across(UnitOfWork.TRANSACTION)
                .addAccessor(HibernateTestAccessor.class)
                .buildModule(),

                new AbstractModule() {

                    protected void configure() {
                        bind(Configuration.class).toInstance(new AnnotationConfiguration()
                            .addAnnotatedClass(HibernateTestEntity.class)
                            .setProperties(Initializer.loadProperties("spt-persistence.properties")));
                    }
                });

        injector.getInstance(PersistenceService.class).start();
    }

    @AfterClass
    public void post() {
        injector.getInstance(SessionFactory.class).close();
        injector = null;
    }

    @Test public void testDynamicFinderListAllMatching() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(UNIQUE_TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(UNIQUE_TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestEntity accessor = injector.getInstance(HibernateTestEntity.class);
        List<HibernateTestEntity> results = accessor.listAllMatching(UNIQUE_TEXT_1, UNIQUE_TEXT_2);
        session.getTransaction().commit();

        //assert them
        assert results.size() == 2 : "atleast 2 results expected! was: " + results.size();

        assert results.get(0).getText().equals(UNIQUE_TEXT_1) || results.get(0).getText().equals(UNIQUE_TEXT_2) : "attribs not persisted correctly";
        assert results.get(1).getText().equals(UNIQUE_TEXT_1) || results.get(1).getText().equals(UNIQUE_TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        List<HibernateTestEntity> results = accessor.listAll();
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();

        System.out.println("Results: " + results);

    }


    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorListAllWithPaging() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);
        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);
        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        List<HibernateTestEntity> results = accessor.listAll(1);
        session.getTransaction().commit();

        //assert them
        assert results.size() == 1 : "only 1 result expected! was: " + results.size();
    }

    //an accessor is an interface bound to web-ext with finder methods
//    @Test TODO fix 
    public void testDynamicAccessorListAllAsArray() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity[] results = accessor.listAllAsArray();
        session.getTransaction().commit();

        //assert them
        assert results.length >= 2 : "atleast 2 results expected! was: " + results.length;

        assert results[0].getText().equals(TEXT_1) || results[0].getText().equals(TEXT_2) : "attribs not persisted correctly";
        assert results[1].getText().equals(TEXT_1) || results[1].getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorNamedQueryListAll() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        List<HibernateTestEntity> results = accessor.listEverything();
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 2 : "atleast 2 results expected! was: " + results.size();


    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithBoundParamsAsSet() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        Set<HibernateTestEntity> results = accessor.find(TEXT_1);
        session.getTransaction().commit();

        //assert them
        assert results.size() >= 1 : "atleast 1 results expected! was: " + results.size();

        for (HibernateTestEntity res : results)
            assert res.getText().equals(TEXT_1) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithNamedBoundParamsSingleFetch() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity result = accessor.fetch(entity.getId());
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorFinderWithRawBoundParamsSingleFetch() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity result = accessor.fetchById(entity.getId(), 1, TEXT_2);
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test(expectedExceptions = ClassCastException.class)
    public void testDynamicAccessorWithList() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity result = accessor.fetchByIdUnnamedList(Collections.singletonList(entity.getId()));
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

        //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorWithNamedList() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity result = accessor.fetchByIdList(Collections.singletonList(entity.getId()));
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }

    //an accessor is an interface bound to web-ext with finder methods
    @Test public void testDynamicAccessorWithArray() {
        Session session = injector.getInstance(Session.class);
        session.beginTransaction();

        HibernateTestEntity entity = new HibernateTestEntity();
        entity.setText(TEXT_1);
        session.save(entity);

        entity = new HibernateTestEntity();
        entity.setText(TEXT_2);
        session.save(entity);

        session.getTransaction().commit();

        //now test our magic finders
        session = injector.getInstance(Session.class);
        session.beginTransaction();
        HibernateTestAccessor accessor = injector.getInstance(HibernateTestAccessor.class);
        HibernateTestEntity result = accessor.fetchByIdArray(new Long[]{entity.getId()});
        session.getTransaction().commit();

        //assert them
        assert result != null : "atleast 1 results expected!";

        assert result.getText().equals(TEXT_2) : "attribs not persisted correctly";

    }
}
