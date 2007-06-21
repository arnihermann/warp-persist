package com.wideplay.warp.jpa;

import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;
import com.wideplay.warp.persist.TransactionStrategy;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;

import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: dprasanna
 * Date: 1/06/2007
 * Time: 11:40:36
 * <p/>
 * TODO: Describe me!
 *
 * @author dprasanna
 * @since 1.0
 */
@Test(suiteName = "jpa")
public class EntityManagerFactoryProvisionTest {
    private Injector injector;

    @BeforeTest
    public void pre() {
        injector = Guice.createInjector(PersistenceService.usingJpa()
            .across(UnitOfWork.TRANSACTION)
            .transactedWith(TransactionStrategy.LOCAL)
            .forAll(Matchers.any())
            .buildModule(),
                new AbstractModule() {

                    protected void configure() {
                        //tell Warp the name of the jpa persistence unit
                        bindConstant().annotatedWith(JpaUnit.class).to("testUnit");
                    }
                });
    }

    @AfterTest public final void post() {
        EntityManagerFactoryHolder.closeCurrentEntityManager();
    }

    @Test
    public void testSessionCreateOnInjection() {
        assert injector.getInstance(EntityManagerFactoryHolder.class).equals(injector.getInstance(EntityManagerFactoryHolder.class));

        assert injector.getInstance(JpaPersistenceService.class).equals(injector.getInstance(JpaPersistenceService.class)) : "SINGLETON VIOLATION " + JpaPersistenceService.class.getName() ;

        //startup persistence
        injector.getInstance(PersistenceService.class)
                .start();

        //obtain em
        assert injector.getInstance(EntityManager.class).isOpen() : "EM is not open!";
    }
}
