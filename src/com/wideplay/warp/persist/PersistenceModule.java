package com.wideplay.warp.persist;

import com.google.inject.Module;

/**
 * @author Robbie Vanbrabant
 */
public interface PersistenceModule extends Module {
    WorkManager getWorkManager();
    PersistenceService getPersistenceService();
}
