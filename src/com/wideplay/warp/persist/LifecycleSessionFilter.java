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

import com.wideplay.warp.util.Lifecycle;
import com.wideplay.warp.util.LifecycleAdapter;
import com.wideplay.warp.util.Lifecycles;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SessionFilter that starts and stops all registered {@link PersistenceService} instances
 * upon {@link javax.servlet.Filter#init(javax.servlet.FilterConfig)} and
 * {@link javax.servlet.Filter#destroy()}.
 * 
 * @author Robbie Vanbrabant
 * @see com.wideplay.warp.persist.SessionFilter
 */
public class LifecycleSessionFilter extends SessionFilter {
    private static final List<PersistenceService> persistenceServices = new CopyOnWriteArrayList<PersistenceService>();
    private static final LifecycleAdapter<PersistenceService> lifecycleAdapter = new LifecycleAdapter<PersistenceService>() {
        public Lifecycle asLifecycle(final PersistenceService instance) {
            return new Lifecycle() {
                public void start() {
                    instance.start();
                }
                public void stop() {
                    instance.shutdown();
                }
            };
        }
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Lifecycles<PersistenceService> lifecycles = new Lifecycles<PersistenceService>(lifecycleAdapter);
        lifecycles.failEarly(persistenceServices);

        super.init(filterConfig);
    }

    @Override
    public void destroy() {
        super.destroy();
        
        Lifecycles<PersistenceService> lifecycles = new Lifecycles<PersistenceService>(lifecycleAdapter);
        lifecycles.leaveNoOneBehind(persistenceServices, lifecycleAdapter);
        persistenceServices.clear();
    }

    /**
     * The different persistence strategies should add their
     * {@link com.wideplay.warp.persist.PersistenceService} here
     * at configuration time if they support {@link UnitOfWork#REQUEST}.
     * @param ps the {@code PersistenceService} to register
     */
    public static void registerPersistenceService(PersistenceService ps) {
        persistenceServices.add(ps);
    }
}
