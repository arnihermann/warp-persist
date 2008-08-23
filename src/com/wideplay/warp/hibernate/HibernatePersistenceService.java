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

package com.wideplay.warp.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.PersistenceService;
import net.jcip.annotations.Immutable;
import org.hibernate.SessionFactory;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
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
        sessionFactoryProvider.get();
    }

    public void shutdown() {
        // We don't try to be smart because we don't know what to synchronize on.
        // Leave it up to the caller to check if it has been closed already.
        sessionFactoryProvider.get().close();
    }

    public String toString() {
        return String.format("%s[sessionFactory: %s]",super.toString(), this.sessionFactoryProvider);
    }
}
