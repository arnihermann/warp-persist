package com.wideplay.warp.persist.spi;

import com.wideplay.warp.persist.WorkManager;
import com.wideplay.warp.persist.PersistenceService;

/**
 * Used to visit a {@link PersistenceModule}
 * and gather state that needs to be used with static methods. This
 * hides the only static state we need (Servlet Filters) behind
 * some OO goodness.
 *
 * @author Robbie Vanbrabant
 */
public interface PersistenceModuleVisitor {
    /**
     * Publishes the module's {@link com.wideplay.warp.persist.WorkManager}
     * for consumption by Warp Persist's common infrastructure,
     * notably {@link com.wideplay.warp.persist.PersistenceFilter}
     * and {@link com.wideplay.warp.persist.PersistenceFilter}.
     * <p>
     * Only use with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}.
     *
     * @param wm the {@code WorkManager} to publish
     */
    void publishWorkManager(WorkManager wm);

    /**
     * Publishes the module's {@link com.wideplay.warp.persist.PersistenceService}
     * for consumption by Warp Persist's common infrastructure,
     * notably {@link com.wideplay.warp.persist.PersistenceFilter}.
     * <p>
     * Usually used with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, but
     * technically it could make sense to use the
     * {@link com.wideplay.warp.persist.PersistenceFilter} with other units
     * of work.
     *
     * @param persistenceService the {@code PersistenceService} to publish
     */
    void publishPersistenceService(PersistenceService persistenceService);
}
