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

package com.wideplay.warp.persist.jpa;

import com.google.inject.Provider;
import com.wideplay.warp.persist.internal.InternalWorkManager;
import net.jcip.annotations.Immutable;

import javax.persistence.EntityManager;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 */
@Immutable
class EntityManagerProvider implements Provider<EntityManager> {
    private final InternalWorkManager<EntityManager> internalWorkManager;

    public EntityManagerProvider(InternalWorkManager<EntityManager> internalWorkManager) {
        this.internalWorkManager = internalWorkManager;
    }

    public EntityManager get() {
        return internalWorkManager.beginWork();
    }
}
