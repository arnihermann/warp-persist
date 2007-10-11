package com.wideplay.codemonkey.web;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.wideplay.codemonkey.model.SourceArtifact;
import com.wideplay.codemonkey.web.startup.InitializerWeb;
import com.wideplay.warp.Warp;
import com.wideplay.warp.WarpModule;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.TransactionStrategy;
import com.wideplay.warp.persist.UnitOfWork;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * Created with IntelliJ IDEA.
 * On: 29/04/2007
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 * @since 1.0
 */
public class HibernateCodeMonkeyModule implements WarpModule {

    public void configure(Warp warp) {
        warp.install(PersistenceService.usingHibernate()

                .across(UnitOfWork.REQUEST)
                .transactedWith(TransactionStrategy.LOCAL)
                .forAll(Matchers.any())

                .buildModule()
        );
        
        warp.install(new AbstractModule() {

            protected void configure() {
                bind(Configuration.class).toInstance(new AnnotationConfiguration()
                    .addAnnotatedClass(SourceArtifact.class)
                    .setProperties(InitializerWeb.loadProperties("persistence.properties")));
                
                bind(InitializerWeb.class).asEagerSingleton();
            }
        });
    }
}
