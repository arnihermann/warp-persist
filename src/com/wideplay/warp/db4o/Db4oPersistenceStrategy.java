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
package com.wideplay.warp.db4o;

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.wideplay.warp.persist.*;
import com.wideplay.warp.util.Text;
import static com.wideplay.warp.util.Text.empty;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;

/**
 * @author Robbie Vanbrabant
 */
public class Db4oPersistenceStrategy implements PersistenceStrategy {
    private final Db4oSettings db4oSettings;
    private final Class<? extends Annotation> annotation;

    private Db4oPersistenceStrategy(Db4oSettings db4oSettings, Class<? extends Annotation> annotation) {
        this.db4oSettings = db4oSettings;
        this.annotation = annotation;
    }

    public PersistenceModule getBindings(final PersistenceConfiguration config) {
        return new Db4oPersistenceModule(config);
    }

    class Db4oPersistenceModule extends AbstractPersistenceModule {
        private final PersistenceConfiguration config;
        private final PersistenceService pService;
        private final WorkManager workManager;
        private final Provider<ObjectContainer> ocp;
        private final ObjectServerProvider osp;

        Db4oPersistenceModule(PersistenceConfiguration config) {
            super(annotation);
            this.config = config;
            this.osp = new ObjectServerProvider(db4oSettings);
            this.ocp = new ObjectContainerProvider(osp);
            this.pService = new Db4oPersistenceService(osp);
            this.workManager = new Db4oWorkManager(osp);
        }

        protected void configure() {
            // Set up Db4o.
            bindSpecial(ObjectServer.class).toProvider(osp);
            bindSpecial(ObjectContainer.class).toProvider(ocp);
            bindSpecial(PersistenceService.class).toInstance(pService);
            bindSpecial(WorkManager.class).toInstance(workManager);

            MethodInterceptor txInterceptor = new Db4oLocalTxnInterceptor(ocp, config.getUnitOfWork());
            bindTransactionInterceptor(config, txInterceptor);

            // No Dynamic Finders yet.
        }

        public WorkManager getWorkManager() {
            return unitOfWorkRequest(config) ? this.workManager : null;
        }

        public PersistenceService getPersistenceService() {
            return unitOfWorkRequest(config) ? this.pService : null;
        }
    }

    public static Db4oPersistenceStrategyBuilder builder() {
        return new Db4oPersistenceStrategyBuilder();
    }

    public static class Db4oPersistenceStrategyBuilder implements PersistenceStrategyBuilder<Db4oPersistenceStrategy> {
        private String databaseFileName;
        private Configuration configuration;
        private String host;
        private String password;
        private int port;
        private String user;
        private Class<? extends Annotation> annotation;

        @Inject(optional = true)
        public void databaseFileName(@Db4Objects String databaseFileName) {
            this.databaseFileName = databaseFileName;

            Text.nonEmpty(databaseFileName, "Db4o database file name was not set; please bindConstant()" +
                    ".annotatedWith(Db4Objects.class) to a string containing the name of a Db4o database file.");
        }

        @Inject(optional = true)
        private void configuration(Configuration configuration) {
            this.configuration = configuration;
        }

        @Inject(optional = true)
        private void host(@Named(Db4Objects.HOST)String host) {
            this.host = host;

            Text.nonEmpty(host, "Please specify a valid host name.");
        }

        @Inject(optional = true)
        private void password(@Named(Db4Objects.PASSWORD)String password) {
            this.password = password;
        }

        @Inject(optional = true)
        private void port(@Named(Db4Objects.PORT)int port) {
            if (port < 0 || port > 65535)
                throw new IllegalArgumentException("Port number was invalid (must be in range 0-65535). Was: "
                        + port);

            this.port = port;
        }

        @Inject(optional = true)
        private void user(@Named(Db4Objects.USER)String user) {
            this.user = user;
        }

        public Db4oPersistenceStrategyBuilder annotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        Db4oSettings buildDb4oSettings() {
            HostKind hostKind;
            if (databaseFileName == null) {
                if (!empty(host))
                    hostKind = HostKind.REMOTE;
                else
                    throw new IllegalStateException("Must specify either database file name: " +
                            "bindConstant().annotatedWith(Db4Objects); or a remote server host: bindConstant()" +
                            ".annotatedWith(Names.named(Db4Objects.HOST)).to(\"localhost\")");
            } else if (!empty(host)) {
                hostKind = HostKind.LOCAL;
            } else {
                hostKind = HostKind.FILE;
            }
            return new Db4oSettings(user, password, host, port, configuration, hostKind, databaseFileName);
        }

        public Db4oPersistenceStrategy build() {
            // TODO validate
            // TODO detect untouched builder and pass in null?
            return new Db4oPersistenceStrategy(null, this.annotation);
        }
    }
}
