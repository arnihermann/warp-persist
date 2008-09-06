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

import com.db4o.Db4o;
import com.db4o.ObjectServer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.wideplay.warp.util.LazyReference;
import static com.wideplay.warp.util.Text.empty;
import net.jcip.annotations.Immutable;

/**
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 * @author Robbie Vanbrabant
 */
@Immutable
class ObjectServerProvider implements Provider<ObjectServer> {
    @Inject
    private final Injector injector = null;

    /**
     * Lazily loaded ObjectServer.
     */
    // TODO refactor this ugly mess
    private LazyReference<ObjectServer> objectServer =
            LazyReference.of(new Provider<ObjectServer>() {
                public ObjectServer get() {
                    Db4oSettings actualSettings = getSettings();

                    //validate configuration object
                    if ( (!HostKind.FILE.equals(actualSettings.getHostKind())) && null == actualSettings.getConfiguration())
                        throw new IllegalStateException("Must specify a Configuration when using " + actualSettings.getHost() + " server mode." +
                                " For starters, try: bind(Configuration.class).toInstance(Db4o.newConfiguration());");

                    //use local (i.e. open our own) object server?
                    if (HostKind.LOCAL.equals(actualSettings.getHostKind())) {
                        ObjectServer objectServer = Db4o.openServer(actualSettings.getConfiguration(), actualSettings.getDatabaseFileName(), actualSettings.getPort());

                        //auth if credentials are available
                        if (!empty(actualSettings.getUser()))
                            objectServer.grantAccess(actualSettings.getUser(), actualSettings.getPassword());

                        return objectServer;
                        //otherwise it's a simple local-file database
                    } else if (HostKind.FILE.equals(actualSettings.getHostKind())) {
                        return Db4o.openServer(actualSettings.getDatabaseFileName(), actualSettings.getPort());
                    }
                    throw new UnsupportedOperationException("Can't create ObjectServer using HostKind.REMOTE");
                }
            });
    
    private final Db4oSettings settings;

    public ObjectServerProvider(Db4oSettings settings) {
        this.settings = settings;
    }
	
	public ObjectServer get() {
		return this.objectServer.get();
	}

    Db4oSettings getSettings() {
        return this.settings != null ?
                this.settings :
                injector.getInstance(Db4oPersistenceStrategy.Db4oPersistenceStrategyBuilder.class).buildDb4oSettings();
    }
}
