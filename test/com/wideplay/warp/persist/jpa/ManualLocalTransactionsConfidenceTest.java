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

package com.wideplay.warp.persist.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.Transactional;
import com.wideplay.warp.persist.UnitOfWork;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.Date;

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
        injector = Guice.createInjector(PersistenceService.usingJpa()
            .across(UnitOfWork.TRANSACTION)
            .forAll(Matchers.any())
            .buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        //tell Warp the name of the jpa persistence unit
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                });

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();
    }

    @AfterClass
    public final void postClass() {
        injector.getInstance(EntityManagerFactory.class).close();
    }


    @Test
    public void testThrowingCleanupInterceptorConfidence() {
        Exception e = null;
        try {
            injector.getInstance(TransactionalObject.class).runOperationInTxn();
        } catch(RuntimeException re) {
            e = re;
            System.out.println("\n\n******************************* EXPECTED EXCEPTION NORMAL TEST BEHAVIOR **********");
            re.printStackTrace(System.out);
            System.out.println("\n\n**********************************************************************************");
        }

        assert null != e : "No exception was thrown!";
        assert e instanceof PersistenceException : "Exception thrown was not what was expected (i.e. commit-time)";
        // TODO commented out because of multiple modules refactoring
        //assert null == EntityManagerFactoryHolder.checkCurrentEntityManager() : "EM was open when it should have been closed on roll back";
    }

//    @Test(dependsOnMethods = "testThrowingCleanupInterceptorConfidence")
//    public void testThrowingCleanupRestorationConfidence() {
//        Exception e = null;
//        try {
//            injector.getInstance(TransactionalObject.class).runOperationInTxn();
//        } catch(RuntimeException re) {
//            e = re;
//            System.out.println("\n\n******************************* EXPECTED EXCEPTION NORMAL TEST BEHAVIOR **********");
//            re.printStackTrace(System.out);
//            System.out.println("\n\n**********************************************************************************");
//        }
//
//        assert null != e : "No exception was thrown!";
//        assert e instanceof PersistenceException : "Exception thrown was not what was expected (i.e. commit-time)";
//        assert !injector.getInstance(EntityManager.class).getTransaction().isActive() : "Session was open when it should have been closed on roll back";
//    }




    public static class TransactionalObject {
        @Inject
        EntityManager em;

        @Transactional
        public void runOperationInTxn() {
            JpaParentTestEntity entity = new JpaParentTestEntity();
            JpaTestEntity child = new JpaTestEntity();

            child.setText(UNIQUE_TEXT_3);
            em.persist(child);

            entity.getChildren().add(child);
            em.persist(entity);

            entity = new JpaParentTestEntity();
            entity.getChildren().add(child);
            em.persist(entity);
        }
    }
}
