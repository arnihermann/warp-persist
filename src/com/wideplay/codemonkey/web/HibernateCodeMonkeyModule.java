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
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
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
