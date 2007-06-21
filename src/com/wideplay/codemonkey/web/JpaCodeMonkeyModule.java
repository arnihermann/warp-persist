package com.wideplay.codemonkey.web;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.web.startup.Initializer;
import com.wideplay.warp.Warp;
import com.wideplay.warp.WarpModule;
import com.wideplay.warp.jpa.JpaUnit;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;

/**
 * Created with IntelliJ IDEA.
 * On: 29/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class JpaCodeMonkeyModule implements WarpModule {

    public void configure(Warp warp) {
        warp.install(PersistenceService.usingJpa()

                .across(UnitOfWork.REQUEST)
                .transactedWith(TransactionStrategy.LOCAL)
                .forAll(Matchers.any())

                .buildModule()
        );

        warp.install(new AbstractModule() {

            protected void configure() {
                bindConstant().annotatedWith(JpaUnit.class).to("testUnit");

                bind(Initializer.class).asEagerSingleton();
            }
        });
    }
}
