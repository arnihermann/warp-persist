package com.wideplay.warp.persist;

/**
 * Internal interface for passing persistence strategies around.
 * @author Robbie Vanbrabant
 */
interface HasPersistenceStrategy {
    PersistenceStrategy getPersistenceStrategy();
}
