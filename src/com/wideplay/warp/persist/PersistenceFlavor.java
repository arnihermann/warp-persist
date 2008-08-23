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
