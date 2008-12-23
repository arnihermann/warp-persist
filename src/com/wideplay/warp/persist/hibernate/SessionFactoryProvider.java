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
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.wideplay.warp.persist.internal.LazyReference;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Immutable
@ThreadSafe
class SessionFactoryProvider implements Provider<SessionFactory> {

    // Injecting the Injector because we can't inject a Hibernate Configuration
    // directly. When using multiple Hibernate modules the user has to bind at least
    // one Configuration using a binding annotation, and we don't know it up front.
    @Inject // injecting finals works and has the same thread safety guarantees as constructors.
    private final Injector injector = null;

    /**
     * Lazily loaded SessionFactory.
     */
    private LazyReference<SessionFactory> sessionFactory =
            LazyReference.of(new Provider<SessionFactory>() {
                public SessionFactory get() {
                    return injector.getInstance(configurationKey).buildSessionFactory();
                }
            });

    /**
     * Key to which the user has bound the Hibernate Configuration.
     * Simply points to the Configuration class if the user did not specify an annotation.
     */
    private final Key<Configuration> configurationKey;
    
    /** Debugging to include in toString. */
    private final String annotationDebug;

    SessionFactoryProvider(Key<Configuration> configurationKey, String annotationDebug) {
        this.configurationKey = configurationKey;
        this.annotationDebug = annotationDebug;
    }

    public SessionFactory get() {
        return sessionFactory.get();
    }

    public String toString() {
        return String.format("%s[boundTo: %s]", super.toString(), this.annotationDebug);
    }
}
