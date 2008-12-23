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

package com.wideplay.warp.persist.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.PersistenceService;
import net.jcip.annotations.Immutable;
import org.hibernate.SessionFactory;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 1.0
 */
@Immutable
class HibernatePersistenceService extends PersistenceService {
    private final Provider<SessionFactory> sessionFactoryProvider;

    @Inject
    public HibernatePersistenceService(Provider<SessionFactory> sessionFactoryProvider) {
        this.sessionFactoryProvider = sessionFactoryProvider;
    }

    public void start() {
        // the provider lazily loads, force start.
        // does its own synchronization and simply returns
        // a closed SessionFactory if it has been closed.
        sessionFactoryProvider.get();
    }

    public synchronized void shutdown() {
        // Hibernate silently lets this call pass
        // if the SessionFactory has been closed already,
        // but a SessionFactory is not thread safe,
        // so we define this method as synchronized.
        // If users use the SessionFactory directly, they're on their own.
        sessionFactoryProvider.get().close();
    }

    public String toString() {
        return String.format("%s[sessionFactory: %s]",super.toString(), this.sessionFactoryProvider);
    }
}
