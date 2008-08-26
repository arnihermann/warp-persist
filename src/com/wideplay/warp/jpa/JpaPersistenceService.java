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

import javax.persistence.EntityManagerFactory;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @since 1.0
 */
class JpaPersistenceService extends PersistenceService {
    private final Provider<EntityManagerFactory> emfProvider;

    public JpaPersistenceService(Provider<EntityManagerFactory> emfProvider) {
        this.emfProvider = emfProvider;
    }

    public void start() {
        emfProvider.get();
    }

    public void shutdown() {
        EntityManagerFactory emf = emfProvider.get();
        if (emf.isOpen()) emf.close();
    }
}
