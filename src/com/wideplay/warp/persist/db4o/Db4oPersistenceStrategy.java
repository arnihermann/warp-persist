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
package com.wideplay.warp.persist.db4o;

import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.db4o.config.Configuration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.wideplay.warp.persist.*;
import com.wideplay.warp.persist.internal.Text;
import com.wideplay.warp.persist.internal.InternalWorkManager;
import static com.wideplay.warp.persist.internal.Text.empty;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

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
        // No Dynamic Finders yet.
        if (config.getAccessors().size() > 0) {
            throw new UnsupportedOperationException("Dynamic Finders or Accessors are not supported with DB4O. " +
                    "Please remove all configured Accessor interfaces.");
        }
        return new Db4oPersistenceModule(config);
    }

    class Db4oPersistenceModule extends AbstractPersistenceModule {
        private final PersistenceConfiguration config;
        private final PersistenceService pService;
        private final WorkManager workManager;
        private final Provider<ObjectContainer> ocp;
        private final ObjectServerProvider osp;
        private InternalWorkManager<ObjectContainer> iwm;

        Db4oPersistenceModule(PersistenceConfiguration config) {
            super(config, annotation);
            this.config = config;
            this.osp = new ObjectServerProvider(db4oSettings);
            iwm = new Db4oInternalWorkManager(osp);
            this.ocp = new ObjectContainerProvider(iwm);
            this.pService = new Db4oPersistenceService(osp);
            this.workManager = new Db4oWorkManager(iwm);
        }

        protected void configure() {
            bindWithUnitAnnotation(ObjectServer.class).toProvider(osp);
            
            bindWithUnitAnnotation(ObjectContainer.class).toProvider(ocp);
            bindWithUnitAnnotation(PersistenceService.class).toInstance(pService);
            bindWithUnitAnnotation(WorkManager.class).toInstance(workManager);

            MethodInterceptor txInterceptor = new Db4oLocalTxnInterceptor(iwm, config.getUnitOfWork());
            bindTransactionInterceptor(txInterceptor);

            if (binder().currentStage() == Stage.DEVELOPMENT) {
                MethodInterceptor throwingMethodInterceptor = new MethodInterceptor() {
                    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                        throw new UnsupportedOperationException("Dynamic Finders are not supported with DB4O. " +
                                "Remove the @Finder annotations and make sure to use unit annotations " +
                                "when using multiple modules (avoids interception conflicts).");
                    }
                };
                bindFinderInterceptor(throwingMethodInterceptor);
            }
        }

        public void visit(PersistenceModuleVisitor visitor) {
            if (unitOfWorkRequest()) {
                visitor.publishWorkManager(this.workManager);
                visitor.publishPersistenceService(this.pService);
            }
        }
    }

    public static Db4oPersistenceStrategyBuilder builder() {
        return new Db4oPersistenceStrategyBuilder();
    }

    // Do not change to be non-static or Guice 2.0 will not be able to inject it.
    public static class Db4oPersistenceStrategyBuilder implements PersistenceStrategyBuilder<Db4oPersistenceStrategy> {
        private boolean untouched = true;
        private String databaseFileName;
        private Configuration configuration;
        private String host;
        private String password;
        private int port;
        private String user;
        private Class<? extends Annotation> annotation;

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder databaseFileName(@Db4Objects String databaseFileName) {
            this.databaseFileName = databaseFileName;

            Text.nonEmpty(databaseFileName, "Db4o database file name was not set; please bindConstant()" +
                    ".annotatedWith(Db4Objects.class) to a string containing the name of a Db4o database file.");
            this.untouched = false;
            return this;
        }

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder configuration(Configuration configuration) {
            this.configuration = configuration;
            this.untouched = false;
            return this;
        }

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder host(@Named(Db4Objects.HOST)String host) {
            this.host = host;

            Text.nonEmpty(host, "Please specify a valid host name.");
            this.untouched = false;
            return this;
        }

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder password(@Named(Db4Objects.PASSWORD)String password) {
            this.password = password;
            this.untouched = false;
            return this;
        }

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder port(@Named(Db4Objects.PORT)int port) {
            if (port < 0 || port > 65535)
                throw new IllegalArgumentException("Port number was invalid (must be in range 0-65535). Was: "
                        + port);

            this.port = port;
            this.untouched = false;
            return this;
        }

        @Inject(optional = true)
        public Db4oPersistenceStrategyBuilder user(@Named(Db4Objects.USER)String user) {
            this.user = user;
            this.untouched = false;
            return this;
        }

        public Db4oPersistenceStrategyBuilder annotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            this.untouched = false;
            return this;
        }

        // internal use only
        Db4oSettings buildDb4oSettings() {
            HostKind hostKind;
            if (databaseFileName == null) {
                if (!empty(host)) {
                    hostKind = HostKind.REMOTE;
                } else {
                    throw new IllegalStateException("Must specify either database file name: " +
                            "bindConstant().annotatedWith(Db4Objects); or a remote server host: bindConstant()" +
                            ".annotatedWith(Names.named(Db4Objects.HOST)).to(\"localhost\")");
                }
            } else if (!empty(host)) {
                hostKind = HostKind.LOCAL;
            } else {
                hostKind = HostKind.FILE;
            }
            return new Db4oSettings(user, password, host, port, configuration, hostKind, databaseFileName);
        }

        public Db4oPersistenceStrategy build() {
            // TODO validate more state
            return new Db4oPersistenceStrategy(untouched ? null : buildDb4oSettings(), this.annotation);
        }
    }
}
