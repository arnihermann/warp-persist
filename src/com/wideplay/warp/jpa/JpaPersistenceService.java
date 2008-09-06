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

package com.wideplay.warp.jpa;

import com.google.inject.Provider;
import com.wideplay.warp.persist.PersistenceService;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import javax.persistence.EntityManagerFactory;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 1.0
 */
@ThreadSafe
class JpaPersistenceService extends PersistenceService {
    private final Provider<EntityManagerFactory> emfProvider;
    @GuardedBy("this")
    private State state = State.FRESH;

    public JpaPersistenceService(Provider<EntityManagerFactory> emfProvider) {
        this.emfProvider = emfProvider;
    }

    public synchronized void start() {
        // Because we implement the provider using lazy loading
        // we do not need to check for the state here. It will
        // simply return the closed EntityManagerFactory
        emfProvider.get();
        this.state = State.STARTED;
    }

    public synchronized void shutdown() {
        // According to the spec (and the spec leads)
        // this method throws ISE when invoked twice, so we prevent it.
        if (State.STARTED == this.state) {
            emfProvider.get().close();
            this.state = State.STOPPED;
        }
    }
    
    private static enum State {
        FRESH, STARTED, STOPPED
    }
}
