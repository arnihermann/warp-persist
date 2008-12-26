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
import com.google.inject.matcher.Matchers;
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

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dhanji
 * Date: Oct 9, 2007
 * Time: 1:44:56 PM
 *
 * @author Dhanji R. Prasanna (dhanji gmail com)
 */
public class HibernateAbstractClassDFTest {
    private Injector injector;

    @BeforeClass
    public final void pre() {
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.TRANSACTION)
            .addAccessor(AbstractWithDF.class)
            .forAll(Matchers.any())

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
    public final void post() {
        injector.getInstance(SessionFactory.class).close();
        injector = null;
    }

    @Test
    public final void testAbstractClassDynamicFinder() {
        AbstractWithDF withDF = injector.getInstance(AbstractWithDF.class);

        assert null != withDF : "no instance returned";

        Session session = injector.getInstance(Session.class);
        session.beginTransaction();
        assert null != withDF.listAll();
        session.getTransaction().commit();
    }

    public static abstract class AbstractWithDF {

        @Finder(query = "from HibernateTestEntity")
        public abstract List<HibernateTestEntity> listAll();
    }
}
