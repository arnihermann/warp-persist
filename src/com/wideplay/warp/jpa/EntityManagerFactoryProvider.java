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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.wideplay.warp.util.LazyReference;
import net.jcip.annotations.Immutable;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
@Immutable
class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
    private final Key<String> persistenceUnitName;
    private final Key<Properties> persistenceProperties;
    private volatile Properties customProperties;
    
    @Inject
    private final Injector injector = null;

    /**
     * Lazily loaded EntityManagerFactory.
     */
    private LazyReference<EntityManagerFactory> emFactory =
            LazyReference.of(new Provider<EntityManagerFactory>() {
                public EntityManagerFactory get() {
                    String psName = injector.getInstance(persistenceUnitName);
                    return Persistence.createEntityManagerFactory(psName);
                    // TODO support custom properties. How do we handle optional values?
                }
            });

    public EntityManagerFactoryProvider(Key<String> persistenceUnitName, Key<Properties> persistenceProperties) {
//                assert null != persistenceUnitName && (!"".equals(persistenceUnitName.trim()))
//                : "Persistence unit name was not set! (please bindConstant().annotatedWith(JpaUnit.class) to the name of a persistence unit";
        this.persistenceUnitName = persistenceUnitName;
        this.persistenceProperties = persistenceProperties; // may be null
    }

    public EntityManagerFactory get() {
        return emFactory.get();
    }
}
