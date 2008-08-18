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

import com.google.inject.AbstractModule;
import com.wideplay.warp.db4o.Db4oConfigurationStrategy;
import com.wideplay.warp.hibernate.HibernateConfigurationStrategy;
import com.wideplay.warp.jpa.JpaConfigurationStrategy;
import com.wideplay.warp.persist.Configuration.PersistenceFlavor;
import net.jcip.annotations.ThreadSafe;

/**
 * Created with IntelliJ IDEA.
 * On: 30/04/2007
 *
 * <p>
 * The module built by {@code PersistenceService.using...} that is eventually passed
 * to Guice.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Robbie Vanbrabant
 * @since 1.0
 */
@ThreadSafe
class PersistenceModule extends AbstractModule {
    private final PersistenceFlavor flavor;
    private final Configuration config;

    PersistenceModule(PersistenceFlavor flavor, Configuration config) {
        this.flavor = flavor;
        this.config = config;
    }

    protected void configure() {
        ConfigurationStrategy configStrategy = null;
        switch (flavor) {
            case HIBERNATE:
                configStrategy = new HibernateConfigurationStrategy();
                break;
            case JPA:
                configStrategy = new JpaConfigurationStrategy();
                break;
            case DB4O:
                configStrategy = new Db4oConfigurationStrategy();
                break;
        }
        install(configStrategy.getBindings(config));
    }
}
