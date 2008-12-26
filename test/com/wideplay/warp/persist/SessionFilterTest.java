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
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.persist.hibernate.HibernateTestEntity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.ManagedSessionContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/** Unit tests the SPR filter. */
public class SessionFilterTest {
    @BeforeClass
    public void pre() {
        // protect against sloppy tests elsewhere
        PersistenceFilter.clearWorkManagers();
    }

    @AfterClass
    public void post() { }

    @AfterMethod
    public void cleanSessionFilter() {
        PersistenceFilter.clearWorkManagers();
    }

    @Test(expectedExceptions = ServletException.class)
    public final void testNoWorkManagersFailure() throws IOException, ServletException {
        PersistenceFilter spr = new PersistenceFilter();
        spr.doFilter(null, null, null);
    }

    @Test
    public final void testUseWorkManager() throws IOException, ServletException {
        PersistenceFilter spr = new PersistenceFilter();
        final ValidatableWorkManager workManager = new ValidatableWorkManager();
        PersistenceFilter.registerWorkManager(workManager);
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assert workManager.beginCalled;
                assert !workManager.endCalled;
            }
        };
        spr.doFilter(null, null, chain);
        assert workManager.beginCalled;
        assert workManager.endCalled;
    }

    @Test
    public final void testWorkManagerBeginThrowsException() throws IOException, ServletException {
        PersistenceFilter spr = new PersistenceFilter();
        final ValidatableWorkManager workManager1 = new ValidatableWorkManager();
        final ValidatableWorkManager workManager2 = new ValidatableWorkManager() {
            public void beginWork() {
                beginCalled = true;
                throw new RuntimeException();
            }
        };
        
        final ValidatableWorkManager workManager3 = new ValidatableWorkManager();
        PersistenceFilter.registerWorkManager(workManager1);
        PersistenceFilter.registerWorkManager(workManager2);
        PersistenceFilter.registerWorkManager(workManager3);
        
        try {
            spr.doFilter(null, null, null);
            throw new AssertionError();
        } catch (RuntimeException e) {}

        assert workManager1.beginCalled;
        assert workManager1.endCalled;
        
        assert workManager2.beginCalled;
        assert !workManager2.endCalled;

        assert !workManager3.beginCalled;
        assert !workManager3.endCalled;
    }

    @Test
    public final void testWorkManagerEndThrowsException() throws IOException, ServletException {
        PersistenceFilter spr = new PersistenceFilter();
        final ValidatableWorkManager workManager1 = new ValidatableWorkManager();
        final ValidatableWorkManager workManager2 = new ValidatableWorkManager() {
            public void endWork() {
                endCalled = true;
                throw new RuntimeException("eep");
            }
        };

        final ValidatableWorkManager workManager3 = new ValidatableWorkManager();
        PersistenceFilter.registerWorkManager(workManager1);
        PersistenceFilter.registerWorkManager(workManager2);
        PersistenceFilter.registerWorkManager(workManager3);

        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assert workManager1.beginCalled;
                assert workManager2.beginCalled;
                assert workManager3.beginCalled;
            }
        };

        try {
            spr.doFilter(null, null, chain);
            throw new AssertionError();
        } catch (RuntimeException e) {}

        assert workManager1.endCalled;
        assert workManager2.endCalled;
        assert workManager3.endCalled;
    }

    @Test
    public final void testWorkManagerBeginAndEndThrowException() throws IOException, ServletException {
        PersistenceFilter spr = new PersistenceFilter();
        final ValidatableWorkManager workManager1 = new ValidatableWorkManager() {
            public void endWork() {
                endCalled = true;
                throw new RuntimeException("eep");
            }
        };
        final ValidatableWorkManager workManager2 = new ValidatableWorkManager() {
            public void beginWork() {
                beginCalled = true;
                throw new RuntimeException();
            }
        };

        final ValidatableWorkManager workManager3 = new ValidatableWorkManager();
        
        PersistenceFilter.registerWorkManager(workManager1);
        PersistenceFilter.registerWorkManager(workManager2);
        PersistenceFilter.registerWorkManager(workManager3);

        try {
            spr.doFilter(null, null, null);
            throw new AssertionError();
        } catch (RuntimeException e) {}
        
        assert workManager1.beginCalled;
        assert workManager2.beginCalled;
        assert !workManager3.beginCalled;
        assert workManager1.endCalled;
        assert !workManager2.endCalled;
        assert !workManager3.endCalled;
    }

    @Test
    public final void testUseRealWorkManager() throws IOException, ServletException {
        final Injector injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.REQUEST)
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
        injector.getInstance(PersistenceService.class).start();

        PersistenceFilter spr = new PersistenceFilter();
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assert ManagedSessionContext.hasBind(injector.getInstance(SessionFactory.class));
            }
        };
        spr.doFilter(null, null, chain);
        assert !ManagedSessionContext.hasBind(injector.getInstance(SessionFactory.class));

        injector.getInstance(PersistenceService.class).shutdown();
    }

    static class ValidatableWorkManager implements WorkManager {
        boolean beginCalled;
        boolean endCalled;
        public void beginWork() {
            beginCalled = true;
        }
        public void endWork() {
            endCalled = true;
        }
    }
}