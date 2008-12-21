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
package com.wideplay.warp.persist;

import com.google.inject.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Module with bindings that can be useful when using certain Warp Persist configurations.
 * Current bindings:
 * <ul><li>{@code List<PersistenceService>} (accumulating all PersistenceServices in regular or multimodules mode)</li></ul>
 *
 * @author Robbie Vanbrabant
 */
public final class PersistenceServiceExtrasModule extends AbstractModule {
    protected void configure() {
        bind(new TypeLiteral<List<PersistenceService>>(){}).toProvider(ListOfPersistenceServicesProvider.class);
    }

    static class ListOfPersistenceServicesProvider implements Provider<List<PersistenceService>> {
            private final Injector injector;

        @Inject
        public ListOfPersistenceServicesProvider(Injector injector) {
            this.injector = injector;
        }

        public List<PersistenceService> get() {
            List<Binding<PersistenceService>> bindings =
            injector.findBindingsByType(new TypeLiteral<PersistenceService>() {});
            List<PersistenceService> persistenceServices = new ArrayList<PersistenceService>(bindings.size());

            for(Binding binding : bindings) {
                PersistenceService persistenceService = (PersistenceService) binding.getProvider().get();
                persistenceServices.add(persistenceService);
            }

            return persistenceServices;
        }
    }
}
