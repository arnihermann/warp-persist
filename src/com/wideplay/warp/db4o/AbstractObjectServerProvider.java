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

import com.db4o.ObjectServer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * @author Robbie Vanbrabant
 */
public abstract class AbstractObjectServerProvider implements Provider<ObjectServer> {
    @Inject
    private final Injector injector = null;

    private final Db4oSettings settings;

    public AbstractObjectServerProvider(Db4oSettings settings) {
        this.settings = settings;
    }

    protected Db4oSettings getSettings() {
        return this.settings != null ?
                this.settings :
                injector.getInstance(Db4oPersistenceStrategy.Db4oPersistenceStrategyBuilder.class).buildDb4oSettings();
    }
}
