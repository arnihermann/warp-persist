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
import com.wideplay.warp.hibernate.HibernateTestEntity;
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
    private Injector injector;

    @BeforeClass
    public void pre() {
        // protect against sloppy tests elsewhere
        SessionFilter.clearWorkManagers();
        injector = Guice.createInjector(PersistenceService.usingHibernate()
            .across(UnitOfWork.REQUEST)
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
        injector.getInstance(PersistenceService.class).start();
    }

    @AfterClass
    public void post() {
        injector.getInstance(PersistenceService.class).shutdown();
    }

    @AfterMethod
    public void cleanSessionFilter() {
        SessionFilter.clearWorkManagers();
    }

    @Test
    public final void testUseWorkManager() throws IOException, ServletException {
        SessionFilter spr = new SessionFilter();
        final ValidatableWorkManager workManager = new ValidatableWorkManager();
        SessionFilter.registerWorkManager(workManager);
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
        SessionFilter spr = new SessionFilter();
        final ValidatableWorkManager workManager1 = new ValidatableWorkManager();
        final ValidatableWorkManager workManager2 = new ValidatableWorkManager() {
            public void beginWork() {
                throw new RuntimeException();
            }
        };
        SessionFilter.registerWorkManager(workManager1);
        SessionFilter.registerWorkManager(workManager2);
        
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                throw new AssertionError();
            }
        };
        try {
            spr.doFilter(null, null, chain);
            throw new AssertionError();
        } catch (RuntimeException e) {}

        assert workManager1.beginCalled;
        assert workManager1.endCalled;
        assert !workManager2.endCalled;
    }

    @Test
    public final void testUseRealWorkManager() throws IOException, ServletException {
        SessionFilter spr = new SessionFilter();
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assert ManagedSessionContext.hasBind(injector.getInstance(SessionFactory.class));
            }
        };
        spr.doFilter(null, null, chain);
        assert !ManagedSessionContext.hasBind(injector.getInstance(SessionFactory.class));
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