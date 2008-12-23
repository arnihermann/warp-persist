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

import com.google.inject.Provider;
import net.jcip.annotations.Immutable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
class SessionProvider implements Provider<Session> {
    //factory is thread safe (presumably!)
    private final Provider<SessionFactory> factory ;

    public SessionProvider(Provider<SessionFactory> factory) {
        this.factory = factory;
    }

    public Session get() {
        return factory.get().getCurrentSession();
    }

    public String toString() {
        return String.format("%s[sessionFactory: %s]", super.toString(), this.factory);
    }
}
