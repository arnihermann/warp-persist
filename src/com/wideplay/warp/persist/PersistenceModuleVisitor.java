package com.wideplay.warp.persist;

/**
 * Used to visit a {@link com.wideplay.warp.persist.PersistenceModule}
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
     * notably {@link com.wideplay.warp.persist.SessionFilter}
     * and {@link PersistenceServiceSessionFilter}.
     * <p>
     * Only use with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}.
     *
     * @param wm the {@code WorkManager} to publish
     */
    void publishWorkManager(WorkManager wm);

    /**
     * Publishes the module's {@link com.wideplay.warp.persist.PersistenceService}
     * for consumption by Warp Persist's common infrastructure,
     * notably {@link PersistenceServiceSessionFilter}.
     * <p>
     * Usually used with {@link com.wideplay.warp.persist.UnitOfWork#REQUEST}, but
     * technically it could make sense to use the
     * {@link PersistenceServiceSessionFilter} with other units
     * of work.
     *
     * @param persistenceService the {@code PersistenceService} to publish
     */
    void publishPersistenceService(PersistenceService persistenceService);    
}
