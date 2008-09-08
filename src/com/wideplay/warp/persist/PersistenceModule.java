package com.wideplay.warp.persist;

import com.google.inject.Module;

/**
 * {@link com.google.inject.Module} returned by
 * a {@link com.wideplay.warp.persist.PersistenceStrategy}.
 * 
 * @author Robbie Vanbrabant
 */
public interface PersistenceModule extends Module {
    /**
     * Retrieves the module's {@link com.wideplay.warp.persist.WorkManager}
     * for consumption by Warp Persist's common infrastructure,
     * notably {@link com.wideplay.warp.persist.SessionFilter}
     * and {@link com.wideplay.warp.persist.LifecycleSessionFilter}.
     * <p>
     * Only use with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}.
     * 
     * @return the {@link com.wideplay.warp.persist.WorkManager} used
     *         for {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, or
     *         {@code null} if the module doesn't publish a {@code WorkManager}
     */
    WorkManager publishWorkManager();

    /**
     * Retrieves the module's {@link com.wideplay.warp.persist.PersistenceService}
     * for consumption by Warp Persist's common infrastructure,
     * notably {@link com.wideplay.warp.persist.LifecycleSessionFilter}.
     * <p>
     * Usually used with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, but
     * technically it could make sense to use the
     * {@link com.wideplay.warp.persist.LifecycleSessionFilter} with other units
     * of work.
     *
     * @return the {@link com.wideplay.warp.persist.PersistenceService} or
     *         {@code null} if the module doesn't publish a {@code PersistenceService}
     */
    PersistenceService publishPersistenceService();
}
