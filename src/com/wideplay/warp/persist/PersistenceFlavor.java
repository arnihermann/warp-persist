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

import com.wideplay.warp.db4o.Db4oPersistenceStrategy;
import com.wideplay.warp.hibernate.HibernatePersistenceStrategy;
import com.wideplay.warp.jpa.JpaPersistenceStrategy;

/**
 * @author Robbie Vanbrabant
 */
public enum PersistenceFlavor implements HasPersistenceStrategy {
    HIBERNATE {
        public PersistenceStrategy getPersistenceStrategy() {
            return new HibernatePersistenceStrategy();
        }
    },
    JPA {
        public PersistenceStrategy getPersistenceStrategy() {
            return new JpaPersistenceStrategy();
        }
    },
    DB4O {
        public PersistenceStrategy getPersistenceStrategy() {
            return new Db4oPersistenceStrategy();
        }
    };
}
