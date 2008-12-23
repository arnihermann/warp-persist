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

import com.db4o.ObjectServer;
import com.google.inject.Provider;
import com.wideplay.warp.persist.PersistenceService;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * @author Jeffrey Chung (jeffreymchung@gmail.com)
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 */
@ThreadSafe
class Db4oPersistenceService extends PersistenceService {
    private final Provider<ObjectServer> objectServerProvider;
    @GuardedBy("this")
    private State state = State.FRESH;

    public Db4oPersistenceService(Provider<ObjectServer> objectServerProvider) {
        this.objectServerProvider = objectServerProvider;
    }

    public synchronized void start() {
        // Because we implement the provider using lazy loading
        // we do not need to check for the state here. It will
        // simply return the closed EntityManagerFactory
        objectServerProvider.get();
        this.state = State.STARTED;
    }

    public synchronized void shutdown() {
        // Not sure if we need the check, but for now
        // it's better to be safe than sorry.
        if (State.STARTED == this.state) {
            objectServerProvider.get().close();
            this.state = State.STOPPED;
        }
    }

    private static enum State {
        FRESH, STARTED, STOPPED
    }
}
