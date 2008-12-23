package com.wideplay.warp.persist.internal;

import com.wideplay.warp.persist.PersistenceStrategy;

/**
 * Internal interface for passing persistence strategies around.
 * @author Robbie Vanbrabant
 */
public interface HasPersistenceStrategy {
    PersistenceStrategy getPersistenceStrategy();
}
